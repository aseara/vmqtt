package com.github.aseara.vmqtt.exception;

import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import io.vertx.core.VertxException;

public class MqttEndpointException extends VertxException {

    private final MqttEndpoint endpoint;

    public MqttEndpointException(MqttEndpoint endpoint, String message) {
        this(endpoint, message, null);
    }

    public MqttEndpointException(MqttEndpoint endpoint, String message, Throwable t) {
        super(message, t);
        this.endpoint = endpoint;
    }

    public MqttEndpoint getEndpoint() {
        return endpoint;
    }

}
