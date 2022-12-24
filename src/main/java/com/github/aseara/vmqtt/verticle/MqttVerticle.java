package com.github.aseara.vmqtt.verticle;

import com.github.aseara.vmqtt.auth.AuthService;
import com.github.aseara.vmqtt.common.FutureUtil;
import com.github.aseara.vmqtt.conf.MqttConfig;
import com.github.aseara.vmqtt.exception.MqttExceptionHandler;
import com.github.aseara.vmqtt.handler.CloseHandler;
import com.github.aseara.vmqtt.handler.EndpointHandler;
import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import com.github.aseara.vmqtt.mqtt.MqttServer;
import com.github.aseara.vmqtt.mqtt.MqttServerOptions;
import com.github.aseara.vmqtt.processor.protocol.*;
import com.github.aseara.vmqtt.retain.RetainStorage;
import com.github.aseara.vmqtt.subscribe.SubscriptionTrie;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MqttVerticle extends AbstractVerticle {

    private final MqttConfig config;

    private final SubscriptionTrie subscriptionTrie;

    private final RetainStorage retainStorage;

    private EndpointHandler endpointHandler;

    private final MqttExceptionHandler exceptionHandler = new MqttExceptionHandler();

    private final CloseHandler closeHandler = new CloseHandler();

    private final List<MqttServer> mqttServers = new ArrayList<>();

    private final Cache<String, MqttEndpoint> endpointCache =
            CacheBuilder.newBuilder().weakValues().build();

    public MqttVerticle(MqttConfig config) {
        this.config = config;
        this.subscriptionTrie = new SubscriptionTrie();
        this.retainStorage = new RetainStorage();
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
        handler.setMqttVerticle(this);
        handler.setConnectProcessor(new ConnectProcessor(authService));
        handler.setDisconnectProcessor(new DisconnectProcessor());
        handler.setPublishProcessor(new PublishProcessor(this, retainStorage, subscriptionTrie));
        handler.setPubAckProcessor(new PubAckProcessor());
        handler.setPubRecProcessor(new PubRecProcessor());
        handler.setPubRelProcessor(new PubRelProcessor());
        handler.setPubCompProcessor(new PubCompProcessor());
        handler.setSubscribeProcessor(new SubscribeProcessor(this, subscriptionTrie, retainStorage));
        handler.setUnSubscribeProcessor(new UnSubscribeProcessor(subscriptionTrie));
        handler.setExceptionHandler(exceptionHandler);
        handler.setCloseHandler(closeHandler);

        return handler;
    }

    public void cacheEndpoint(MqttEndpoint endpoint) {
        endpointCache.put(endpoint.clientIdentifier(), endpoint);
    }

    public MqttEndpoint getEndpoint(String clientId) {
        return endpointCache.getIfPresent(clientId);
    }

    public void publish(MqttEndpoint sub, String topic, Buffer buf, MqttQoS qos, boolean retain) {
        int messageId = sub.nextMessageId();
        sub.publish(topic, buf, qos, false, retain, messageId).onComplete(ar -> {
            if (ar.succeeded()) {
                log.info("send message success");
            } else {
                log.error("send message failed: ", ar.cause());
            }
        });
        if (qos.value() > MqttQoS.AT_MOST_ONCE.value()) {
            // TODO need store publish message to sub session
            log.info("store message to sub session");
        }
    }



}
