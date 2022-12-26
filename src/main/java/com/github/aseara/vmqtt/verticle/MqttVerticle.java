package com.github.aseara.vmqtt.verticle;

import com.github.aseara.vmqtt.auth.AuthService;
import com.github.aseara.vmqtt.common.FutureUtil;
import com.github.aseara.vmqtt.conf.MqttConfig;
import com.github.aseara.vmqtt.exception.MqttExceptionHandler;
import com.github.aseara.vmqtt.handler.CloseHandler;
import com.github.aseara.vmqtt.handler.EndpointHandler;
import com.github.aseara.vmqtt.mqtt.MqttServer;
import com.github.aseara.vmqtt.mqtt.MqttServerOptions;
import com.github.aseara.vmqtt.processor.protocol.*;
import com.github.aseara.vmqtt.retain.RetainStorage;
import com.github.aseara.vmqtt.service.PubService;
import com.github.aseara.vmqtt.session.SessionStore;
import com.github.aseara.vmqtt.subscribe.SubscriptionTrie;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
public class MqttVerticle extends AbstractVerticle {

    private final MqttConfig config;

    private final SubscriptionTrie subscriptionTrie;

    private final SessionStore sessionStore;

    private final RetainStorage retainStorage;

    private final AuthService authService = new AuthService();

    private final MqttExceptionHandler exceptionHandler = new MqttExceptionHandler();

    private EndpointHandler endpointHandler;

    private final CloseHandler closeHandler;

    private final PubService pubService;

    private final List<MqttServer> mqttServers = new ArrayList<>();

    public MqttVerticle(MqttConfig config) {
        this.config = config;
        this.subscriptionTrie = new SubscriptionTrie();
        this.retainStorage = new RetainStorage();
        this.sessionStore = new SessionStore(config.getMqtt().getSessionExpireSeconds());
        this.pubService = new PubService(this);
        this.closeHandler = new CloseHandler(this);
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
        EndpointHandler handler = new EndpointHandler();
        handler.setPubService(pubService);
        handler.setConnectProcessor(new ConnectProcessor(this));
        handler.setDisconnectProcessor(new DisconnectProcessor(this));
        handler.setPublishProcessor(new PublishProcessor(this));
        handler.setPubAckProcessor(new PubAckProcessor(this));
        handler.setPubRecProcessor(new PubRecProcessor(this));
        handler.setPubRelProcessor(new PubRelProcessor(this));
        handler.setPubCompProcessor(new PubCompProcessor(this));
        handler.setSubscribeProcessor(new SubscribeProcessor(this));
        handler.setUnSubscribeProcessor(new UnSubscribeProcessor(this));
        handler.setExceptionHandler(exceptionHandler);
        handler.setCloseHandler(closeHandler);

        return handler;
    }



}
