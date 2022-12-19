package com.github.aseara.vmqtt.processor.protocol;

import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import com.github.aseara.vmqtt.mqtt.MqttTopicSubscription;
import com.github.aseara.vmqtt.mqtt.messages.MqttSubscribeMessage;
import com.github.aseara.vmqtt.mqtt.messages.codes.MqttSubAckReasonCode;
import com.github.aseara.vmqtt.processor.RequestProcessor;
import io.netty.handler.codec.mqtt.MqttProperties;
import io.vertx.core.Future;

import java.util.ArrayList;
import java.util.List;

public class SubscribeProcessor extends RequestProcessor<MqttSubscribeMessage> {

    @Override
    public Future<MqttEndpoint> processInternal(MqttEndpoint endpoint, MqttSubscribeMessage subscribe) {
        List<MqttSubAckReasonCode> reasonCodes = new ArrayList<>();
        for (MqttTopicSubscription s: subscribe.topicSubscriptions()) {
            System.out.println("Subscription for " + s.topicName() + " with QoS " + s.qualityOfService());
            reasonCodes.add(MqttSubAckReasonCode.qosGranted(s.qualityOfService()));
        }
        // ack the subscriptions request
        endpoint.subscribeAcknowledge(subscribe.messageId(), reasonCodes, MqttProperties.NO_PROPERTIES);

        return Future.succeededFuture().map(endpoint);
    }
}
