package com.github.aseara.vmqtt.processor.protocol;

import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import com.github.aseara.vmqtt.mqtt.messages.MqttDisconnectMessage;
import com.github.aseara.vmqtt.mqtt.messages.codes.MqttDisconnectReasonCode;
import com.github.aseara.vmqtt.processor.RequestProcessor;
import io.vertx.core.Future;
import lombok.extern.slf4j.Slf4j;

import static com.github.aseara.vmqtt.common.MqttConstants.DISCONNECT_KEY;
import static com.github.aseara.vmqtt.mqtt.messages.codes.MqttDisconnectReasonCode.NORMAL;

@Slf4j
public class DisconnectProcessor extends RequestProcessor<MqttDisconnectMessage> {

    @Override
    public Future<MqttEndpoint> processInternal(MqttEndpoint endpoint, MqttDisconnectMessage message) {
        MqttDisconnectReasonCode code = message == null ? NORMAL : message.code();
        log.info("endpoint [{}] disconnected, code: {}", endpoint.clientIdentifier(), code);
        endpoint.putContextInfo(DISCONNECT_KEY, code);
        return Future.succeededFuture(endpoint);
    }
}
