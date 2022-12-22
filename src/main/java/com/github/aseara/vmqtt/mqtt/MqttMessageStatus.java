package com.github.aseara.vmqtt.mqtt;


import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
public class MqttMessageStatus {

    private final int messageId;

    private final long firstRecordTime = System.currentTimeMillis();

    private final AtomicInteger recordTime = new AtomicInteger(0);

    public MqttMessageStatus(int messageId) {
        this.messageId = messageId;
    }

    public int incrementRecordTime() {
        return recordTime.incrementAndGet();
    }

}
