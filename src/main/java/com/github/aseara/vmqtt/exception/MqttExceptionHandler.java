package com.github.aseara.vmqtt.exception;

import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import io.vertx.core.Handler;
import io.vertx.core.VertxException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MqttExceptionHandler implements Handler<Throwable> {
    @Override
    public void handle(Throwable t) {
        log.error("mqtt connection process error: ", t);
        if (t instanceof MqttEndpointException ex) {
            MqttEndpoint ep = ex.getEndpoint();
            if (ep != null) {
                if (ep.isClosed()) {
                    ep.close();
                }
            }
            return;
        }
        // If t is not an endpoint exception, rethrow it.
        if (t instanceof RuntimeException ex) {
            throw ex;
        }
        throw new VertxException("exception can not be handled", t);
    }
}
