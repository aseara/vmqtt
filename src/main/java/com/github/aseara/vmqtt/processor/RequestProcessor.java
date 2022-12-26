package com.github.aseara.vmqtt.processor;

import com.github.aseara.vmqtt.exception.MqttEndpointException;
import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import com.github.aseara.vmqtt.session.SessionStore;
import com.github.aseara.vmqtt.verticle.MqttVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import org.slf4j.Logger;

public abstract class RequestProcessor<M> {

    protected final Vertx vertx;

    protected final MqttVerticle verticle;

    protected final SessionStore sessionStore;

    public RequestProcessor(MqttVerticle verticle) {
        this.verticle = verticle;
        this.vertx = verticle.getVertx();
        this.sessionStore = verticle.getSessionStore();
    }

    public final void processMessage(MqttEndpoint endpoint, M message, Handler<AsyncResult<MqttEndpoint>> handler) {
        Future<MqttEndpoint> fut;
        try {
            fut = processInternal(endpoint, message);
            sessionStore.touch(endpoint.clientIdentifier());
        } catch (MqttEndpointException e) {
            fut = Future.failedFuture(e);
        } catch (Throwable t) {
            fut = Future.failedFuture(new MqttEndpointException(endpoint, "message process error:", t));
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
