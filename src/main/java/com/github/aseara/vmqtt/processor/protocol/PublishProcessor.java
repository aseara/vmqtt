package com.github.aseara.vmqtt.processor.protocol;

import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import com.github.aseara.vmqtt.mqtt.messages.MqttPublishMessage;
import com.github.aseara.vmqtt.processor.RequestProcessor;
import com.github.aseara.vmqtt.service.PubService;
import com.github.aseara.vmqtt.verticle.MqttVerticle;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Future;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

@Slf4j
public class PublishProcessor extends RequestProcessor<MqttPublishMessage> {

    private final PubService pubService;

    public PublishProcessor(MqttVerticle verticle) {
        super(verticle);
        this.pubService = verticle.getPubService();
    }

    @Override
    public Future<MqttEndpoint> processInternal(MqttEndpoint endpoint, MqttPublishMessage message) {
        log.info("Just received {} message [len:{}] with QoS [{}], Dup [{}], Retain [{}]",
                message.topicName(),
                message.payload().length(),
                message.qosLevel(),
                message.isDup(),
                message.isRetain());

        // 1. qos < 2 process message
        // 2. qos = 2 check message status record to determine the processing of message,
        //    update or create a new message status record.

        Future<MqttEndpoint> result = Future.succeededFuture(endpoint);
        if (message.qosLevel() != MqttQoS.EXACTLY_ONCE ||
                sessionStore.addUnReleaseMsg(endpoint.clientIdentifier(), message.messageId())) {
            pubService.publish(message.topicName(), message.payload(), message.qosLevel(), message.isRetain());
        }

        return result;
    }

    @Override
    protected Logger getLog() {
        return log;
    }

}
