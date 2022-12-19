package com.github.aseara.vmqtt.processor;

import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import io.vertx.core.Future;

public abstract class RequestProcessor<M> {

    /**
     * mqtt message processor
     * @param endpoint mqtt endpoint
     * @param message message
     */
    public final Future<MqttEndpoint> processMessage(MqttEndpoint endpoint, M message) {
        try {
            return processInternal(endpoint, message);
        } catch (Throwable t) {
            return Future.failedFuture(t);
        }
    }

    /**
     * mqtt message processor
     * @param endpoint mqtt endpoint
     * @param message message
     */
    protected abstract Future<MqttEndpoint> processInternal(MqttEndpoint endpoint, M message);

}
