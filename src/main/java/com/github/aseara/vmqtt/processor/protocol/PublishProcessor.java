package com.github.aseara.vmqtt.processor.protocol;

import com.github.aseara.vmqtt.message.SubTrie;
import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import com.github.aseara.vmqtt.mqtt.messages.MqttPublishMessage;
import com.github.aseara.vmqtt.processor.RequestProcessor;
import com.github.aseara.vmqtt.storage.MemoryStorage;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Future;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;

@Slf4j
public class PublishProcessor extends RequestProcessor<MqttPublishMessage> {

    private final MemoryStorage storage;

    private final SubTrie subscriptions;

    public PublishProcessor(MemoryStorage storage, SubTrie subscriptions) {
        this.storage = storage;
        this.subscriptions = subscriptions;
    }

    @Override
    public Future<MqttEndpoint> processInternal(MqttEndpoint endpoint, MqttPublishMessage message) {
        log.info("Just received message [" + message.payload().toString(Charset.defaultCharset()) + "] with QoS [" + message.qosLevel() + "]");

        if (message.qosLevel() == MqttQoS.AT_LEAST_ONCE) {
            endpoint.publishAcknowledge(message.messageId());

        } else if (message.qosLevel() == MqttQoS.EXACTLY_ONCE) {
            endpoint.publishReceived(message.messageId());
        }

        return Future.succeededFuture().map(endpoint);
    }
}
