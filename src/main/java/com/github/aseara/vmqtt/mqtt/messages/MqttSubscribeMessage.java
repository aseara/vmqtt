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

import com.github.aseara.vmqtt.mqtt.MqttTopicSubscription;
import com.github.aseara.vmqtt.mqtt.messages.impl.MqttSubscribeMessageImpl;
import io.netty.handler.codec.mqtt.MqttProperties;

import java.util.List;

/**
 * Represents an MQTT SUBSCRIBE message
 */

public interface MqttSubscribeMessage extends MqttMessage {

  /**
   * Create a concrete instance of a Vert.x subscribe message
   *
   * @param messageId          message identifier
   * @param topicSubscriptions list with topics and related quality of service levels (from Netty)
   * @return Vert.x subscribe message
   */
  static MqttSubscribeMessage create(int messageId, List<io.netty.handler.codec.mqtt.MqttTopicSubscription> topicSubscriptions) {

    return new MqttSubscribeMessageImpl(messageId, topicSubscriptions, MqttProperties.NO_PROPERTIES);
  }

  /**
   * Create a concrete instance of a Vert.x subscribe message
   *
   * @param messageId          message identifier
   * @param topicSubscriptions list with topics and related quality of service levels (from Netty)
   * @param properties MQTT message properties
   * @return Vert.x subscribe message
   */
  static MqttSubscribeMessage create(int messageId, List<io.netty.handler.codec.mqtt.MqttTopicSubscription> topicSubscriptions, MqttProperties properties) {
    return new MqttSubscribeMessageImpl(messageId, topicSubscriptions, properties);
  }


  /**
   * @return  List with topics and related quolity of service levels
   */
  List<MqttTopicSubscription> topicSubscriptions();

  /**
   * @return MQTT properties
   */
  MqttProperties properties();
}
