package com.github.aseara.vmqtt.processor.protocol;

import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import com.github.aseara.vmqtt.mqtt.messages.MqttPubRelMessage;
import com.github.aseara.vmqtt.processor.RequestProcessor;
import io.vertx.core.Future;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import static com.github.aseara.vmqtt.common.MqttConstants.MESSAGE_STATUS_KEY;

@Slf4j
public class PubRelProcessor extends RequestProcessor<MqttPubRelMessage> {

    @Override
    public Future<MqttEndpoint> processInternal(MqttEndpoint endpoint, MqttPubRelMessage message) {
        boolean rm = endpoint.removeContextInfo(MESSAGE_STATUS_KEY + message.messageId());
        log.info("delete message status for msg {}: {}", message.messageId(), rm);
        return Future.succeededFuture(endpoint);
    }

    @Override
    protected Logger getLog() {
        return log;
    }
}
