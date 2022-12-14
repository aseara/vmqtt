package com.github.aseara.vmqtt.verticle;

import com.github.aseara.vmqtt.conf.MqttConfig;
import com.github.aseara.vmqtt.mqtt.MqttServer;
import com.github.aseara.vmqtt.mqtt.MqttServerOptions;
import com.github.aseara.vmqtt.mqtt.MqttTopicSubscription;
import com.github.aseara.vmqtt.mqtt.messages.codes.MqttSubAckReasonCode;
import io.netty.handler.codec.mqtt.MqttProperties;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MqttVerticle extends AbstractVerticle {

    private final MqttConfig config;

    private List<MqttServer> mqttServers;

    public MqttVerticle(MqttConfig config) {
        this.config = config;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void start(Promise<Void> startPromise) {
        mqttServers = new ArrayList<>();
        int serverNum = Runtime.getRuntime().availableProcessors() * 2;
        for (int i = 0; i < serverNum; i++) {
            //  deploy more instances of the MQTT server to use more cores.
            mqttServers.add(createMqttServer());
        }

        List<Future> startFutures = new ArrayList<>();
        mqttServers.forEach(s -> startFutures.add(s.listen()));

        CompositeFuture.all(startFutures).onComplete(ar -> {
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
        MqttServer mqttServer = MqttServer.create(vertx, options);

        mqttServer.endpointHandler(endpoint -> {
            // shows main connect info
            log.info("MQTT client [" + endpoint.clientIdentifier() + "] request to connect, clean session = " + endpoint.isCleanSession());

            if (endpoint.auth() != null) {
                log.info("[username = " + endpoint.auth().getUsername() + ", password = " + endpoint.auth().getPassword() + "]");
            }
            log.info("[properties = " + endpoint.connectProperties() + "]");
            if (endpoint.will() != null) {
                System.out.println("[will topic = " + endpoint.will().getWillTopic() + " msg = " + new String(endpoint.will().getWillMessageBytes()) +
                        " QoS = " + endpoint.will().getWillQos() + " isRetain = " + endpoint.will().isWillRetain() + "]");
            }

            log.info("[keep alive timeout = " + endpoint.keepAliveTimeSeconds() + "]");

            // accept connection from the remote client
            endpoint.accept(false);


            endpoint.subscribeHandler(subscribe -> {
                List<MqttSubAckReasonCode> reasonCodes = new ArrayList<>();
                for (MqttTopicSubscription s: subscribe.topicSubscriptions()) {
                    System.out.println("Subscription for " + s.topicName() + " with QoS " + s.qualityOfService());
                    reasonCodes.add(MqttSubAckReasonCode.qosGranted(s.qualityOfService()));
                }
                // ack the subscriptions request
                endpoint.subscribeAcknowledge(subscribe.messageId(), reasonCodes, MqttProperties.NO_PROPERTIES);

            });

            endpoint.unsubscribeHandler(unsubscribe -> {

                for (String t: unsubscribe.topics()) {
                    System.out.println("Unsubscription for " + t);
                }
                // ack the subscriptions request
                endpoint.unsubscribeAcknowledge(unsubscribe.messageId());
            });

            endpoint.publishHandler(message -> {

                log.info("Just received message [" + message.payload().toString(Charset.defaultCharset()) + "] with QoS [" + message.qosLevel() + "]");

                if (message.qosLevel() == MqttQoS.AT_LEAST_ONCE) {
                    endpoint.publishAcknowledge(message.messageId());
                } else if (message.qosLevel() == MqttQoS.EXACTLY_ONCE) {
                    endpoint.publishReceived(message.messageId());
                }

            }).publishReleaseHandler(endpoint::publishComplete);

        });

        return mqttServer;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void stop(Promise<Void> stopPromise) {
        if (mqttServers == null || mqttServers.size() == 0) {
            stopPromise.complete();
            return;
        }

        List<Future> close = new ArrayList<>();
        mqttServers.forEach(s -> close.add(s.close()));

        CompositeFuture.all(close).onComplete(ar -> {
            if (ar.succeeded()) {
                stopPromise.complete();
            } else {
                stopPromise.fail(ar.cause());
            }
        });
    }
}
