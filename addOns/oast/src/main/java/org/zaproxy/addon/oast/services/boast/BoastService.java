/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2021 The ZAP Development Team
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
package org.zaproxy.addon.oast.services.boast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionHook;
import org.zaproxy.addon.oast.ExtensionOast;
import org.zaproxy.addon.oast.services.oast.OastOptionsPanelCard;
import org.zaproxy.addon.oast.services.oast.OastService;

public class BoastService extends OastService {

    private BoastOptionsPanelCard boastOptionsPanelCard;
    private BoastParam boastParam;
    private ExtensionOast extensionOast;

    private static final Logger LOGGER = LogManager.getLogger(OastService.class);

    private final List<BoastServer> registeredServers = new ArrayList<>();

    public BoastService(ExtensionOast extensionOast) {
        this.extensionOast = extensionOast;
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        BoastPoller boastPoller = new BoastPoller(this);
        executorService.scheduleAtFixedRate(boastPoller, 0, 1, TimeUnit.MINUTES);
    }

    @Override
    public String getName() {
        return Constant.messages.getString("oast.boast.name");
    }

    @Override
    public OastOptionsPanelCard getOptionsPanelCard() {
        if (boastOptionsPanelCard == null) {
            boastOptionsPanelCard = new BoastOptionsPanelCard(this);
        }
        return boastOptionsPanelCard;
    }

    @Override
    public void hook(ExtensionHook extensionHook) {
        extensionHook.addOptionsParamSet(getBoastParam());
    }

    public BoastParam getBoastParam() {
        if (boastParam == null) {
            boastParam = new BoastParam();
        }
        return boastParam;
    }

    public List<BoastServer> getRegisteredServers() {
        return registeredServers;
    }

    public BoastServer register(String boastUri) throws Exception {
        BoastServer boastServer = new BoastServer(boastUri);
        this.registeredServers.add(boastServer);
        return boastServer;
    }

    public ExtensionOast getExtensionOast() {
        return extensionOast;
    }
}
