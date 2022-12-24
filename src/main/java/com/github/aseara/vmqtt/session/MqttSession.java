package com.github.aseara.vmqtt.session;

import com.github.aseara.vmqtt.mqtt.messages.MqttPubRelMessage;
import com.github.aseara.vmqtt.mqtt.messages.MqttPublishMessage;
import com.github.aseara.vmqtt.subscribe.Subscriber;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class MqttSession {

    // 1. 订阅
    // 2. 未确认消息
    // 3. 离线消息
    private final List<Subscriber> subscribers = new ArrayList<>();

    private final Map<Integer, MqttPublishMessage> unAckMessages = new LinkedHashMap<>();

    private final Map<Integer, MqttPubRelMessage> unAckPubRelMessages = new LinkedHashMap<>();

    private final List<MqttPublishMessage> offlineMessages = new ArrayList<>();

}
