package com.github.aseara.vmqtt.processor.protocol;

import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import com.github.aseara.vmqtt.mqtt.messages.MqttPubRelMessage;
import com.github.aseara.vmqtt.processor.RequestProcessor;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

public class PubRelProcessor extends RequestProcessor<MqttPubRelMessage> {

    public PubRelProcessor(Vertx vertx) {
        super(vertx);
    }

    @Override
    public Future<MqttEndpoint> processInternal(MqttEndpoint endpoint, MqttPubRelMessage message) {
        endpoint.publishComplete(message.messageId());
        return null;
    }
}
