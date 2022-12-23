package com.github.aseara.vmqtt.processor.protocol;

import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import com.github.aseara.vmqtt.mqtt.MqttTopicSubscription;
import com.github.aseara.vmqtt.mqtt.messages.MqttSubscribeMessage;
import com.github.aseara.vmqtt.mqtt.messages.codes.MqttSubAckReasonCode;
import com.github.aseara.vmqtt.processor.RequestProcessor;
import com.github.aseara.vmqtt.subscribe.Subscriber;
import com.github.aseara.vmqtt.subscribe.SubscriptionTrie;
import io.netty.handler.codec.mqtt.MqttProperties;
import io.vertx.core.Future;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class SubscribeProcessor extends RequestProcessor<MqttSubscribeMessage> {

    private final SubscriptionTrie subscriptionTrie;

    public SubscribeProcessor(SubscriptionTrie subscriptionTrie) {
        this.subscriptionTrie = subscriptionTrie;
    }

    @Override
    public Future<MqttEndpoint> processInternal(MqttEndpoint endpoint, MqttSubscribeMessage subscribe) {
        // TODO add session sub store
        List<MqttSubAckReasonCode> reasonCodes = new ArrayList<>();
        for (MqttTopicSubscription s: subscribe.topicSubscriptions()) {
            Subscriber sub = Subscriber.of(endpoint.clientIdentifier(), s.topicName(), s.qualityOfService());
            subscriptionTrie.subscribe(sub);
            sendRetainMessage(sub);
            reasonCodes.add(MqttSubAckReasonCode.qosGranted(s.qualityOfService()));
            log.info("Subscription for " + s.topicName() + " with QoS " + s.qualityOfService());
        }
        // ack the subscriptions request
        endpoint.subscribeAcknowledge(subscribe.messageId(), reasonCodes, MqttProperties.NO_PROPERTIES);
        return Future.succeededFuture(endpoint);
    }

    @Override
    protected Logger getLog() {
        return log;
    }

    private void sendRetainMessage(Subscriber sub) {
        // TODO send retain message
        log.info("lookup retain message and send!");
        // need to be processed using block
        // Vertx.currentContext()
    }
}
