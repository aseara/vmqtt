package com.github.aseara.vmqtt.processor.protocol;

import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import com.github.aseara.vmqtt.mqtt.messages.MqttPubRelMessage;
import com.github.aseara.vmqtt.processor.RequestProcessor;
import com.github.aseara.vmqtt.verticle.MqttVerticle;
import io.vertx.core.Future;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

@Slf4j
public class PubRelProcessor extends RequestProcessor<MqttPubRelMessage> {

    public PubRelProcessor(MqttVerticle verticle) {
        super(verticle);
    }

    @Override
    public Future<MqttEndpoint> processInternal(MqttEndpoint endpoint, MqttPubRelMessage message) {
        boolean rm = sessionStore.delUnReleaseMsg(endpoint.clientIdentifier(), message.messageId());
        log.info("delete message status for msg {}: {}", message.messageId(), rm);
        return Future.succeededFuture(endpoint);
    }

    @Override
    protected Logger getLog() {
        return log;
    }
}
