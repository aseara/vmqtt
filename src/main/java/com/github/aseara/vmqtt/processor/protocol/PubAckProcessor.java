package com.github.aseara.vmqtt.processor.protocol;

import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import com.github.aseara.vmqtt.mqtt.messages.MqttPubAckMessage;
import com.github.aseara.vmqtt.processor.RequestProcessor;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import static com.github.aseara.vmqtt.common.MqttConstants.RESEND_MESSAGE_KEY;

@Slf4j
public class PubAckProcessor extends RequestProcessor<MqttPubAckMessage> {

    private final Vertx vertx;

    public PubAckProcessor(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public Future<MqttEndpoint> processInternal(MqttEndpoint endpoint, MqttPubAckMessage msg) {
        String timeIdKey = RESEND_MESSAGE_KEY + msg.messageId();
        Long timeId = (Long)endpoint.getContextInfo(timeIdKey);
        if (timeId != null) {
            vertx.cancelTimer(timeId);
            endpoint.removeContextInfo(timeIdKey);
        }
        return Future.succeededFuture().map(endpoint);
    }

    @Override
    protected Logger getLog() {
        return log;
    }
}
