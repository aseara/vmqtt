package com.github.aseara.vmqtt.session;

import com.github.aseara.vmqtt.mqtt.messages.MqttPublishMessage;
import com.github.aseara.vmqtt.subscribe.Subscriber;
import lombok.Data;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Data
public class MqttSession {

    private final List<Subscriber> subscribers =
            Collections.synchronizedList(new ArrayList<>());

    private final Map<Integer, MqttPublishMessage> unAckMessages =
            Collections.synchronizedMap(new LinkedHashMap<>());

    private final Map<Integer, Integer> unAckPubRelMessages =
            Collections.synchronizedMap(new LinkedHashMap<>());

    private final Set<Integer> unReleaseMessages =
            Collections.synchronizedSet(new HashSet<>());

    private final List<MqttPublishMessage> offlineMessages =
            Collections.synchronizedList(new ArrayList<>());

    private final ConcurrentMap<String, Object> contextMap =
            new ConcurrentHashMap<>();

}
