package com.github.aseara.vmqtt.processor.protocol;

import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import com.github.aseara.vmqtt.mqtt.messages.MqttDisconnectMessage;
import com.github.aseara.vmqtt.processor.RequestProcessor;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

public class DisconnectProcessor extends RequestProcessor<MqttDisconnectMessage> {

    public DisconnectProcessor(Vertx vertx) {
        super(vertx);
    }

    @Override
    public Future<MqttEndpoint> processInternal(MqttEndpoint endpoint, MqttDisconnectMessage message) {
        return null;
    }
}
