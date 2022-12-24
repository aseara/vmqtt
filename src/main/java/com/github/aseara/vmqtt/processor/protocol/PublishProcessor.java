package com.github.aseara.vmqtt.processor.protocol;

import com.github.aseara.vmqtt.common.TopicUtil;
import com.github.aseara.vmqtt.exception.MqttEndpointException;
import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import com.github.aseara.vmqtt.mqtt.MqttMessageStatus;
import com.github.aseara.vmqtt.mqtt.messages.MqttPublishMessage;
import com.github.aseara.vmqtt.processor.RequestProcessor;
import com.github.aseara.vmqtt.retain.RetainMessage;
import com.github.aseara.vmqtt.retain.RetainStorage;
import com.github.aseara.vmqtt.subscribe.Subscriber;
import com.github.aseara.vmqtt.subscribe.SubscriptionTrie;
import com.github.aseara.vmqtt.verticle.MqttVerticle;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import java.util.Collection;

import static com.github.aseara.vmqtt.common.MqttConstants.MESSAGE_STATUS_KEY;

@Slf4j
public class PublishProcessor extends RequestProcessor<MqttPublishMessage> {

    private final MqttVerticle verticle;

    private final RetainStorage retainStorage;

    private final SubscriptionTrie subscriptionTrie;

    public PublishProcessor(MqttVerticle verticle, RetainStorage retainStorage, SubscriptionTrie subscriptionTrie) {
        this.verticle = verticle;
        this.retainStorage = retainStorage;
        this.subscriptionTrie = subscriptionTrie;
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
        if (message.qosLevel() != MqttQoS.EXACTLY_ONCE) {
            retainAndDispatch(endpoint, message);
            return result;
        }
        MqttMessageStatus status =
                (MqttMessageStatus) endpoint.getContextInfo(MESSAGE_STATUS_KEY + message.messageId());
        if (status == null) {
            retainAndDispatch(endpoint, message);
            status = new MqttMessageStatus(message.messageId());
            endpoint.putContextInfo(MESSAGE_STATUS_KEY + message.messageId(), status);
        }
        int recCnt = status.incrementRecordTime();
        log.info("message qos=2 record {} time(s).", recCnt);
        return result;
    }

    @Override
    protected Logger getLog() {
        return log;
    }

    private void retainAndDispatch(MqttEndpoint endpoint, MqttPublishMessage message) {

        Buffer payload = Buffer.buffer(message.payload().getBytes());

        String topic = TopicUtil.trimTopic(message.topicName());
        if (!TopicUtil.checkMessageTopic(topic)) {
            throw new MqttEndpointException(endpoint, "invalid message topic: " + message.topicName());
        }

        String[] topicLevels = TopicUtil.splitTopic(topic);

        if (message.isRetain()) {
            RetainMessage retain = new RetainMessage(topic, topicLevels, payload, message.qosLevel());
            retainStorage.retain(retain);
        }

        Collection<Subscriber> subscribers = subscriptionTrie.lookup(topicLevels);
        subscribers.forEach(sub -> {
            MqttEndpoint subEndpoint = verticle.getEndpoint(sub.getClientId());
            if (subEndpoint != null && subEndpoint.isConnected()) {
                MqttQoS sendQos = message.qosLevel().value() > sub.getQos().value() ?
                        sub.getQos() : message.qosLevel();
                verticle.publish(subEndpoint, topic, payload, sendQos, false);
            }
        });
    }

}
