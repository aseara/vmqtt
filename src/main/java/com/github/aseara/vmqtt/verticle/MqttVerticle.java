package com.github.aseara.vmqtt.verticle;

import com.github.aseara.vmqtt.auth.AuthService;
import com.github.aseara.vmqtt.common.FutureUtil;
import com.github.aseara.vmqtt.conf.MqttConfig;
import com.github.aseara.vmqtt.exception.MqttExceptionHandler;
import com.github.aseara.vmqtt.handler.CloseHandler;
import com.github.aseara.vmqtt.handler.EndpointHandler;
import com.github.aseara.vmqtt.subscribe.SubscriptionTrie;
import com.github.aseara.vmqtt.mqtt.MqttServer;
import com.github.aseara.vmqtt.mqtt.MqttServerOptions;
import com.github.aseara.vmqtt.processor.protocol.ConnectProcessor;
import com.github.aseara.vmqtt.processor.protocol.DisconnectProcessor;
import com.github.aseara.vmqtt.processor.protocol.PubAckProcessor;
import com.github.aseara.vmqtt.processor.protocol.PubCompProcessor;
import com.github.aseara.vmqtt.processor.protocol.PubRecProcessor;
import com.github.aseara.vmqtt.processor.protocol.PubRelProcessor;
import com.github.aseara.vmqtt.processor.protocol.PublishProcessor;
import com.github.aseara.vmqtt.processor.protocol.SubscribeProcessor;
import com.github.aseara.vmqtt.processor.protocol.UnSubscribeProcessor;
import com.github.aseara.vmqtt.retain.RetainMessageStorage;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MqttVerticle extends AbstractVerticle {

    private final MqttConfig config;

    private final SubscriptionTrie subscriptionTrie;

    private final RetainMessageStorage storage;

    private EndpointHandler endpointHandler;

    private final MqttExceptionHandler exceptionHandler = new MqttExceptionHandler();

    private final CloseHandler closeHandler = new CloseHandler();

    private final List<MqttServer> mqttServers = new ArrayList<>();

    public MqttVerticle(MqttConfig config) {
        this.config = config;
        this.subscriptionTrie = new SubscriptionTrie();
        this.storage = new RetainMessageStorage();
    }

    @Override
    public void start(Promise<Void> startPromise) {
        this.endpointHandler = createHandler();

        vertx.getOrCreateContext().put("", "");

        MqttServerOptions options = new MqttServerOptions()
                .setPort(config.getMqtt().getPort())
                .setMaxMessageSize(config.getMqtt().getMaxMsgSize())
                .setMaxClientIdLength(config.getMqtt().getMaxClientIdLength());

        options.setLogActivity(config.getMqtt().isLogNetty());

        for (int i = 0; i < config.getMqtt().getHandlerThreadNum(); i++) {
            //  deploy more instances of the MQTT server to use more cores.
            mqttServers.add(createMqttServer(options));
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

    private MqttServer createMqttServer(MqttServerOptions options) {
        return MqttServer.create(vertx, options)
                .endpointHandler(endpointHandler)
                .exceptionHandler(exceptionHandler);
    }

    private EndpointHandler createHandler() {
        AuthService authService = new AuthService();

        EndpointHandler handler = new EndpointHandler();
        handler.setConnectProcessor(new ConnectProcessor(authService));
        handler.setDisconnectProcessor(new DisconnectProcessor());
        handler.setPublishProcessor(new PublishProcessor(vertx, storage, subscriptionTrie));
        handler.setPubAckProcessor(new PubAckProcessor(vertx));
        handler.setPubRecProcessor(new PubRecProcessor(vertx));
        handler.setPubRelProcessor(new PubRelProcessor());
        handler.setPubCompProcessor(new PubCompProcessor(vertx));
        handler.setSubscribeProcessor(new SubscribeProcessor(subscriptionTrie));
        handler.setUnSubscribeProcessor(new UnSubscribeProcessor(subscriptionTrie));
        handler.setExceptionHandler(exceptionHandler);
        handler.setCloseHandler(closeHandler);

        return handler;
    }

}
