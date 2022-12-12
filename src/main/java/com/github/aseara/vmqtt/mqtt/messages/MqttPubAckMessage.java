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

import com.github.aseara.vmqtt.mqtt.messages.codes.MqttPubAckReasonCode;
import com.github.aseara.vmqtt.mqtt.messages.impl.MqttPubAckMessageImpl;
import io.netty.handler.codec.mqtt.MqttProperties;

/**
 * Represents an MQTT PUBACK message
 */

public interface MqttPubAckMessage {

  /**
   * Create a concrete instance of a Vert.x puback message
   *
   * @param messageId message Id
   * @param code  return code from the puback
   * @param properties MQTT properties of the puback message
   * @return
   */
  static MqttPubAckMessage create(int messageId, MqttPubAckReasonCode code, MqttProperties properties) {
    return new MqttPubAckMessageImpl(messageId, code, properties);
  }

  int messageId();

  /**
   * @return  reason code from the puback request
   */
  MqttPubAckReasonCode code();

  /**
   * @return MQTT properties
   */
  MqttProperties properties();
}
