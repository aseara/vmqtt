package com.github.aseara.vmqtt.auth;

import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.vertx.core.Future;
import lombok.extern.slf4j.Slf4j;

import static io.netty.handler.codec.mqtt.MqttConnectReturnCode.CONNECTION_ACCEPTED;

@Slf4j
public class AuthService {

    public Future<MqttConnectReturnCode> authEndpoint(MqttEndpoint endpoint) {
        if (endpoint.auth() != null) {
            log.info("[username = " + endpoint.auth().getUsername() + ", password = " + endpoint.auth().getPassword() + "]");
        }

        return Future.succeededFuture(CONNECTION_ACCEPTED);
    }

}
