package com.github.aseara.vmqtt.processor.protocol;

import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import com.github.aseara.vmqtt.mqtt.messages.MqttDisconnectMessage;
import com.github.aseara.vmqtt.processor.RequestProcessor;
import io.vertx.core.Future;

public class DisconnectProcessor implements RequestProcessor<MqttDisconnectMessage> {

    @Override
    public Future<MqttEndpoint> processRequest(MqttEndpoint endpoint, MqttDisconnectMessage message) {
        return null;
    }
}
