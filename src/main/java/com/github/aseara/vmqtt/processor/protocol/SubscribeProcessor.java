package com.github.aseara.vmqtt.processor.protocol;

import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import com.github.aseara.vmqtt.mqtt.MqttTopicSubscription;
import com.github.aseara.vmqtt.mqtt.messages.MqttSubscribeMessage;
import com.github.aseara.vmqtt.processor.RequestProcessor;
import com.github.aseara.vmqtt.subscribe.Subscriber;
import com.github.aseara.vmqtt.subscribe.SubscriptionTrie;
import io.vertx.core.Future;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SubscribeProcessor extends RequestProcessor<MqttSubscribeMessage> {

    private final SubscriptionTrie subscriptionTrie;

    public SubscribeProcessor(SubscriptionTrie subscriptionTrie) {
        this.subscriptionTrie = subscriptionTrie;
    }

    @Override
    public Future<MqttEndpoint> processInternal(MqttEndpoint endpoint, MqttSubscribeMessage subscribe) {
        for (MqttTopicSubscription s: subscribe.topicSubscriptions()) {
            Subscriber sub = Subscriber.of(endpoint, s.topicName(), s.qualityOfService());
            subscriptionTrie.subscribe(sub);
            log.info("Subscription for " + s.topicName() + " with QoS " + s.qualityOfService());
        }
        return Future.succeededFuture(endpoint);
    }
}
