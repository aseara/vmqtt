package com.github.aseara.vmqtt.processor.protocol;

import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import com.github.aseara.vmqtt.mqtt.messages.MqttPubRecMessage;
import com.github.aseara.vmqtt.processor.RequestProcessor;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

public class PubRecProcessor extends RequestProcessor<MqttPubRecMessage> {

    public PubRecProcessor(Vertx vertx) {
        super(vertx);
    }

    @Override
    public Future<MqttEndpoint> processInternal(MqttEndpoint endpoint, MqttPubRecMessage message) {
        return Future.succeededFuture().map(endpoint);
    }
}
