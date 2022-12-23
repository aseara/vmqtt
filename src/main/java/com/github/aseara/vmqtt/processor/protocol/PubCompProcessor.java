package com.github.aseara.vmqtt.processor.protocol;

import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import com.github.aseara.vmqtt.mqtt.messages.MqttPubCompMessage;
import com.github.aseara.vmqtt.processor.RequestProcessor;
import io.vertx.core.Future;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

@Slf4j
public class PubCompProcessor extends RequestProcessor<MqttPubCompMessage> {

    @Override
    public Future<MqttEndpoint> processInternal(MqttEndpoint endpoint, MqttPubCompMessage msg) {
        // TODO delete pubrel message in session
        return Future.succeededFuture(endpoint);
    }

    @Override
    protected Logger getLog() {
        return log;
    }
}
