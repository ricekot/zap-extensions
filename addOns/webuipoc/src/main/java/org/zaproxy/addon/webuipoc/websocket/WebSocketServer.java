/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2012 The ZAP Development Team
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
package org.zaproxy.addon.webuipoc.websocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * An HTTP server which serves Web Socket requests at:
 *
 * <p>http://localhost:8080/websocket
 *
 * <p>Open your browser at <a href="http://localhost:8080/">http://localhost:8080/</a>, then the
 * demo page will be loaded and a Web Socket connection will be made automatically.
 *
 * <p>This server illustrates support for the different web socket specification versions and will
 * work with:
 *
 * <ul>
 *   <li>Safari 5+ (draft-ietf-hybi-thewebsocketprotocol-00)
 *   <li>Chrome 6-13 (draft-ietf-hybi-thewebsocketprotocol-00)
 *   <li>Chrome 14+ (draft-ietf-hybi-thewebsocketprotocol-10)
 *   <li>Chrome 16+ (RFC 6455 aka draft-ietf-hybi-thewebsocketprotocol-17)
 *   <li>Firefox 7+ (draft-ietf-hybi-thewebsocketprotocol-10)
 *   <li>Firefox 11+ (RFC 6455 aka draft-ietf-hybi-thewebsocketprotocol-17)
 * </ul>
 */
public final class WebSocketServer {

    static final boolean SSL = System.getProperty("ssl") != null;

    private WebSocketFrameHandler handler;

    public WebSocketServer() {
        handler = new WebSocketFrameHandler();
    }

    public void start(String host, int port) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new WebSocketServerInitializer(handler));

            Channel ch = b.bind(host, port).sync().channel();

            System.out.println(
                    "Open your web browser and navigate to "
                            + (SSL ? "https" : "http")
                            + "://"
                            + host
                            + ":"
                            + port
                            + '/');

            // ch.closeFuture().sync();
        } finally {
            //            bossGroup.shutdownGracefully();
            //            workerGroup.shutdownGracefully();
        }
    }

    public void sendMessage(WebSocketEvent message) {
        handler.sendMessage(message.toString());
    }
}
