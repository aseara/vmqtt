package com.github.aseara.vmqtt.processor.protocol;

import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import com.github.aseara.vmqtt.mqtt.messages.MqttDisconnectMessage;
import com.github.aseara.vmqtt.processor.RequestProcessor;
import io.vertx.core.Future;

public class DisconnectProcessor extends RequestProcessor<MqttDisconnectMessage> {

    @Override
    public Future<MqttEndpoint> processInternal(MqttEndpoint endpoint, MqttDisconnectMessage message) {
        // delete will topic store
        return null;
    }
}
