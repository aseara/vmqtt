package com.github.aseara.vmqtt.processor.protocol;

import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import com.github.aseara.vmqtt.mqtt.MqttTopicSubscription;
import com.github.aseara.vmqtt.mqtt.messages.MqttSubscribeMessage;
import com.github.aseara.vmqtt.mqtt.messages.codes.MqttSubAckReasonCode;
import com.github.aseara.vmqtt.processor.RequestProcessor;
import com.github.aseara.vmqtt.retain.RetainMessage;
import com.github.aseara.vmqtt.retain.RetainStorage;
import com.github.aseara.vmqtt.subscribe.Subscriber;
import com.github.aseara.vmqtt.subscribe.SubscriptionTrie;
import com.github.aseara.vmqtt.verticle.MqttVerticle;
import io.netty.handler.codec.mqtt.MqttProperties;
import io.vertx.core.Future;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class SubscribeProcessor extends RequestProcessor<MqttSubscribeMessage> {

    private final MqttVerticle verticle;

    private final SubscriptionTrie subscriptionTrie;

    private final RetainStorage retainStorage;

    public SubscribeProcessor(MqttVerticle verticle, SubscriptionTrie subscriptionTrie, RetainStorage retainStorage) {
        this.verticle = verticle;
        this.subscriptionTrie = subscriptionTrie;
        this.retainStorage = retainStorage;
    }

    @Override
    public Future<MqttEndpoint> processInternal(MqttEndpoint endpoint, MqttSubscribeMessage subscribe) {
        // TODO add session sub store
        List<MqttSubAckReasonCode> reasonCodes = new ArrayList<>();
        for (MqttTopicSubscription s: subscribe.topicSubscriptions()) {
            Subscriber sub = Subscriber.of(endpoint.clientIdentifier(), s.topicName(), s.qualityOfService());
            subscriptionTrie.subscribe(sub);
            sendRetainMessage(endpoint, sub);
            reasonCodes.add(MqttSubAckReasonCode.qosGranted(s.qualityOfService()));
            log.info("client {} subscribe {} with qos {}, total sub now: {}",
                    endpoint.clientIdentifier(), s.topicName(), s.qualityOfService(), subscriptionTrie.getCount());
        }
        // ack the subscriptions request
        endpoint.subscribeAcknowledge(subscribe.messageId(), reasonCodes, MqttProperties.NO_PROPERTIES);
        return Future.succeededFuture(endpoint);
    }

    @Override
    protected Logger getLog() {
        return log;
    }

    private void sendRetainMessage(MqttEndpoint endpoint, Subscriber sub) {
        log.info("lookup retain message and send!");
        List<RetainMessage> msgs = retainStorage.lookup(sub.getLevels());
        for (RetainMessage msg: msgs) {
            verticle.publish(endpoint, msg.topic(), msg.payload(), msg.qos(), true);
        }
    }
}
