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

package com.github.aseara.vmqtt.mqtt.messages.impl;

import com.github.aseara.vmqtt.mqtt.messages.MqttUnsubscribeMessage;
import io.netty.handler.codec.mqtt.MqttProperties;

import java.util.List;

/**
 * Represents an MQTT UNSUBSCRIBE message
 */
public class MqttUnsubscribeMessageImpl implements MqttUnsubscribeMessage {

  private final int messageId;
  private final List<String> topics;
  private final MqttProperties properties;

  /**
   * Constructor
   *
   * @param messageId message identifier
   * @param topics    list of topics to unsubscribe
   * @param properties UNSUBSCRIBE message properties
   */
  public MqttUnsubscribeMessageImpl(int messageId, List<String> topics, MqttProperties properties) {

    this.messageId = messageId;
    this.topics = topics;
    this.properties = properties;
  }

  public int messageId() {
    return this.messageId;
  }

  public List<String> topics() {
    return this.topics;
  }

  public MqttProperties properties() {
    return this.properties;
  }
}
