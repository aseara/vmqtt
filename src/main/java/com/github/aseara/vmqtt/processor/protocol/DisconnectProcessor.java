package com.github.aseara.vmqtt.processor.protocol;

import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import com.github.aseara.vmqtt.mqtt.messages.MqttDisconnectMessage;
import com.github.aseara.vmqtt.mqtt.messages.codes.MqttDisconnectReasonCode;
import com.github.aseara.vmqtt.processor.RequestProcessor;
import com.github.aseara.vmqtt.verticle.MqttVerticle;
import io.vertx.core.Future;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import static com.github.aseara.vmqtt.common.MqttConstants.DISCONNECT_KEY;
import static com.github.aseara.vmqtt.mqtt.messages.codes.MqttDisconnectReasonCode.NORMAL;

@Slf4j
public class DisconnectProcessor extends RequestProcessor<MqttDisconnectMessage> {

    public DisconnectProcessor(MqttVerticle verticle) {
        super(verticle);
    }

    @Override
    public Future<MqttEndpoint> processInternal(MqttEndpoint endpoint, MqttDisconnectMessage message) {
        MqttDisconnectReasonCode code = message == null ? NORMAL : message.code();
        log.info("endpoint [{}] disconnected, code: {}", endpoint.clientIdentifier(), code);
        sessionStore.putContextInfo(endpoint.clientIdentifier(), DISCONNECT_KEY, code);
        return Future.succeededFuture(endpoint);
    }

    @Override
    protected Logger getLog() {
        return log;
    }
}
