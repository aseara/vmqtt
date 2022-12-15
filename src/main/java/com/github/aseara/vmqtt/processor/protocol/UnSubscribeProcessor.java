package com.github.aseara.vmqtt.processor.protocol;

import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import com.github.aseara.vmqtt.mqtt.messages.MqttUnsubscribeMessage;
import com.github.aseara.vmqtt.processor.RequestProcessor;
import io.vertx.core.Future;

public class UnSubscribeProcessor implements RequestProcessor<MqttUnsubscribeMessage> {

    @Override
    public Future<MqttEndpoint> processRequest(MqttEndpoint endpoint, MqttUnsubscribeMessage unsubscribe) {
        for (String t: unsubscribe.topics()) {
            System.out.println("Unsubscription for " + t);
        }
        // ack the subscriptions request
        endpoint.unsubscribeAcknowledge(unsubscribe.messageId());
        return null;
    }
}
