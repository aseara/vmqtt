package com.github.aseara.vmqtt.processor.protocol;

import com.github.aseara.vmqtt.auth.AuthService;
import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import io.vertx.core.Future;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConnectProcessor {

    private final AuthService authService;

    public ConnectProcessor(AuthService authService) {
        this.authService = authService;
    }

    public Future<MqttEndpoint> processRequest(MqttEndpoint endpoint) {
        // shows main connect info
        log.info("MQTT client [" + endpoint.clientIdentifier() + "] request to connect, clean session = " + endpoint.isCleanSession());

        if (endpoint.auth() != null) {
            log.info("[username = " + endpoint.auth().getUsername() + ", password = " + endpoint.auth().getPassword() + "]");
        }
        log.info("[properties = " + endpoint.connectProperties() + "]");
        if (endpoint.will() != null) {
            System.out.println("[will topic = " + endpoint.will().getWillTopic() + " msg = " + new String(endpoint.will().getWillMessageBytes()) +
                    " QoS = " + endpoint.will().getWillQos() + " isRetain = " + endpoint.will().isWillRetain() + "]");
        }

        log.info("[keep alive timeout = " + endpoint.keepAliveTimeSeconds() + "]");

        // accept connection from the remote client
        endpoint.accept(false);

        return Future.succeededFuture().map(endpoint);
    }
}
