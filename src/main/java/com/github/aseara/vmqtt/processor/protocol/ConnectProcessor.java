package com.github.aseara.vmqtt.processor.protocol;

import com.github.aseara.vmqtt.auth.AuthService;
import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import com.github.aseara.vmqtt.processor.RequestProcessor;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;

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
        endpoint.protocolVersion();

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

        return Future.succeededFuture().map(endpoint);
    }

}
