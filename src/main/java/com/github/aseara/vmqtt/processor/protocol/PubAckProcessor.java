package com.github.aseara.vmqtt.processor.protocol;

import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import com.github.aseara.vmqtt.mqtt.messages.MqttPubAckMessage;
import com.github.aseara.vmqtt.processor.RequestProcessor;
import io.vertx.core.Future;

public class PubAckProcessor implements RequestProcessor<MqttPubAckMessage> {

    @Override
    public Future<MqttEndpoint> processRequest(MqttEndpoint endpoint, MqttPubAckMessage message) {
        return Future.succeededFuture().map(endpoint);
    }
}
