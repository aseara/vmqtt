package com.github.aseara.vmqtt.processor.protocol;

import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import com.github.aseara.vmqtt.mqtt.messages.MqttPubCompMessage;
import com.github.aseara.vmqtt.processor.RequestProcessor;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

public class PubCompProcessor extends RequestProcessor<MqttPubCompMessage> {

    public PubCompProcessor(Vertx vertx) {
        super(vertx);
    }

    @Override
    public Future<MqttEndpoint> processInternal(MqttEndpoint endpoint, MqttPubCompMessage message) {
        return null;
    }
}
