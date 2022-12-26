package com.github.aseara.vmqtt.service;

import com.github.aseara.vmqtt.common.TopicUtil;
import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import com.github.aseara.vmqtt.retain.RetainMessage;
import com.github.aseara.vmqtt.retain.RetainStorage;
import com.github.aseara.vmqtt.session.SessionStore;
import com.github.aseara.vmqtt.subscribe.Subscriber;
import com.github.aseara.vmqtt.subscribe.SubscriptionTrie;
import com.github.aseara.vmqtt.verticle.MqttVerticle;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.VertxException;
import io.vertx.core.buffer.Buffer;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;

@Slf4j
public class PubService {

    private final SessionStore sessionStore;

    private final RetainStorage retainStorage;

    private final SubscriptionTrie subTrie;

    private final Cache<String, MqttEndpoint> endpointCache =
            CacheBuilder.newBuilder().weakValues().build();

    public PubService(MqttVerticle verticle) {
        this.sessionStore = verticle.getSessionStore();
        this.retainStorage = verticle.getRetainStorage();
        this.subTrie = verticle.getSubscriptionTrie();
    }

    public void publish(String topic, Buffer buff, MqttQoS recvQos, boolean retain) {
        Buffer payload = Buffer.buffer(buff.getBytes());

        String trimTopic = TopicUtil.trimTopic(topic);
        if (!TopicUtil.checkMessageTopic(trimTopic)) {
            throw new VertxException( "invalid message topic: " + topic);
        }

        String[] topicLevels = TopicUtil.splitTopic(trimTopic);

        if (retain) {
            RetainMessage retainMsg = new RetainMessage(trimTopic, topicLevels, payload, recvQos);
            retainStorage.retain(retainMsg);
        }

        Collection<Subscriber> subscribers = subTrie.lookup(topicLevels);
        subscribers.forEach(sub -> {
            MqttEndpoint subEndpoint = getEndpoint(sub.getClientId());

            MqttQoS sendQos = recvQos.value() > sub.getQos().value() ? sub.getQos() : recvQos;
            if (subEndpoint != null && subEndpoint.isConnected()) {
                publish(subEndpoint, trimTopic, payload, sendQos, false);
            } else {
                boolean save = sessionStore.addOfflineMsg(sub.getClientId(), trimTopic, payload, sendQos);
                if (save) {
                    log.info("save to offline message list");
                }
            }
        });
    }

    public void publish(MqttEndpoint sub, String topic, Buffer buf, MqttQoS qos, boolean retain) {
        int messageId = sub.nextMessageId();
        sub.publish(topic, buf, qos, false, retain, messageId).onComplete(ar -> {
            if (ar.succeeded()) {
                log.info("send message success");
            } else {
                log.error("send message failed: ", ar.cause());
            }
        });
        if (qos.value() > MqttQoS.AT_MOST_ONCE.value()) {
            log.info("store message to sub session");
            sessionStore.addUnAckMsg(sub.clientIdentifier(), messageId, topic, buf, qos, retain);
        }
    }

    public void publishDup(MqttEndpoint sub, int messageId, String topic, Buffer buf, MqttQoS qos) {
        sub.publish(topic, buf, qos, true, false, messageId).onComplete(ar -> {
            if (ar.succeeded()) {
                log.info("send message success");
            } else {
                log.error("send message failed: ", ar.cause());
            }
        });
    }

    public void cacheEndpoint(MqttEndpoint endpoint) {
        endpointCache.put(endpoint.clientIdentifier(), endpoint);
    }

    private MqttEndpoint getEndpoint(String clientId) {
        return endpointCache.getIfPresent(clientId);
    }

}
