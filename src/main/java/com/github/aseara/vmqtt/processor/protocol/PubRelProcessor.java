package com.github.aseara.vmqtt.processor.protocol;

import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import com.github.aseara.vmqtt.mqtt.messages.MqttPubRelMessage;
import com.github.aseara.vmqtt.processor.RequestProcessor;
import io.vertx.core.Future;

public class PubRelProcessor implements RequestProcessor<MqttPubRelMessage> {

    @Override
    public Future<MqttEndpoint> processRequest(MqttEndpoint endpoint, MqttPubRelMessage message) {
        endpoint.publishComplete(message.messageId());
        return null;
    }
}
