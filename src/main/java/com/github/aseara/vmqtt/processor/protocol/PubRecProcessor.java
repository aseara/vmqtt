package com.github.aseara.vmqtt.processor.protocol;

import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import com.github.aseara.vmqtt.mqtt.messages.MqttPubRecMessage;
import com.github.aseara.vmqtt.processor.RequestProcessor;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import static com.github.aseara.vmqtt.common.MqttConstants.RESEND_MESSAGE_KEY;

@Slf4j
public class PubRecProcessor extends RequestProcessor<MqttPubRecMessage> {

    private final Vertx vertx;

    public PubRecProcessor(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public Future<MqttEndpoint> processInternal(MqttEndpoint endpoint, MqttPubRecMessage msg) {
        String timeIdKey = RESEND_MESSAGE_KEY + msg.messageId();
        Long timeId = (Long)endpoint.getContextInfo(timeIdKey);
        if (timeId != null) {
            vertx.cancelTimer(timeId);
        }
        // set resend message timer
        timeId = vertx.setPeriodic(60, id -> {
            if (endpoint.isClosed()) {
                vertx.cancelTimer(id);
                return;
            }
            endpoint.publishRelease(msg.messageId());
        });
        endpoint.putContextInfo(timeIdKey, timeId);
        return Future.succeededFuture().map(endpoint);
    }

    @Override
    protected Logger getLog() {
        return log;
    }
}
