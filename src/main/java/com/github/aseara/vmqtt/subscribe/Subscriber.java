package com.github.aseara.vmqtt.subscribe;

import com.github.aseara.vmqtt.common.TopicUtil;
import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import io.netty.handler.codec.mqtt.MqttQoS;
import lombok.Getter;

import java.lang.ref.WeakReference;


@Getter
public final class Subscriber {

    private final WeakReference<MqttEndpoint> endpoint;

    private final String subTopic;

    private final MqttQoS qos;

    private final String[] levels;

    public Subscriber(MqttEndpoint endpoint, String subTopic, MqttQoS qos) {
        this.endpoint = new WeakReference<>(endpoint);
        this.subTopic = subTopic;
        this.qos = qos;
        this.levels = TopicUtil.splitTopic(subTopic);
    }

    public static Subscriber of(MqttEndpoint endpoint, String subTopic, MqttQoS qos) {
        return new Subscriber(endpoint, subTopic, qos);
    }

    public static Subscriber of(MqttEndpoint endpoint, String subTopic) {
        return of(endpoint, subTopic, null);
    }

}
