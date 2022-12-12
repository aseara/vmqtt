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

import com.github.aseara.vmqtt.mqtt.messages.impl.MqttConnAckMessageImpl;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.netty.handler.codec.mqtt.MqttProperties;

/**
 * Represents an MQTT CONNACK message
 */

public interface MqttConnAckMessage {

  /**
   * Create a concrete instance of a Vert.x connack message
   *
   * @param code  return code from the connection request
   * @param isSessionPresent  is an old session is present
   * @return
   */
  static MqttConnAckMessage create(MqttConnectReturnCode code, boolean isSessionPresent) {
    return new MqttConnAckMessageImpl(code, isSessionPresent);
  }

  /**
   * Create a concrete instance of a Vert.x connack message
   *
   * @param code  return code from the connection request
   * @param isSessionPresent  is an old session is present
   * @param properties MQTT properties
   * @return
   */
  static MqttConnAckMessage create(MqttConnectReturnCode code, boolean isSessionPresent, MqttProperties properties) {
    return new MqttConnAckMessageImpl(code, isSessionPresent, properties);
  }

  /**
   * @return  return code from the connection request
   */
  MqttConnectReturnCode code();

  /**
   * @return  is an old session is present
   */
  boolean isSessionPresent();

  MqttProperties properties();
}
