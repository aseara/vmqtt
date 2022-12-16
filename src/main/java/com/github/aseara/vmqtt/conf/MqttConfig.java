package com.github.aseara.vmqtt.conf;

import lombok.Data;

import static io.netty.handler.codec.mqtt.MqttConstant.DEFAULT_MAX_CLIENT_ID_LENGTH;

@Data
public class MqttConfig {

    private Mqtt mqtt;

    @Data
    public static class Mqtt {
        private int port = 1883;

        private int handlerThreadNum = Runtime.getRuntime().availableProcessors() * 2;

        private int maxMsgSize = 8192;

        // when mqtt.version = 3.1 will check client id length
        private int maxClientIdLength = DEFAULT_MAX_CLIENT_ID_LENGTH * 2;

        private boolean logNetty;
    }

}
