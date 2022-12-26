package com.github.aseara.vmqtt.processor.protocol;

import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import com.github.aseara.vmqtt.mqtt.messages.MqttPubRecMessage;
import com.github.aseara.vmqtt.processor.RequestProcessor;
import com.github.aseara.vmqtt.verticle.MqttVerticle;
import io.vertx.core.Future;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

@Slf4j
public class PubRecProcessor extends RequestProcessor<MqttPubRecMessage> {

    public PubRecProcessor(MqttVerticle verticle) {
        super(verticle);
    }

    @Override
    public Future<MqttEndpoint> processInternal(MqttEndpoint endpoint, MqttPubRecMessage msg) {
        sessionStore.addUnAckPubrel(endpoint.clientIdentifier(), msg.messageId());
        return Future.succeededFuture().map(endpoint);
    }

    @Override
    protected Logger getLog() {
        return log;
    }
}
