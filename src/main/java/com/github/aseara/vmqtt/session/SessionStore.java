package com.github.aseara.vmqtt.session;

import com.github.aseara.vmqtt.mqtt.messages.MqttPublishMessage;
import com.github.aseara.vmqtt.subscribe.Subscriber;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.VertxException;
import io.vertx.core.buffer.Buffer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SessionStore {

    private final Cache<String, MqttSession> sessionCache;


    public SessionStore(long expireSeconds) {
        sessionCache = CacheBuilder.newBuilder().expireAfterAccess(expireSeconds, TimeUnit.SECONDS).build();
    }

    public boolean create(String clientId) {
        MqttSession session = sessionCache.getIfPresent(clientId);
        if (session != null) {
            return false;
        }
        sessionCache.put(clientId, new MqttSession());
        return true;
    }

    public void touch(String clientId) {
        MqttSession session = sessionCache.getIfPresent(clientId);
        if (session == null) {
            throw new VertxException("session expire for " + clientId);
        }
    }

    public void clear(String clientId) {
        MqttSession session = sessionCache.getIfPresent(clientId);
        if (session != null) {
            sessionCache.invalidate(clientId);
        }
    }

    /**
     * add context info
     * @param key  key
     * @param info value
     */
    public void putContextInfo(String clientId, String key, Object info){
        MqttSession session = sessionCache.getIfPresent(clientId);
        if (session == null) {
            throw new VertxException("session expire for " + clientId);
        }
        session.getContextMap().put(key, info);
    }

    /**
     * get context info
     * @param key key
     * @return value
     */
    public Object getContextInfo(String clientId, String key){
        MqttSession session = sessionCache.getIfPresent(clientId);
        if (session == null) {
            throw new VertxException("session expire for " + clientId);
        }
        return session.getContextMap().get(key);
    }

    /**
     * remove context info
     * @param key key
     */
    public void removeContextInfo(String clientId, String key){
        MqttSession session = sessionCache.getIfPresent(clientId);
        if (session == null) {
            throw new VertxException("session expire for " + clientId);
        }
        session.getContextMap().remove(key);
    }

    public void addSub(String clientId, Subscriber sub) {
        MqttSession session = sessionCache.getIfPresent(clientId);
        if (session == null) {
            throw new VertxException("session expire for " + clientId);
        }
        session.getSubscribers().add(sub);
    }

    public void unsub(String clientId, Subscriber sub) {
        MqttSession session = sessionCache.getIfPresent(clientId);
        if (session == null) {
            throw new VertxException("session expire for " + clientId);
        }
        session.getSubscribers().remove(sub);
    }

    public List<Subscriber> getSubs(String clientId) {
        MqttSession session = sessionCache.getIfPresent(clientId);
        if (session == null) {
            throw new VertxException("session expire for " + clientId);
        }
        return Collections.unmodifiableList(session.getSubscribers());
    }

    public void addUnAckMsg(String clientId, int messageId, String topic, Buffer payload, MqttQoS qos, boolean retain) {
        MqttSession session = sessionCache.getIfPresent(clientId);
        if (session == null) {
            throw new VertxException("session expire for " + clientId);
        }
        MqttPublishMessage msg = MqttPublishMessage
                .create(messageId, qos, false, retain, topic, payload.getByteBuf());
        session.getUnAckMessages().put(messageId, msg);
    }

    public void delUnAckMsg(String clientId, int messageId) {
        MqttSession session = sessionCache.getIfPresent(clientId);
        if (session == null) {
            throw new VertxException("session expire for " + clientId);
        }
        session.getUnAckMessages().remove(messageId);
    }

    public List<MqttPublishMessage> getUnAckPubMsgs(String clientId) {
        MqttSession session = sessionCache.getIfPresent(clientId);
        if (session == null) {
            throw new VertxException("session expire for " + clientId);
        }
        Collection<MqttPublishMessage> msgs = session.getUnAckMessages().values();
        return msgs.size() ==0 ? Collections.emptyList() : new ArrayList<>(msgs);
    }

    public void addUnAckPubrel(String clientId, int messageId) {
        MqttSession session = sessionCache.getIfPresent(clientId);
        if (session == null) {
            throw new VertxException("session expire for " + clientId);
        }
        session.getUnAckPubRelMessages().put(messageId, messageId);
    }

    public void delUnAckPubrel(String clientId, int messageId) {
        MqttSession session = sessionCache.getIfPresent(clientId);
        if (session == null) {
            throw new VertxException("session expire for " + clientId);
        }
        session.getUnAckPubRelMessages().remove(messageId);
    }

    public List<Integer> getUnAckPubrelMsgs(String clientId) {
        MqttSession session = sessionCache.getIfPresent(clientId);
        if (session == null) {
            throw new VertxException("session expire for " + clientId);
        }
        Collection<Integer> msgs = session.getUnAckPubRelMessages().values();
        return msgs.size() ==0 ? Collections.emptyList() : new ArrayList<>(msgs);
    }

    public boolean addOfflineMsg(String clientId, String topic, Buffer payload, MqttQoS qos) {
        MqttSession session = sessionCache.getIfPresent(clientId);
        if (session == null) {
            return false;
        }
        MqttPublishMessage msg = MqttPublishMessage
                .create(0, qos, false, false, topic, payload.getByteBuf());
        session.getOfflineMessages().add(msg);
        return true;
    }

    public List<MqttPublishMessage> getAndClearOfflineMsgs(String clientId) {
        MqttSession session = sessionCache.getIfPresent(clientId);
        if (session == null) {
            return Collections.emptyList();
        }
        List<MqttPublishMessage> offlineMsgs = Collections.unmodifiableList(session.getOfflineMessages());
        session.getOfflineMessages().clear();
        return offlineMsgs;
    }

    public boolean addUnReleaseMsg(String clientId, int messageId) {
        MqttSession session = sessionCache.getIfPresent(clientId);
        if (session == null) {
            throw new VertxException("session expire for " + clientId);
        }
        return session.getUnReleaseMessages().add(messageId);
    }

    public boolean delUnReleaseMsg(String clientId, int messageId) {
        MqttSession session = sessionCache.getIfPresent(clientId);
        if (session == null) {
            throw new VertxException("session expire for " + clientId);
        }
        return session.getUnReleaseMessages().remove(messageId);
    }

}
