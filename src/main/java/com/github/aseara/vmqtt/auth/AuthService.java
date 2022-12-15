package com.github.aseara.vmqtt.auth;

import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import io.vertx.core.Future;

public class AuthService {

    public Future<Boolean> authEndpoint(MqttEndpoint endpoint) {
        return Future.succeededFuture();
    }

}
