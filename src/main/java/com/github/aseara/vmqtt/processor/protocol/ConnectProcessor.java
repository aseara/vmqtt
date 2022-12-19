package com.github.aseara.vmqtt.processor.protocol;

import com.github.aseara.vmqtt.auth.AuthService;
import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import com.github.aseara.vmqtt.processor.RequestProcessor;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import lombok.extern.slf4j.Slf4j;

import java.util.WeakHashMap;

import static io.netty.handler.codec.mqtt.MqttConnectReturnCode.CONNECTION_ACCEPTED;
import static io.netty.handler.codec.mqtt.MqttConnectReturnCode.CONNECTION_REFUSED_SERVER_UNAVAILABLE;
import static io.netty.handler.codec.mqtt.MqttConnectReturnCode.CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION;

@Slf4j
public class ConnectProcessor extends RequestProcessor<MqttEndpoint> {

    private final AuthService authService;

    private final WeakHashMap<String, MqttEndpoint> endpointMap = new WeakHashMap<>();

    public ConnectProcessor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public Future<MqttEndpoint> processInternal(MqttEndpoint endpoint, MqttEndpoint redundant) {
        return processConnect(endpoint);
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
                .onSuccess(r -> checkPrevious(endpoint))
                .onSuccess(r -> storeWill(endpoint))
                .onSuccess(r -> storeSession(endpoint))
                .map(r -> accept(endpoint));
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

    private void storeSession(MqttEndpoint endpoint) {
        log.info("MQTT client [" + endpoint.clientIdentifier() +
                "] request to connect, clean session = " + endpoint.isCleanSession());
    }

    private void storeWill(MqttEndpoint endpoint) {
        if (endpoint.will() != null && endpoint.will().isWillFlag()) {
            log.info("[will topic = " + endpoint.will().getWillTopic() + " msg = " +
                    new String(endpoint.will().getWillMessageBytes()) +
                    " QoS = " + endpoint.will().getWillQos() + " isRetain = " +
                    endpoint.will().isWillRetain() + "]");
        }
    }

    private MqttEndpoint accept(MqttEndpoint endpoint) {
        endpoint.accept();
        log.info("[CONNECT] ->{},client= {}, connect to this mqtt server success",
                endpoint.remoteAddress(), endpoint.clientIdentifier());
        endpointMap.put(endpoint.clientIdentifier(), endpoint);
        // TODO redis record connection to current mqtt server
        // TODO gauge record
        return endpoint;
    }

}
