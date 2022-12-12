/*
 * Copyright 2016 Red Hat Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.aseara.vmqtt.mqtt.messages;

import com.github.aseara.vmqtt.mqtt.messages.impl.MqttPublishMessageImpl;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.mqtt.MqttProperties;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.buffer.Buffer;

/**
 * Represents an MQTT PUBLISH message
 */

public interface MqttPublishMessage extends MqttMessage {

  /**
   * Create a concrete instance of a Vert.x publish message
   *
   * @param messageId message identifier
   * @param qosLevel  quality of service level
   * @param isDup     if the message is a duplicate
   * @param isRetain  if the message needs to be retained
   * @param topicName topic on which the message was published
   * @param payload   payload message
   * @return Vert.x publish message
   */
  static MqttPublishMessage create(int messageId, MqttQoS qosLevel, boolean isDup, boolean isRetain, String topicName, ByteBuf payload) {

    return new MqttPublishMessageImpl(messageId, qosLevel, isDup, isRetain, topicName, payload, MqttProperties.NO_PROPERTIES);
  }
  /**
   * Create a concrete instance of a Vert.x publish message
   *
   * @param messageId  message identifier
   * @param qosLevel   quality of service level
   * @param isDup      if the message is a duplicate
   * @param isRetain   if the message needs to be retained
   * @param topicName  topic on which the message was published
   * @param payload    payload message
   * @param properties message properties
   * @return Vert.x publish message
   */
  static MqttPublishMessage create(int messageId, MqttQoS qosLevel, boolean isDup, boolean isRetain, String topicName, ByteBuf payload, MqttProperties properties) {

    return new MqttPublishMessageImpl(messageId, qosLevel, isDup, isRetain, topicName, payload, properties);
  }

  /**
   * @return  Quality of service level
   */
  MqttQoS qosLevel();

  /**
   * @return  If the message is a duplicate
   */
  boolean isDup();

  /**
   * @return  If the message needs to be retained
   */
  boolean isRetain();

  /**
   * @return  Topic on which the message was published
   */
  String topicName();

  /**
   * @return  Payload message
   */
  Buffer payload();

  /**
   * @return MQTT properties
   */
  MqttProperties properties();
}
