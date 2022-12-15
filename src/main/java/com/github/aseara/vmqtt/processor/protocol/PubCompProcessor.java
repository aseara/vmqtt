package com.github.aseara.vmqtt.processor.protocol;

import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import com.github.aseara.vmqtt.mqtt.messages.MqttPubCompMessage;
import com.github.aseara.vmqtt.processor.RequestProcessor;
import io.vertx.core.Future;

public class PubCompProcessor implements RequestProcessor<MqttPubCompMessage> {

    @Override
    public Future<MqttEndpoint> processRequest(MqttEndpoint endpoint, MqttPubCompMessage message) {
        return null;
    }
}
