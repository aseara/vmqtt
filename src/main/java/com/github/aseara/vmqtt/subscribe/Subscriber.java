package com.github.aseara.vmqtt.subscribe;

import com.github.aseara.vmqtt.common.TopicUtil;
import io.netty.handler.codec.mqtt.MqttQoS;
import lombok.Getter;


@Getter
public final class Subscriber {

    private final String clientId;

    private final String subTopic;

    private final MqttQoS qos;

    private final String[] levels;

    public Subscriber(String clientId, String subTopic, MqttQoS qos) {
        this.clientId = clientId;
        this.subTopic = subTopic;
        this.qos = qos;
        this.levels = TopicUtil.splitTopic(subTopic);
    }

    public static Subscriber of(String clientId, String subTopic, MqttQoS qos) {
        return new Subscriber(clientId, subTopic, qos);
    }

    public static Subscriber of(String clientId, String subTopic) {
        return of(clientId, subTopic, null);
    }

}
