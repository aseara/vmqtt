package com.github.aseara.vmqtt.processor.protocol;

import com.github.aseara.vmqtt.auth.AuthService;
import com.github.aseara.vmqtt.exception.MqttEndpointException;
import com.github.aseara.vmqtt.exception.MqttExceptionHandler;
import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import com.github.aseara.vmqtt.mqtt.messages.MqttPublishMessage;
import com.github.aseara.vmqtt.processor.RequestProcessor;
import com.github.aseara.vmqtt.service.PubService;
import com.github.aseara.vmqtt.subscribe.SubscriptionTrie;
import com.github.aseara.vmqtt.verticle.MqttVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import java.util.WeakHashMap;

import static com.github.aseara.vmqtt.common.MqttConstants.DISCONNECT_KEY;
import static io.netty.handler.codec.mqtt.MqttConnectReturnCode.*;

@Slf4j
public class ConnectProcessor extends RequestProcessor<MqttEndpoint> {

    private final AuthService authService;

    private final PubService pubService;

    private final SubscriptionTrie subTrie;

    private final MqttExceptionHandler exceptionHandler;

    private final WeakHashMap<String, MqttEndpoint> endpointMap = new WeakHashMap<>();

    public ConnectProcessor(MqttVerticle verticle) {
        super(verticle);
        this.authService = verticle.getAuthService();
        this.pubService = verticle.getPubService();
        this.subTrie = verticle.getSubscriptionTrie();
        this.exceptionHandler = verticle.getExceptionHandler();
    }

    public final void processMessage(MqttEndpoint endpoint, Handler<AsyncResult<MqttEndpoint>> handler) {
        processMessage(endpoint, endpoint, handler);
    }

    @Override
    public Future<MqttEndpoint> processInternal(MqttEndpoint endpoint, MqttEndpoint redundant) {
        return processConnect(endpoint);
    }

    @Override
    protected Logger getLog() {
        return log;
    }

    private Future<MqttEndpoint> processConnect(MqttEndpoint endpoint) {
        Future<MqttEndpoint> result = Future.succeededFuture(endpoint);

        if (!versionValid(endpoint.protocolVersion())) {
            endpoint.reject(CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION);
            return result;
        }

        // 1. auth
        // 3. previous client
        // 4. session
        // 5. will
        return  auth(endpoint)
                .onSuccess(r -> {
                    checkPrevious(endpoint);
                    storeWill(endpoint);
                    boolean sessionPresent = processSession(endpoint);
                    accept(endpoint, sessionPresent);
                    afterConn(endpoint);
                }).map(r -> endpoint);
    }

    private boolean versionValid(int mqttVersion) {
        return mqttVersion == 3 || mqttVersion == 4;
    }

    private Future<Void> auth(MqttEndpoint endpoint) {
        Promise<Void> promise = Promise.promise();

        authService.authEndpoint(endpoint).onComplete(ar -> {
            if (ar.failed()) {
                promise.fail(ar.cause().getMessage());
                endpoint.reject(CONNECTION_REFUSED_SERVER_UNAVAILABLE);
                return;
            }
            if (ar.result() != CONNECTION_ACCEPTED) {
                promise.fail("auth failed!");
                endpoint.reject(ar.result());
                return;
            }
            promise.complete();
        });

        return promise.future();
    }

    private void checkPrevious(MqttEndpoint endpoint) {
        MqttEndpoint pre = endpointMap.get(endpoint.clientIdentifier());
        if (pre != null && pre.isConnected()) {
            log.info("[CONNECT] ->{},client= {}, previous connection need to be closed as new connection accepted",
                    pre.remoteAddress(), pre.clientIdentifier());
            pre.close();
        }
    }

    private boolean processSession(MqttEndpoint endpoint) {
        log.info("MQTT client [" + endpoint.clientIdentifier() +
                "] request to connect, clean session = " + endpoint.isCleanSession());

        if (endpoint.isCleanSession()) {
            sessionStore.clear(endpoint.clientIdentifier());
        }

        boolean create = sessionStore.create(endpoint.clientIdentifier());
        if (create) {
            return false;
        }

        vertx.executeBlocking(p -> {
            String clientId = endpoint.clientIdentifier();

            // 1. 订阅
            sessionStore.getSubs(clientId).forEach(subTrie::subscribe);
            // 2. unAckMsgs
            for (MqttPublishMessage msg : sessionStore.getUnAckPubMsgs(clientId)) {
                pubService.publishDup(endpoint, msg.messageId(), msg.topicName(), msg.payload(), msg.qosLevel());
            }
            // 3. unAckPubrel
            sessionStore.getUnAckPubrelMsgs(clientId).forEach(endpoint::publishRelease);
            // 4. offline msg
            for (MqttPublishMessage msg : sessionStore.getAndClearOfflineMsgs(clientId)) {
                pubService.publish(endpoint, msg.topicName(), msg.payload(), msg.qosLevel(), false);
            }
        }, r -> {
            if (r.failed()) {
                exceptionHandler.handle(new MqttEndpointException(endpoint,
                        "session process of connect has some error", r.cause()));
            }
        });
        return true;
    }

    private void storeWill(MqttEndpoint endpoint) {
        if (endpoint.will() != null && endpoint.will().isWillFlag()) {
            log.info("[will topic = " + endpoint.will().getWillTopic() + " msg = " +
                    new String(endpoint.will().getWillMessageBytes()) +
                    " QoS = " + endpoint.will().getWillQos() + " isRetain = " +
                    endpoint.will().isWillRetain() + "]");
        }
    }

    private void accept(MqttEndpoint endpoint, boolean sessionPresent) {
        endpoint.accept(sessionPresent);
        log.info("[CONNECT] ->{},client= {}, connect to this mqtt server success",
                endpoint.remoteAddress(), endpoint.clientIdentifier());
        endpointMap.put(endpoint.clientIdentifier(), endpoint);
    }

    private void afterConn(MqttEndpoint endpoint) {
        // TODO redis record connection to current mqtt server
        // TODO gauge record
        sessionStore.removeContextInfo(endpoint.clientIdentifier(), DISCONNECT_KEY);
    }

}
