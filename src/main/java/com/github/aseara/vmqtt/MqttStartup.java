package com.github.aseara.vmqtt;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;

public class MqttStartup {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        DeploymentOptions options = new DeploymentOptions()
                .setInstances(Runtime.getRuntime().availableProcessors() * 2);

        vertx.deployVerticle("com.github.aseara.vmqtt.verticle.MqttVerticle", options);
    }

}
