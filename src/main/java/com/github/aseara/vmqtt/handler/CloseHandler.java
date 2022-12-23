package com.github.aseara.vmqtt.handler;

import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import io.vertx.core.Handler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CloseHandler implements Handler<MqttEndpoint> {

    @Override
    public void handle(MqttEndpoint endpoint) {
        // TODO check endpoint status to clear resources
        log.info("endpoint for {} is closed.", endpoint.clientIdentifier());
    }
}
