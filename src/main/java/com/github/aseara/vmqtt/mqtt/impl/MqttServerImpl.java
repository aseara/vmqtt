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

import com.github.aseara.vmqtt.mqtt.MqttEndpoint;
import com.github.aseara.vmqtt.mqtt.MqttServer;
import com.github.aseara.vmqtt.mqtt.MqttServerOptions;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.ReferenceCountUtil;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.impl.NetSocketInternal;


/**
 * An MQTT server implementation
 */
public class MqttServerImpl implements MqttServer {

  private final VertxInternal vertx;
  private final NetServer server;
  private Handler<MqttEndpoint> endpointHandler;
  private Handler<Throwable> exceptionHandler;

  private final MqttServerOptions options;

  public MqttServerImpl(Vertx vertx, MqttServerOptions options) {
    this.vertx = (VertxInternal) vertx;
    this.server = vertx.createNetServer(options);
    this.options = options;
  }

  @Override
  public Future<MqttServer> listen() {
    return listen(this.options.getPort());
  }

  @Override
  public Future<MqttServer> listen(int port, String host) {
    Handler<MqttEndpoint> h1 = endpointHandler;
    Handler<Throwable> h2 = exceptionHandler;
    if (h1 == null) {
      return vertx.getOrCreateContext().failedFuture(new IllegalStateException("Please set handler before server is listening"));
    }
    server.connectHandler(so -> {
      NetSocketInternal soi = (NetSocketInternal) so;
      ChannelPipeline pipeline = soi.channelHandlerContext().pipeline();

      initChannel(pipeline);
      MqttServerConnection conn = new MqttServerConnection(soi, h1, h2, options);

      soi.eventHandler(ReferenceCountUtil::release);

      soi.messageHandler(msg -> {
        synchronized (conn) {
          conn.handleMessage(msg);
        }
      });
    });
    return server.listen(port, host).map(this);
  }

  @Override
  public Future<MqttServer> listen(int port) {
    return listen(port, this.options.getHost());
  }

  @Override
  public MqttServer listen(int port, Handler<AsyncResult<MqttServer>> listenHandler) {
    return listen(port, this.options.getHost(), listenHandler);
  }

  @Override
  public MqttServer listen(Handler<AsyncResult<MqttServer>> listenHandler) {
    return listen(this.options.getPort(), listenHandler);
  }

  @Override
  public MqttServer listen(int port, String host, Handler<AsyncResult<MqttServer>> listenHandler) {

    Future<MqttServer> fut = listen(port, host);
    if (listenHandler != null) {
      fut.onComplete(listenHandler);
    }
    return this;
  }

  @Override
  public synchronized MqttServer endpointHandler(Handler<MqttEndpoint> handler) {
    endpointHandler = handler;
    return this;
  }

  @Override
  public synchronized MqttServer exceptionHandler(Handler<Throwable> handler) {
    exceptionHandler = handler;
    return this;
  }

  @Override
  public int actualPort() {
    return server.actualPort();
  }

  @Override
  public Future<Void> close() {
    return server.close();
  }

  @Override
  public void close(Handler<AsyncResult<Void>> completionHandler) {
    server.close(completionHandler);
  }

  private void initChannel(ChannelPipeline pipeline) {
    pipeline.addBefore("handler", "mqttEncoder", MqttEncoder.INSTANCE);
    pipeline.addBefore("handler", "mqttDecoder",
            new MqttDecoder(this.options.getMaxMessageSize(), this.options.getMaxClientIdLength()));

    // adding the idle state handler for timeout on CONNECT packet
    pipeline.addBefore("handler", "idle", new IdleStateHandler(this.options.timeoutOnConnect(), 0, 0));
    pipeline.addBefore("handler", "timeoutOnConnect", new ChannelDuplexHandler() {
      @Override
      public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent e) {
          if (e.state() == IdleState.READER_IDLE) {
            // as MQTT 3.1.1 describes, if no packet is sent after a "reasonable" time (here CONNECT timeout)
            // the connection is closed
            ctx.channel().close();
          }
        }
        super.userEventTriggered(ctx, evt);
      }
    });
  }

}
