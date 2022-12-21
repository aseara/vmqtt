package com.github.aseara.vmqtt.mqtt;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MqttMessageStatus {

    private final int messageId;

    private int phase;

    public MqttMessageStatus(int messageId) {
        this.messageId = messageId;
    }



}
