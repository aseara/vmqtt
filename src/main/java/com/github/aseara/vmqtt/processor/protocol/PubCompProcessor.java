package com.github.aseara.vmqtt.processor.protocol;

import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import com.github.aseara.vmqtt.mqtt.messages.MqttPubCompMessage;
import com.github.aseara.vmqtt.processor.RequestProcessor;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import static com.github.aseara.vmqtt.common.MqttConstants.RESEND_MESSAGE_KEY;

@Slf4j
public class PubCompProcessor extends RequestProcessor<MqttPubCompMessage> {

    private final Vertx vertx;

    public PubCompProcessor(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public Future<MqttEndpoint> processInternal(MqttEndpoint endpoint, MqttPubCompMessage msg) {
        String timeIdKey = RESEND_MESSAGE_KEY + msg.messageId();
        Long timeId = (Long)endpoint.getContextInfo(timeIdKey);
        if (timeId != null) {
            vertx.cancelTimer(timeId);
            endpoint.removeContextInfo(timeIdKey);
        }
        return Future.succeededFuture(endpoint);
    }

    @Override
    protected Logger getLog() {
        return log;
    }
}
