package com.github.aseara.vmqtt.processor.protocol;

import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import com.github.aseara.vmqtt.mqtt.messages.MqttPubRecMessage;
import com.github.aseara.vmqtt.processor.RequestProcessor;
import io.vertx.core.Future;

public class PubRecProcessor extends RequestProcessor<MqttPubRecMessage> {

    @Override
    public Future<MqttEndpoint> processInternal(MqttEndpoint endpoint, MqttPubRecMessage message) {
        return Future.succeededFuture().map(endpoint);
    }
}
