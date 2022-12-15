package com.github.aseara.vmqtt.pubsub;

import com.github.aseara.vmqtt.message.SubTrie;
import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import com.github.aseara.vmqtt.mqtt.messages.MqttPublishMessage;
import com.github.aseara.vmqtt.storage.MemoryStorage;
import io.netty.handler.codec.mqtt.MqttQoS;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;

@Slf4j
public class PubSubService {

    private final MemoryStorage storage;

    private final SubTrie subscriptions;

    public PubSubService(SubTrie subscriptions, MemoryStorage storage) {
        this.subscriptions = subscriptions;
        this.storage = storage;
    }

    public void onPublish(MqttEndpoint endpoint, MqttPublishMessage message) {
        log.info("Just received message [" + message.payload().toString(Charset.defaultCharset()) + "] with QoS [" + message.qosLevel() + "]");

        if (message.qosLevel() == MqttQoS.AT_LEAST_ONCE) {
            endpoint.publishAcknowledge(message.messageId());

        } else if (message.qosLevel() == MqttQoS.EXACTLY_ONCE) {
            endpoint.publishReceived(message.messageId());
        }
    }

}
