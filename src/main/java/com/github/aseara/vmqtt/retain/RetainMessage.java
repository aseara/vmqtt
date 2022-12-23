package com.github.aseara.vmqtt.retain;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.buffer.Buffer;

public record RetainMessage(String topic, String[] levels, Buffer payload, MqttQoS qos) {

}
