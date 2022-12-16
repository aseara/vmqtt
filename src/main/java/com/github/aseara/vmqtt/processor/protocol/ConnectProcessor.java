package com.github.aseara.vmqtt.processor.protocol;

import com.github.aseara.vmqtt.auth.AuthService;
import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import com.github.aseara.vmqtt.mqtt.impl.MqttEndpointImpl;
import com.github.aseara.vmqtt.processor.RequestProcessor;
import io.vertx.core.Context;
import io.vertx.core.Future;
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

        // shows main connect info
        log.info("MQTT client [" + endpoint.clientIdentifier() + "] request to connect, clean session = " + endpoint.isCleanSession());

        if (!versionValid(endpoint.protocolVersion())) {
            endpoint.reject(CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION);
            return result;
        }

        // 1. auth
        // 2. keep alive
        // 3. previous client
        // 4. session
        // 5. will

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

    private void afterAuth(MqttEndpoint endpoint) {

        if (endpoint.will() != null && endpoint.will().isWillFlag()) {
            System.out.println("[will topic = " + endpoint.will().getWillTopic() + " msg = " + new String(endpoint.will().getWillMessageBytes()) +
                    " QoS = " + endpoint.will().getWillQos() + " isRetain = " + endpoint.will().isWillRetain() + "]");
        }

        log.info("[keep alive timeout = " + endpoint.keepAliveTimeSeconds() + "]");

        // accept connection from the remote client
        endpoint.accept(false);
    }

    private Future<Boolean> keepAlive(MqttEndpoint endpoint) {
        if (endpoint instanceof MqttEndpointImpl) {
//            vertx.getOrCreateContext().
        }
        return Future.succeededFuture(true);
    }

}
