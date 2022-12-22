package com.github.aseara.vmqtt.processor.protocol;

import com.github.aseara.vmqtt.common.TopicUtil;
import com.github.aseara.vmqtt.exception.MqttEndpointException;
import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import com.github.aseara.vmqtt.mqtt.MqttMessageStatus;
import com.github.aseara.vmqtt.mqtt.messages.MqttPublishMessage;
import com.github.aseara.vmqtt.processor.RequestProcessor;
import com.github.aseara.vmqtt.retain.RetainMessageStorage;
import com.github.aseara.vmqtt.subscribe.Subscriber;
import com.github.aseara.vmqtt.subscribe.SubscriptionTrie;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import java.util.List;

import static com.github.aseara.vmqtt.common.MqttConstants.MESSAGE_STATUS_KEY;
import static com.github.aseara.vmqtt.common.MqttConstants.RESEND_MESSAGE_KEY;

@Slf4j
public class PublishProcessor extends RequestProcessor<MqttPublishMessage> {

    private final Vertx vertx;

    private final RetainMessageStorage retainStorage;

    private final SubscriptionTrie subscriptionTrie;

    public PublishProcessor(Vertx vertx, RetainMessageStorage retainStorage, SubscriptionTrie subscriptionTrie) {
        this.vertx = vertx;
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
        if (message.isRetain()) {
            retainStorage.retain(message);
        }

        String topic = TopicUtil.trimTopic(message.topicName());
        if (!TopicUtil.checkMessageTopic(topic)) {
            throw new MqttEndpointException(endpoint, "invalid message topic: " + message.topicName());
        }

        List<Subscriber> subscribers = subscriptionTrie.lookup(topic);

        Buffer payload = Buffer.buffer(message.payload().getBytes());

        subscribers.forEach(sub -> {
            MqttEndpoint subEndpoint = sub.getEndpoint().get();
            if (subEndpoint != null) {
                MqttQoS sendQos = message.qosLevel().value() > sub.getQos().value() ?
                        sub.getQos() : message.qosLevel();
                dispatchMessage(subEndpoint, topic, payload, sendQos, message.isRetain());
            }
        });
    }

    private void dispatchMessage(MqttEndpoint sub, String topic, Buffer buf,
                                 MqttQoS qos, boolean retain) {
        int messageId = sub.nextMessageId();
        sub.publish(topic, buf, qos, false, retain, messageId).onComplete(ar -> {
            if (ar.succeeded()) {
                log.info("send message success");
            } else {
                log.error("send message failed: ", ar.cause());
            }
        });
        if (qos.value() > MqttQoS.AT_MOST_ONCE.value()) {
            // set resend message timer
            long timeId = vertx.setPeriodic(60, id -> {
                if (sub.isClosed()) {
                    vertx.cancelTimer(id);
                    return;
                }
                sub.publish(topic, buf, qos, true, retain, messageId).onComplete(ar -> {
                    if (ar.succeeded()) {
                        log.info("send message success");
                    } else {
                        log.error("send message failed: ", ar.cause());
                    }
                });
            });
            sub.putContextInfo(RESEND_MESSAGE_KEY + messageId, timeId);
        }
    }

}
