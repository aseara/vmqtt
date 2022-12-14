package com.github.aseara.vmqtt.conf;

import lombok.Data;

@Data
public class MqttConfig {

    private Mqtt mqtt;

    @Data
    public static class Mqtt {
        private int port = 1883;
    }

}
