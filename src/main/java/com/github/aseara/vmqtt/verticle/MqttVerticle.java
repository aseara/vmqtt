package com.github.aseara.vmqtt.verticle;

import com.github.aseara.vmqtt.auth.AuthService;
import com.github.aseara.vmqtt.conf.MqttConfig;
import com.github.aseara.vmqtt.handler.EndpointHandler;
import com.github.aseara.vmqtt.message.SubTrie;
import com.github.aseara.vmqtt.mqtt.MqttServer;
import com.github.aseara.vmqtt.mqtt.MqttServerOptions;
import com.github.aseara.vmqtt.processor.protocol.*;
import com.github.aseara.vmqtt.storage.MemoryStorage;
import com.github.aseara.vmqtt.util.FutureUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MqttVerticle extends AbstractVerticle {

    private final MqttConfig config;

    private final SubTrie subscriptions;

    private final MemoryStorage storage;

    private final EndpointHandler endpointHandler;

    private final List<MqttServer> mqttServers = new ArrayList<>();

    public MqttVerticle(MqttConfig config) {
        this.config = config;
        this.subscriptions = new SubTrie();
        this.storage = new MemoryStorage();
        this.endpointHandler = createHandler();
    }

    @Override
    public void start(Promise<Void> startPromise) {
        for (int i = 0; i < config.getMqtt().getHandlerThreadNum(); i++) {
            //  deploy more instances of the MQTT server to use more cores.
            mqttServers.add(createMqttServer());
        }

        FutureUtil.listAll(mqttServers, MqttServer::listen, ar -> {
            if (ar.succeeded()) {
                log.info("MQTT server is listening on port " + config.getMqtt().getPort());
                startPromise.complete();
            } else {
                log.error("start failed: ", ar.cause());
                startPromise.fail(ar.cause());
            }
        });
    }

    private MqttServer createMqttServer() {
        MqttServerOptions options = new MqttServerOptions();
        options.setPort(config.getMqtt().getPort());

        return MqttServer.create(vertx, options)
                .endpointHandler(endpointHandler)
                .exceptionHandler(t -> {
                    log.error("mqtt process error: ", t);
                });
    }

    @Override
    public void stop(Promise<Void> stopPromise) {
        FutureUtil.listAll(mqttServers, MqttServer::close, ar -> {
            if (ar.succeeded()) {
                stopPromise.complete();
            } else {
                stopPromise.fail(ar.cause());
            }
        });
    }

    private EndpointHandler createHandler() {
        AuthService authService = new AuthService();
        ConnectProcessor connect = new ConnectProcessor(authService);
        DisconnectProcessor disconnect = new DisconnectProcessor();
        PublishProcessor publish = new PublishProcessor(storage, subscriptions);
        PubAckProcessor pubAck = new PubAckProcessor();
        PubRecProcessor pubRec = new PubRecProcessor();
        PubRelProcessor pubRel = new PubRelProcessor();
        PubCompProcessor pubComp = new PubCompProcessor();
        SubscribeProcessor sub = new SubscribeProcessor();
        UnSubscribeProcessor unSub = new UnSubscribeProcessor();
        return new EndpointHandler(connect, disconnect, publish, pubAck, pubRec, pubRel, pubComp, sub, unSub);
    }

}
