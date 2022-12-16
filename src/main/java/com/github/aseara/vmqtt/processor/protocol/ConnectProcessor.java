package com.github.aseara.vmqtt.processor.protocol;

import com.github.aseara.vmqtt.auth.AuthService;
import com.github.aseara.vmqtt.mqtt.MqttAuth;
import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import com.github.aseara.vmqtt.processor.RequestProcessor;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;

import static io.netty.handler.codec.mqtt.MqttConnectReturnCode.CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD;
import static io.netty.handler.codec.mqtt.MqttConnectReturnCode.CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION;

@Slf4j
public class ConnectProcessor extends RequestProcessor<MqttEndpoint> {

    private final AuthService authService;

    public ConnectProcessor(Vertx vertx, AuthService authService) {
        super(vertx);
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

        return authService.authEndpoint(endpoint).onSuccess(r -> {
            if (!r) {
                endpoint.reject(CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD);
            }
            afterAuth(endpoint);
        }).map(endpoint);
    }

    private boolean versionValid(int mqttVersion) {
        return mqttVersion == 3 || mqttVersion == 4;
    }

    private boolean auth(MqttEndpoint endpoint) {
        return true;
    }

    private void afterAuth(MqttEndpoint endpoint) {
        Context context = vertx.getOrCreateContext();
        log.info("Run in work context: {}", context.isWorkerContext());

        // shows main connect info
        log.info("MQTT client [" + endpoint.clientIdentifier() + "] request to connect, clean session = " + endpoint.isCleanSession());

        if (endpoint.auth() != null) {
            log.info("[username = " + endpoint.auth().getUsername() + ", password = " + endpoint.auth().getPassword() + "]");
        }
        log.info("[properties = " + endpoint.connectProperties() + "]");
        if (endpoint.will() != null && endpoint.will().isWillFlag()) {
            System.out.println("[will topic = " + endpoint.will().getWillTopic() + " msg = " + new String(endpoint.will().getWillMessageBytes()) +
                    " QoS = " + endpoint.will().getWillQos() + " isRetain = " + endpoint.will().isWillRetain() + "]");
        }

        log.info("[keep alive timeout = " + endpoint.keepAliveTimeSeconds() + "]");

        // accept connection from the remote client
        endpoint.accept(false);
    }

}
