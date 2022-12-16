package com.github.aseara.vmqtt.handler;

import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import com.github.aseara.vmqtt.processor.protocol.*;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;

import static io.netty.handler.codec.mqtt.MqttConnectReturnCode.CONNECTION_REFUSED_SERVER_UNAVAILABLE;

@Slf4j
public class EndpointHandler implements Handler<MqttEndpoint> {

    private final Vertx vertx;

    private final ConnectProcessor connectProcessor;

    private final DisconnectProcessor disconnectProcessor;

    private final PublishProcessor publishProcessor;

    private final PubAckProcessor pubAckProcessor;

    private final PubRecProcessor pubRecProcessor;

    private final PubRelProcessor pubRelProcessor;

    private final PubCompProcessor pubCompProcessor;

    private final SubscribeProcessor subscribeProcessor;

    private final UnSubscribeProcessor unSubscribeProcessor;

    public EndpointHandler(Vertx vertx, ConnectProcessor connectProcessor,
                           DisconnectProcessor disconnectProcessor,
                           PublishProcessor publishProcessor,
                           PubAckProcessor pubAckProcessor,
                           PubRecProcessor pubRecProcessor,
                           PubRelProcessor pubRelProcessor,
                           PubCompProcessor pubCompProcessor,
                           SubscribeProcessor subscribeProcessor,
                           UnSubscribeProcessor unSubscribeProcessor) {
        this.vertx = vertx;
        this.connectProcessor = connectProcessor;
        this.disconnectProcessor = disconnectProcessor;
        this.publishProcessor = publishProcessor;
        this.pubAckProcessor = pubAckProcessor;
        this.pubRecProcessor = pubRecProcessor;
        this.pubRelProcessor = pubRelProcessor;
        this.pubCompProcessor = pubCompProcessor;
        this.subscribeProcessor = subscribeProcessor;
        this.unSubscribeProcessor = unSubscribeProcessor;
    }

    @Override
    public void handle(MqttEndpoint endpoint) {
        connectProcessor.processMessage(endpoint, endpoint).onFailure(t -> {
            log.error("error occurred in processing connect: ", t);
            if (!endpoint.isClosed()) {
                endpoint.reject(CONNECTION_REFUSED_SERVER_UNAVAILABLE);
            }
        }).onSuccess(e -> {
            if (!e.isConnected()) {
                return;
            }
            vertx.getOrCreateContext().put("endpoint", endpoint);
            // add message process after connect been accepted
            endpoint.publishHandler(m -> publishProcessor.processMessage(endpoint, m))
                    .publishAcknowledgeMessageHandler(m -> pubAckProcessor.processMessage(endpoint, m))
                    .publishReceivedMessageHandler(m -> pubRecProcessor.processMessage(endpoint, m))
                    .publishReleaseMessageHandler(m -> pubRelProcessor.processMessage(endpoint, m))
                    .publishCompletionMessageHandler(m -> pubCompProcessor.processMessage(endpoint, m))
                    .subscribeHandler(m -> subscribeProcessor.processMessage(endpoint, m))
                    .unsubscribeHandler(m -> unSubscribeProcessor.processMessage(endpoint, m))
                    .disconnectMessageHandler(m -> disconnectProcessor.processMessage(endpoint, m));
        });
    }
}
