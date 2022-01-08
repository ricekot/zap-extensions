/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2022 The ZAP Development Team
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
package org.zaproxy.addon.api;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionAdaptor;

public class ExtensionApi extends ExtensionAdaptor {

    private static final Logger LOGGER = LogManager.getLogger(ExtensionApi.class);

    private Server server;

    @Override
    public void init() {
        try {
            server = ServerBuilder.forPort(8081).addService(new AlertApiImpl()).build();
            server.start();
        } catch (IOException e) {
            LOGGER.error("Could not start gRPC API server: {}", e.getLocalizedMessage());
        }
    }

    @Override
    public void stop() {
        server.shutdown();
    }

    @Override
    public String getUIName() {
        return Constant.messages.getString("api.ext.name");
    }

    @Override
    public String getDescription() {
        return Constant.messages.getString("api.ext.desc");
    }
}
