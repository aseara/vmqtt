package com.github.aseara.vmqtt.subscribe;

import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import io.netty.handler.codec.mqtt.MqttQoS;


public record Subscriber(MqttEndpoint endpoint, String subTopic, MqttQoS qos) {

    public static Subscriber of(MqttEndpoint endpoint, String subTopic, MqttQoS qos) {
        return new Subscriber(endpoint, subTopic, qos);
    }

    public static Subscriber of(MqttEndpoint endpoint, String subTopic) {
        return of(endpoint, subTopic, null);
    }

}
