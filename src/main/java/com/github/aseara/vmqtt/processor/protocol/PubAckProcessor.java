package com.github.aseara.vmqtt.processor.protocol;

import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import com.github.aseara.vmqtt.mqtt.messages.MqttPubAckMessage;
import com.github.aseara.vmqtt.processor.RequestProcessor;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

public class PubAckProcessor extends RequestProcessor<MqttPubAckMessage> {

    public PubAckProcessor(Vertx vertx) {
        super(vertx);
    }

    @Override
    public Future<MqttEndpoint> processInternal(MqttEndpoint endpoint, MqttPubAckMessage message) {
        return Future.succeededFuture().map(endpoint);
    }
}
