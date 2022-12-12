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

import com.github.aseara.vmqtt.mqtt.messages.impl.MqttUnsubscribeMessageImpl;
import io.netty.handler.codec.mqtt.MqttProperties;

import java.util.List;

/**
 * Represents an MQTT UNSUBSCRIBE message
 */

public interface MqttUnsubscribeMessage extends MqttMessage {

  /**
   * Create a concrete instance of a Vert.x unsubscribe message
   *
   * @param messageId message identifier
   * @param topics    list of topics to unsubscribe
   */
  static MqttUnsubscribeMessage create(int messageId, List<String> topics) {

    return new MqttUnsubscribeMessageImpl(messageId, topics, MqttProperties.NO_PROPERTIES);
  }

  /**
   * Create a concrete instance of a Vert.x unsubscribe message
   *
   * @param messageId message identifier
   * @param topics    list of topics to unsubscribe
   * @param properties UNSUBSCRIBE message properties
   */
  static MqttUnsubscribeMessage create(int messageId, List<String> topics, MqttProperties properties) {

    return new MqttUnsubscribeMessageImpl(messageId, topics, properties);
  }


  /**
   * @return  List of topics to unsubscribe
   */
  List<String> topics();

  /**
   * @return MQTT properties
   */
  MqttProperties properties();
}
