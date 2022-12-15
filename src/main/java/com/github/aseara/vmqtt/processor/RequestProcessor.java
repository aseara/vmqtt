package com.github.aseara.vmqtt.processor;

import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import io.vertx.core.Future;

public interface RequestProcessor<M> {

    /**
     * mqtt message processor
     * @param endpoint mqtt endpoint
     * @param message message
     */
    Future<MqttEndpoint> processRequest(MqttEndpoint endpoint, M message);

}
