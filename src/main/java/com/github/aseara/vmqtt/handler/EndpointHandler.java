package com.github.aseara.vmqtt.handler;

import com.github.aseara.vmqtt.exception.MqttExceptionHandler;
import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import com.github.aseara.vmqtt.processor.protocol.ConnectProcessor;
import com.github.aseara.vmqtt.processor.protocol.DisconnectProcessor;
import com.github.aseara.vmqtt.processor.protocol.PubAckProcessor;
import com.github.aseara.vmqtt.processor.protocol.PubCompProcessor;
import com.github.aseara.vmqtt.processor.protocol.PubRecProcessor;
import com.github.aseara.vmqtt.processor.protocol.PubRelProcessor;
import com.github.aseara.vmqtt.processor.protocol.PublishProcessor;
import com.github.aseara.vmqtt.processor.protocol.SubscribeProcessor;
import com.github.aseara.vmqtt.processor.protocol.UnSubscribeProcessor;
import io.vertx.core.Handler;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import static io.netty.handler.codec.mqtt.MqttConnectReturnCode.CONNECTION_REFUSED_SERVER_UNAVAILABLE;

@Slf4j
@Setter
public class EndpointHandler implements Handler<MqttEndpoint> {

    private ConnectProcessor connectProcessor;

    private DisconnectProcessor disconnectProcessor;

    private PublishProcessor publishProcessor;

    private PubAckProcessor pubAckProcessor;

    private PubRecProcessor pubRecProcessor;

    private PubRelProcessor pubRelProcessor;

    private PubCompProcessor pubCompProcessor;

    private SubscribeProcessor subscribeProcessor;

    private UnSubscribeProcessor unSubscribeProcessor;

    private MqttExceptionHandler exceptionHandler;

    private CloseHandler closeHandler;

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
            // add message process after connect been accepted
            endpoint.closeHandler(v -> closeHandler.handle(endpoint))
                    .publishHandler(m -> publishProcessor.processMessage(endpoint, m))
                    .publishAcknowledgeMessageHandler(m -> pubAckProcessor.processMessage(endpoint, m))
                    .publishReceivedMessageHandler(m -> pubRecProcessor.processMessage(endpoint, m))
                    .publishReleaseMessageHandler(m -> pubRelProcessor.processMessage(endpoint, m))
                    .publishCompletionMessageHandler(m -> pubCompProcessor.processMessage(endpoint, m))
                    .subscribeHandler(m -> subscribeProcessor.processMessage(endpoint, m))
                    .unsubscribeHandler(m -> unSubscribeProcessor.processMessage(endpoint, m))
                    .disconnectMessageHandler(m -> disconnectProcessor.processMessage(endpoint, m))
                    .exceptionHandler(exceptionHandler);
        });
    }
}
