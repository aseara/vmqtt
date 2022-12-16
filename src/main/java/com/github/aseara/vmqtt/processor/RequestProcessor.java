package com.github.aseara.vmqtt.processor;

import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

public abstract class RequestProcessor<M> {

    protected final Vertx vertx;

    protected RequestProcessor(Vertx vertx) {
        this.vertx = vertx;
    }

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
