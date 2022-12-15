package com.github.aseara.vmqtt.handler;

import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import com.github.aseara.vmqtt.mqtt.MqttTopicSubscription;
import com.github.aseara.vmqtt.mqtt.messages.codes.MqttSubAckReasonCode;
import io.netty.handler.codec.mqtt.MqttProperties;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Handler;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class EndpointHandler implements Handler<MqttEndpoint> {

    @Override
    public void handle(MqttEndpoint endpoint) {
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
    }
}
