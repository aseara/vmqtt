package com.github.aseara.vmqtt.handler;

import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import com.github.aseara.vmqtt.mqtt.MqttWill;
import com.github.aseara.vmqtt.service.PubService;
import com.github.aseara.vmqtt.session.SessionStore;
import com.github.aseara.vmqtt.subscribe.SubscriptionTrie;
import com.github.aseara.vmqtt.verticle.MqttVerticle;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Handler;
import lombok.extern.slf4j.Slf4j;

import static com.github.aseara.vmqtt.common.MqttConstants.DISCONNECT_KEY;
import static com.github.aseara.vmqtt.mqtt.messages.codes.MqttDisconnectReasonCode.NORMAL;

@Slf4j
public class CloseHandler implements Handler<MqttEndpoint> {
    private final SessionStore sessionStore;
    private final PubService pubService;
    private final SubscriptionTrie subTrie;


    public CloseHandler(MqttVerticle verticle) {
        this.sessionStore = verticle.getSessionStore();
        this.subTrie = verticle.getSubscriptionTrie();
        this.pubService = verticle.getPubService();
    }

    @Override
    public void handle(MqttEndpoint endpoint) {
        Object code = sessionStore.getContextInfo(endpoint.clientIdentifier(), DISCONNECT_KEY);
        MqttWill will = endpoint.will();
        if (NORMAL != code && will != null) {
            pubService.publish(will.getWillTopic(), will.getWillMessage(),
                    MqttQoS.valueOf(will.getWillQos()), will.isWillRetain());
        }
        sessionStore.removeContextInfo(endpoint.clientIdentifier(), DISCONNECT_KEY);
        sessionStore.getSubs(endpoint.clientIdentifier()).forEach(subTrie::unsubscribe);
        if (endpoint.isCleanSession()) {
            sessionStore.clear(endpoint.clientIdentifier());
        }

        log.info("endpoint for {} is closed.", endpoint.clientIdentifier());
    }
}
