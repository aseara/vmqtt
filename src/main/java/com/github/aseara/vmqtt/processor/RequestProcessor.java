package com.github.aseara.vmqtt.processor;

import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

public abstract class RequestProcessor<M> {


    public final void processMessage(MqttEndpoint endpoint, M message, Handler<AsyncResult<MqttEndpoint>> handler) {
        Future<MqttEndpoint> fut;
        try {
            fut = processInternal(endpoint, message);
        } catch (Throwable t) {
            fut = Future.failedFuture(t);
        }
        fut.onComplete(handler);
    }

    /**
     * mqtt message processor
     * @param endpoint mqtt endpoint
     * @param message message
     */
    public final void processMessage(MqttEndpoint endpoint, M message) {
        processMessage(endpoint, message, ar -> {
            if (ar.failed()) {
                getLog().error("message process error: ", ar.cause());
                endpoint.close();
            }
        });
    }

    /**
     * mqtt message processor
     * @param endpoint mqtt endpoint
     * @param message message
     */
    protected abstract Future<MqttEndpoint> processInternal(MqttEndpoint endpoint, M message);

    protected abstract Logger getLog();
}
