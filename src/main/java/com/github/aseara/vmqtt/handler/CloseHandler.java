package com.github.aseara.vmqtt.handler;

import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import io.vertx.core.Handler;

public class CloseHandler implements Handler<MqttEndpoint> {
    @Override
    public void handle(MqttEndpoint endpoint) {
        // TODO check endpoint status to clear resources
    }
}
