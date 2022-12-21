package com.github.aseara.vmqtt.mqtt.messages.codes;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.codec.mqtt.MqttVersion;

/**
 * Reason codes for SUBACK MQTT message
 */
public enum MqttSubAckReasonCode implements MqttReasonCode {
    //All MQTT versions
    GRANTED_QOS0((byte)0x0),
    GRANTED_QOS1((byte)0x1),
    GRANTED_QOS2((byte)0x2),
    UNSPECIFIED_ERROR((byte)0x80),
    //MQTT5 or higher
    IMPLEMENTATION_SPECIFIC_ERROR((byte)0x83),
    NOT_AUTHORIZED((byte)0x87),
    TOPIC_FILTER_INVALID((byte)0x8F),
    PACKET_IDENTIFIER_IN_USE((byte)0x91),
    QUOTA_EXCEEDED((byte)0x97),
    SHARED_SUBSCRIPTIONS_NOT_SUPPORTED((byte)0x9E),
    SUBSCRIPTION_IDENTIFIERS_NOT_SUPPORTED((byte)0xA1),
    WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED((byte)0xA2);

    MqttSubAckReasonCode(byte byteValue) {
        this.byteValue = byteValue;
    }

    private final byte byteValue;

    public byte value() {
        return byteValue;
    }

    public static MqttSubAckReasonCode qosGranted(MqttQoS qos) {
        return switch (qos) {
            case AT_MOST_ONCE -> MqttSubAckReasonCode.GRANTED_QOS0;
            case AT_LEAST_ONCE -> MqttSubAckReasonCode.GRANTED_QOS1;
            case EXACTLY_ONCE -> MqttSubAckReasonCode.GRANTED_QOS2;
            default -> MqttSubAckReasonCode.UNSPECIFIED_ERROR;
        };
    }
}
