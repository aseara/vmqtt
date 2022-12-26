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
package com.github.aseara.vmqtt.mqtt;

import com.github.aseara.vmqtt.mqtt.messages.MqttDisconnectMessage;
import com.github.aseara.vmqtt.mqtt.messages.MqttPubAckMessage;
import com.github.aseara.vmqtt.mqtt.messages.MqttPubCompMessage;
import com.github.aseara.vmqtt.mqtt.messages.MqttPubRecMessage;
import com.github.aseara.vmqtt.mqtt.messages.MqttPubRelMessage;
import com.github.aseara.vmqtt.mqtt.messages.MqttPublishMessage;
import com.github.aseara.vmqtt.mqtt.messages.MqttSubscribeMessage;
import com.github.aseara.vmqtt.mqtt.messages.MqttUnsubscribeMessage;
import com.github.aseara.vmqtt.mqtt.messages.codes.MqttDisconnectReasonCode;
import com.github.aseara.vmqtt.mqtt.messages.codes.MqttPubAckReasonCode;
import com.github.aseara.vmqtt.mqtt.messages.codes.MqttPubCompReasonCode;
import com.github.aseara.vmqtt.mqtt.messages.codes.MqttPubRecReasonCode;
import com.github.aseara.vmqtt.mqtt.messages.codes.MqttPubRelReasonCode;
import com.github.aseara.vmqtt.mqtt.messages.codes.MqttSubAckReasonCode;
import com.github.aseara.vmqtt.mqtt.messages.codes.MqttUnsubAckReasonCode;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.netty.handler.codec.mqtt.MqttProperties;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.SocketAddress;

import javax.net.ssl.SSLSession;
import java.util.List;

/**
 * Represents an MQTT endpoint for point-to-point communication with the remote MQTT client
 */
public interface MqttEndpoint {

  /**
   * Close the endpoint, so the connection with remote MQTT client
   */
  void close();

  /**
   * @return the remote address for this socket
   */
  SocketAddress remoteAddress();

  /**
   * @return the local address for this socket
   */
  SocketAddress localAddress();

  /**
   * @return true if this {@link MqttEndpoint} is encrypted via SSL/TLS.
   */
  boolean isSsl();

  /**
   *  Returns the HTTP headers sent by the client when a WebSocket transport is used otherwise {@code null}
   *
   * @return the HTTP headers
   */
  MultiMap httpHeaders();

  /**
   *  Returns the HTTP request URI sent by the client when a WebSocket transport is used otherwise {@code null}
   *
   * @return the HTTP request uri
   */
  String httpRequestURI();

  /**
   * @return SSLSession associated with the underlying socket. Returns null if connection is
   *         not SSL.
   * @see SSLSession
   */
  SSLSession sslSession();

  /**
   * @return the client identifier as provided by the remote MQTT client
   */
  String clientIdentifier();

  /**
   * @return the Authentication information as provided by the remote MQTT client
   */
  MqttAuth auth();

  /**
   * @return the Will information as provided by the remote MQTT client
   */
  MqttWill will();

  /**
   * @return the protocol version required by the remote MQTT client
   */
  int protocolVersion();

  /**
   * @return the protocol name provided by the remote MQTT client
   */
  String protocolName();

  /**
   * @return true when clean session is requested by the remote MQTT client
   */
  boolean isCleanSession();

  /**
   * @return the keep alive timeout (in seconds) specified by the remote MQTT client
   */
  int keepAliveTimeSeconds();

  /**
   * @return the message identifier used for last published message
   */
  int lastMessageId();

  /**
   * Enable/disable subscription/unsubscription requests auto acknowledge
   *
   * @param isSubscriptionAutoAck auto acknowledge status
   */
  void subscriptionAutoAck(boolean isSubscriptionAutoAck);

  /**
   * @return true when auto acknowledge status for subscription/unsubscription requests
   */
  boolean isSubscriptionAutoAck();

  /**
   * Enable/disable publishing (in/out) auto acknowledge
   *
   * @param isPublishAutoAck auto acknowledge status
   * @return  a reference to this, so the API can be used fluently
   */
  MqttEndpoint publishAutoAck(boolean isPublishAutoAck);

  /**
   * @return  auto acknowledge status for publishing (in/out)
   */
  boolean isPublishAutoAck();

  /**
   * Enable/disable auto keep alive (sending ping response)
   *
   * @param isAutoKeepAlive auto keep alive
   * @return  a reference to this, so the API can be used fluently
   */
  MqttEndpoint autoKeepAlive(boolean isAutoKeepAlive);

  /**
   * @return the auto keep alive status (sending ping response)
   */
  boolean isAutoKeepAlive();

  /**
   * @return  if the connection between remote client and local endpoint is established/open
   */
  boolean isConnected();

  /**
   * @return if the close method has been invoked
   */
  boolean isClosed();


  /**
   * @return MQTT properties of the CONNECT message
   */
  MqttProperties connectProperties();

  /**
   * Set client identifier if not provided by the remote MQTT client (zero-bytes)
   *
   * @param clientIdentifier the client identifier
   * @return  a reference to this, so the API can be used fluently
   */
  MqttEndpoint setClientIdentifier(String clientIdentifier);

  /**
   * Set a disconnect handler on the MQTT endpoint. This handler is called when a DISCONNECT
   * message is received by the remote MQTT client
   *
   * @param handler the handler
   * @return a reference to this, so the API can be used fluently
   */
  MqttEndpoint disconnectHandler(Handler<Void> handler);

  /**
   * Set a disconnect handler on the MQTT endpoint. This handler is called when a DISCONNECT
   * message is received by the remote MQTT client
   *
   * @param handler the handler
   * @return a reference to this, so the API can be used fluently
   */
  MqttEndpoint disconnectMessageHandler(Handler<MqttDisconnectMessage> handler);

  /**
   * Set a subscribe handler on the MQTT endpoint. This handler is called when a SUBSCRIBE
   * message is received by the remote MQTT client
   *
   * @param handler the handler
   * @return a reference to this, so the API can be used fluently
   */
  MqttEndpoint subscribeHandler(Handler<MqttSubscribeMessage> handler);

  /**
   * Set a unsubscribe handler on the MQTT endpoint. This handler is called when a UNSUBSCRIBE
   * message is received by the remote MQTT client
   *
   * @param handler the handler
   * @return a reference to this, so the API can be used fluently
   */
  MqttEndpoint unsubscribeHandler(Handler<MqttUnsubscribeMessage> handler);

  /**
   * Set the publish handler on the MQTT endpoint. This handler is called when a PUBLISH
   * message is received by the remote MQTT client
   *
   * @param handler the handler
   * @return a reference to this, so the API can be used fluently
   */
  MqttEndpoint publishHandler(Handler<MqttPublishMessage> handler);

  /**
   * Set the puback handler on the MQTT endpoint. This handler is called when a PUBACK
   * message is received by the remote MQTT client
   *
   * @param handler the handler
   * @return a reference to this, so the API can be used fluently
   */
  MqttEndpoint publishAcknowledgeHandler(Handler<Integer> handler);

  /**
   * Set the puback handler on the MQTT endpoint. This handler is called when a PUBACK
   * message is received by the remote MQTT client
   *
   * @param handler the handler
   * @return a reference to this, so the API can be used fluently
   */
  MqttEndpoint publishAcknowledgeMessageHandler(Handler<MqttPubAckMessage> handler);

  /**
   * Set the pubrec handler on the MQTT endpoint. This handler is called when a PUBREC
   * message is received by the remote MQTT client
   *
   * @param handler the handler
   * @return a reference to this, so the API can be used fluently
   */
  MqttEndpoint publishReceivedHandler(Handler<Integer> handler);

  /**
   * Set the pubrec handler on the MQTT endpoint. This handler is called when a PUBREC
   * message is received by the remote MQTT client
   *
   * @param handler the handler
   * @return a reference to this, so the API can be used fluently
   */
  MqttEndpoint publishReceivedMessageHandler(Handler<MqttPubRecMessage> handler);

  /**
   * Set the pubrel handler on the MQTT endpoint. This handler is called when a PUBREL
   * message is received by the remote MQTT client
   *
   * @param handler the handler
   * @return a reference to this, so the API can be used fluently
   */
  MqttEndpoint publishReleaseHandler(Handler<Integer> handler);

  /**
   * Set the pubrel handler on the MQTT endpoint. This handler is called when a PUBREL
   * message is received by the remote MQTT client
   *
   * @param handler the handler
   * @return a reference to this, so the API can be used fluently
   */
  MqttEndpoint publishReleaseMessageHandler(Handler<MqttPubRelMessage> handler);


  /**
   * Set the pubcomp handler on the MQTT endpoint. This handler is called when a PUBCOMP
   * message is received by the remote MQTT client
   *
   * @param handler the handler
   * @return a reference to this, so the API can be used fluently
   */
  MqttEndpoint publishCompletionHandler(Handler<Integer> handler);

  /**
   * Set the pubcomp handler on the MQTT endpoint. This handler is called when a PUBCOMP
   * message is received by the remote MQTT client
   *
   * @param handler the handler
   * @return a reference to this, so the API can be used fluently
   */
  MqttEndpoint publishCompletionMessageHandler(Handler<MqttPubCompMessage> handler);

  /**
   * Set the pingreq handler on the MQTT endpoint. This handler is called when a PINGREQ
   * message is received by the remote MQTT client. In any case the endpoint sends the
   * PINGRESP internally after executing this handler.
   *
   * @param handler the handler
   * @return a reference to this, so the API can be used fluently
   */
  MqttEndpoint pingHandler(Handler<Void> handler);

  /**
   * Set a close handler. This will be called when the MQTT endpoint is closed
   *
   * @param handler the handler
   * @return a reference to this, so the API can be used fluently
   */
  MqttEndpoint closeHandler(Handler<Void> handler);

  /**
   * Set an exception handler. This will be called when an error at protocol level happens
   *
   * @param handler the handler
   * @return  a reference to this, so the API can be used fluently
   */
  MqttEndpoint exceptionHandler(Handler<Throwable> handler);

  /**
   * Like {@link #accept(boolean)} with no session is present.
   */
  MqttEndpoint accept();

  /**
   * Sends the CONNACK message to the remote MQTT client with "connection accepted"
   * return code. See {@link #reject(MqttConnectReturnCode)} for refusing connection
   *
   * @param sessionPresent  if a previous session is present
   * @return  a reference to this, so the API can be used fluently
     */

  MqttEndpoint accept(boolean sessionPresent);

  /**
   * Sends the CONNACK message to the remote MQTT client with "connection accepted"
   * return code. See {@link #reject(MqttConnectReturnCode)} for refusing connection
   *
   * @param sessionPresent  if a previous session is present
   * @param properties CONNACK message properties (MQTT5)
   * @return  a reference to this, so the API can be used fluently
   */
  MqttEndpoint accept(boolean sessionPresent, MqttProperties properties);

  /**
   * Sends the CONNACK message to the remote MQTT client rejecting the connection
   * request with specified return code. See {@link #accept(boolean)} for accepting connection
   *
   * @param returnCode  the connect return code
   * @return  a reference to this, so the API can be used fluently
     */
  MqttEndpoint reject(MqttConnectReturnCode returnCode);

  /**
   * Sends the CONNACK message to the remote MQTT client rejecting the connection
   * request with specified return code. See {@link #accept(boolean)} for accepting connection
   *
   * @param returnCode  the connect return code
   * @param properties CONNACK message properties
   * @return  a reference to this, so the API can be used fluently
   */
  MqttEndpoint reject(MqttConnectReturnCode returnCode, MqttProperties properties);

  /**
   * Sends the SUBACK message to the remote MQTT client
   *
   * @param subscribeMessageId identifier of the SUBSCRIBE message to acknowledge
   * @param grantedQoSLevels   granted QoS levels for the requested topics
   * @return a reference to this, so the API can be used fluently
   */
  MqttEndpoint subscribeAcknowledge(int subscribeMessageId, List<MqttQoS> grantedQoSLevels);

  /**
   * Sends the SUBACK message to the remote MQTT client
   *
   * @param subscribeMessageId identifier of the SUBSCRIBE message to acknowledge
   * @param reasonCodes reason codes
   * @param properties MQTT message properties
   * @return a reference to this, so the API can be used fluently
   */
  MqttEndpoint subscribeAcknowledge(int subscribeMessageId, List<MqttSubAckReasonCode> reasonCodes, MqttProperties properties);

  /**
   * Sends the UNSUBACK message to the remote MQTT client
   *
   * @param unsubscribeMessageId identifier of the UNSUBSCRIBE message to acknowledge
   * @return a reference to this, so the API can be used fluently
   */
  MqttEndpoint unsubscribeAcknowledge(int unsubscribeMessageId);

  /**
   * Sends the UNSUBACK message to the remote MQTT client
   *
   * @param unsubscribeMessageId identifier of the UNSUBSCRIBE message to acknowledge
   * @param reasonCodes reason codes
   * @param properties MQTT message properties
   * @return a reference to this, so the API can be used fluently
   */
  MqttEndpoint unsubscribeAcknowledge(int unsubscribeMessageId, List<MqttUnsubAckReasonCode> reasonCodes, MqttProperties properties);

  /**
   * Sends the PUBACK message to the remote MQTT client
   *
   * @param publishMessageId identifier of the PUBLISH message to acknowledge
   * @return a reference to this, so the API can be used fluently
   */
  MqttEndpoint publishAcknowledge(int publishMessageId);


  MqttEndpoint publishAcknowledge(int publishMessageId, MqttPubAckReasonCode reasonCode, MqttProperties properties);

  /**
   * Sends the PUBREC message to the remote MQTT client
   *
   * @param publishMessageId identifier of the PUBLISH message to acknowledge
   * @return a reference to this, so the API can be used fluently
   */
  MqttEndpoint publishReceived(int publishMessageId);

  /**
   * Sends the PUBREC message to the remote MQTT client
   *
   * @param publishMessageId identifier of the PUBLISH message to acknowledge
   * @param reasonCode code of the outcome
   * @param properties MQTT properties
   * @return a reference to this, so the API can be used fluently
   */
  MqttEndpoint publishReceived(int publishMessageId, MqttPubRecReasonCode reasonCode, MqttProperties properties);

  /**
   * Sends the PUBREL message to the remote MQTT client
   *
   * @param publishMessageId identifier of the PUBLISH message to acknowledge
   * @return a reference to this, so the API can be used fluently
   */
  MqttEndpoint publishRelease(int publishMessageId);

  /**
   * Sends the PUBREL message to the remote MQTT client
   *
   * @param publishMessageId identifier of the PUBLISH message to acknowledge
   * @param reasonCode reason code
   * @param properties MQTT message properties
   * @return a reference to this, so the API can be used fluently
   */
  MqttEndpoint publishRelease(int publishMessageId, MqttPubRelReasonCode reasonCode, MqttProperties properties);

  /**
   * Sends the PUBCOMP message to the remote MQTT client
   *
   * @param publishMessageId identifier of the PUBLISH message to acknowledge
   * @return a reference to this, so the API can be used fluently
   */
  MqttEndpoint publishComplete(int publishMessageId);

  /**
   * Sends the PUBCOMP message to the remote MQTT client
   *
   * @param publishMessageId identifier of the PUBLISH message to acknowledge
   * @param reasonCode reason code
   * @param properties MQTT message properties
   * @return a reference to this, so the API can be used fluently
   */
  MqttEndpoint publishComplete(int publishMessageId, MqttPubCompReasonCode reasonCode, MqttProperties properties);

  /**
   * Sends the PUBLISH message to the remote MQTT client
   *
   * @param topic    topic on which the message is published
   * @param payload  message payload
   * @param qosLevel quality of service level
   * @param isDup    if the message is a duplicate
   * @param isRetain if the message needs to be retained
   * @return a {@code Future} completed after PUBLISH packet sent with a packetId
   */
  Future<Integer> publish(String topic, Buffer payload, MqttQoS qosLevel, boolean isDup, boolean isRetain);

  /**
   * Sends the PUBLISH message to the remote MQTT server
   *
   * @param topic              topic on which the message is published
   * @param payload            message payload
   * @param qosLevel           QoS level
   * @param isDup              if the message is a duplicate
   * @param isRetain           if the message needs to be retained
   * @param publishSentHandler handler called after PUBLISH packet sent with a packetId
   * @return current MQTT client instance
   */
  MqttEndpoint publish(String topic, Buffer payload, MqttQoS qosLevel, boolean isDup, boolean isRetain, Handler<AsyncResult<Integer>> publishSentHandler);

  /**
   * Like {@link #publish(String, Buffer, MqttQoS, boolean, boolean, int, Handler)} but returns a {@code Future} of the asynchronous result
   */
  Future<Integer> publish(String topic, Buffer payload, MqttQoS qosLevel, boolean isDup, boolean isRetain, int messageId);

  /**
   * Like {@link #publish(String, Buffer, MqttQoS, boolean, boolean, int, MqttProperties, Handler)} but returns a {@code Future} of the asynchronous result
   */
  Future<Integer> publish(String topic, Buffer payload, MqttQoS qosLevel, boolean isDup, boolean isRetain, int messageId, MqttProperties properties);

  /**
   * Sends the PUBLISH message to the remote MQTT server explicitly specifying the messageId
   *
   * @param topic              topic on which the message is published
   * @param payload            message payload
   * @param qosLevel           QoS level
   * @param isDup              if the message is a duplicate
   * @param isRetain           if the message needs to be retained
   * @param messageId          message ID
   * @param publishSentHandler handler called after PUBLISH packet sent with a packetId
   * @return current MQTT client instance
   */
  MqttEndpoint publish(String topic, Buffer payload, MqttQoS qosLevel, boolean isDup, boolean isRetain, int messageId, Handler<AsyncResult<Integer>> publishSentHandler);

  /**
   * Sends the PUBLISH message to the remote MQTT server explicitly specifying the messageId
   *
   * @param topic              topic on which the message is published
   * @param payload            message payload
   * @param qosLevel           QoS level
   * @param isDup              if the message is a duplicate
   * @param isRetain           if the message needs to be retained
   * @param messageId          message ID
   * @param properties         PUBLISH message properties
   * @param publishSentHandler handler called after PUBLISH packet sent with a packetId
   * @return current MQTT client instance
   */
  MqttEndpoint publish(String topic,
                       Buffer payload,
                       MqttQoS qosLevel,
                       boolean isDup,
                       boolean isRetain,
                       int messageId,
                       MqttProperties properties,
                       Handler<AsyncResult<Integer>> publishSentHandler);

  /**
   * Sends the PINGRESP message to the remote MQTT client
   *
   * @return a reference to this, so the API can be used fluently
   */

  MqttEndpoint pong();


  /**
   * Sends the DISCONNECT message to the remote MQTT client
   *
   * @param code reason code
   * @param properties MQTT message properties
   * @return a reference to this, so the API can be used fluently
   */
  MqttEndpoint disconnect(MqttDisconnectReasonCode code, MqttProperties properties);

  /**
   * Update and return the next message identifier
   *
   * @return message identifier
   */
  public int nextMessageId();
}
