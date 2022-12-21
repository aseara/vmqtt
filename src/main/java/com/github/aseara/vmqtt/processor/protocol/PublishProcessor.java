package com.github.aseara.vmqtt.processor.protocol;

import com.github.aseara.vmqtt.subscribe.SubscriptionTrie;
import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import com.github.aseara.vmqtt.mqtt.messages.MqttPublishMessage;
import com.github.aseara.vmqtt.processor.RequestProcessor;
import com.github.aseara.vmqtt.retain.RetainMessageStorage;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Future;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;

@Slf4j
public class PublishProcessor extends RequestProcessor<MqttPublishMessage> {

    private final RetainMessageStorage retainStorage;

    private final SubscriptionTrie subscriptionTrie;

    public PublishProcessor(RetainMessageStorage retainStorage, SubscriptionTrie subscriptionTrie) {
        this.retainStorage = retainStorage;
        this.subscriptionTrie = subscriptionTrie;
    }

    @Override
    public Future<MqttEndpoint> processInternal(MqttEndpoint endpoint, MqttPublishMessage message) {
        log.info("Just received message [" + message.payload().toString(Charset.defaultCharset()) + "] with QoS [" + message.qosLevel() + "]");

        // 1. qos < 2 process message
        // 2. qos = 2 check message status record to determine the processing of message,
        //    update or create a new message status record.

        if (message.qosLevel() != MqttQoS.EXACTLY_ONCE) {
            processMessage(message);
        } else {

        }

        if (message.isRetain()) {
            retainStorage.retain(message);
        }

        return Future.succeededFuture().map(endpoint);
    }

    private void processMessage(MqttPublishMessage message) {

    }

}
