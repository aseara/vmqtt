package com.github.aseara.vmqtt.processor.protocol;

import com.github.aseara.vmqtt.session.SessionStore;
import com.github.aseara.vmqtt.subscribe.Subscriber;
import com.github.aseara.vmqtt.subscribe.SubscriptionTrie;
import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import com.github.aseara.vmqtt.mqtt.messages.MqttUnsubscribeMessage;
import com.github.aseara.vmqtt.processor.RequestProcessor;
import com.github.aseara.vmqtt.verticle.MqttVerticle;
import io.vertx.core.Future;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

@Slf4j
public class UnSubscribeProcessor extends RequestProcessor<MqttUnsubscribeMessage> {

    private final SubscriptionTrie subscriptionTrie;

    private final SessionStore sessionStore;

    public UnSubscribeProcessor(MqttVerticle verticle) {
        super(verticle);
        this.subscriptionTrie = verticle.getSubscriptionTrie();
        this.sessionStore = verticle.getSessionStore();
    }

    @Override
    public Future<MqttEndpoint> processInternal(MqttEndpoint endpoint, MqttUnsubscribeMessage msg) {
        for (String t: msg.topics()) {
            Subscriber unsub = Subscriber.of(endpoint.clientIdentifier(), t);
            subscriptionTrie.unsubscribe(unsub);
            sessionStore.unsub(endpoint.clientIdentifier(), unsub);
            log.info("Unsubscription for " + t);
        }
        endpoint.unsubscribeAcknowledge(msg.messageId());
        return Future.succeededFuture(endpoint);
    }

    @Override
    protected Logger getLog() {
        return log;
    }
}
