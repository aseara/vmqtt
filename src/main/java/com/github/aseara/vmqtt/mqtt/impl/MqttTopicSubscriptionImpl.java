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

package com.github.aseara.vmqtt.mqtt.impl;

import com.github.aseara.vmqtt.mqtt.MqttTopicSubscription;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.codec.mqtt.MqttSubscriptionOption;

/**
 * Represents a subscription to a topic
 */
public class MqttTopicSubscriptionImpl implements MqttTopicSubscription {

  private final String topicName;
  private final MqttSubscriptionOption subscriptionOption;

  /**
   * Constructor
   *
   * @param topicName        topic name for the subscription
   * @param qualityOfService quality of service level
   */
  public MqttTopicSubscriptionImpl(String topicName, MqttQoS qualityOfService) {
    this(topicName, MqttSubscriptionOption.onlyFromQos(qualityOfService));
  }

  public MqttTopicSubscriptionImpl(String topicName, MqttSubscriptionOption subscriptionOption) {
    this.topicName = topicName;
    this.subscriptionOption = subscriptionOption;
  }


  public String topicName() {
    return this.topicName;
  }

  public MqttQoS qualityOfService() {
    return subscriptionOption.qos();
  }

  public MqttSubscriptionOption subscriptionOption() {
    return subscriptionOption;
  }
}
