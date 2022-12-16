package com.github.aseara.vmqtt.processor.protocol;

import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import com.github.aseara.vmqtt.mqtt.messages.MqttUnsubscribeMessage;
import com.github.aseara.vmqtt.processor.RequestProcessor;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

public class UnSubscribeProcessor extends RequestProcessor<MqttUnsubscribeMessage> {

    public UnSubscribeProcessor(Vertx vertx) {
        super(vertx);
    }

    @Override
    public Future<MqttEndpoint> processInternal(MqttEndpoint endpoint, MqttUnsubscribeMessage unsubscribe) {
        for (String t: unsubscribe.topics()) {
            System.out.println("Unsubscription for " + t);
        }
        // ack the subscriptions request
        endpoint.unsubscribeAcknowledge(unsubscribe.messageId());
        return null;
    }
}
