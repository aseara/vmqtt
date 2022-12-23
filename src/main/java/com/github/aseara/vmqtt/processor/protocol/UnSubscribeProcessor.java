package com.github.aseara.vmqtt.processor.protocol;

import com.github.aseara.vmqtt.subscribe.Subscriber;
import com.github.aseara.vmqtt.subscribe.SubscriptionTrie;
import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import com.github.aseara.vmqtt.mqtt.messages.MqttUnsubscribeMessage;
import com.github.aseara.vmqtt.processor.RequestProcessor;
import io.vertx.core.Future;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

@Slf4j
public class UnSubscribeProcessor extends RequestProcessor<MqttUnsubscribeMessage> {

    private final SubscriptionTrie subscriptionTrie;

    public UnSubscribeProcessor(SubscriptionTrie subscriptionTrie) {
        this.subscriptionTrie = subscriptionTrie;
    }

    @Override
    public Future<MqttEndpoint> processInternal(MqttEndpoint endpoint, MqttUnsubscribeMessage msg) {
        // TODO delete session sub store
        for (String t: msg.topics()) {
            Subscriber unsub = Subscriber.of(endpoint.clientIdentifier(), t);
            if (subscriptionTrie.unsubscribe(unsub)) {
                log.info("Unsubscription for " + t);
            }
        }
        endpoint.unsubscribeAcknowledge(msg.messageId());
        return Future.succeededFuture(endpoint);
    }

    @Override
    protected Logger getLog() {
        return log;
    }
}
