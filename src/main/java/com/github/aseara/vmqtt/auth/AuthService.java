package com.github.aseara.vmqtt.auth;

import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import io.vertx.core.Future;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthService {

    public Future<Boolean> authEndpoint(MqttEndpoint endpoint) {
        log.info("[username = " + endpoint.auth().getUsername() + ", password = " + endpoint.auth().getPassword() + "]");
        return Future.succeededFuture(true);
    }

}
