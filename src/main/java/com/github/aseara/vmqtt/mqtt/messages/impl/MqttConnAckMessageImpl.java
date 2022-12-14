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

import com.github.aseara.vmqtt.mqtt.messages.MqttConnAckMessage;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.netty.handler.codec.mqtt.MqttProperties;

/**
 * Represents an MQTT CONNACK message
 */
public class MqttConnAckMessageImpl implements MqttConnAckMessage {

  private final MqttConnectReturnCode code;
  private final boolean isSessionPresent;
  private final MqttProperties properties;

  /**
   * Constructor
   *
   * @param code  return code from the connection request
   * @param isSessionPresent  is an old session is present
   */
  public MqttConnAckMessageImpl(MqttConnectReturnCode code, boolean isSessionPresent) {
    this(code, isSessionPresent, MqttProperties.NO_PROPERTIES);
  }

  /**
   * Constructor
   *
   * @param code  return code from the connection request
   * @param isSessionPresent  is an old session is present
   * @param properties MQTT properties
   */
  public MqttConnAckMessageImpl(MqttConnectReturnCode code, boolean isSessionPresent, MqttProperties properties) {
    this.code = code;
    this.isSessionPresent = isSessionPresent;
    this.properties = properties;
  }

  public MqttConnectReturnCode code() {
    return this.code;
  }

  public boolean isSessionPresent() {
    return this.isSessionPresent;
  }

  public MqttProperties properties() {
    return this.properties;
  }
}
