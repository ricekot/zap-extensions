/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2024 The ZAP Development Team
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
package org.zaproxy.addon.webui;

import java.io.File;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.zaproxy.addon.network.ExtensionNetwork;

public class ExtensionWebUi extends ExtensionAdaptor {

    // The name is public so that other extensions can access it
    public static final String NAME = "ExtensionWebUi";

    protected static final String PREFIX = "webui";

    private static final String BASE_DIRECTORY_NAME = "webui";
    private static final String TEMPLATES_DIRECTORY_NAME = "templates";

    private WebUiServer tutorialServer;

    public ExtensionWebUi() {
        super(NAME);
        setI18nPrefix(PREFIX);
    }

    @Override
    public void hook(ExtensionHook extensionHook) {
        super.hook(extensionHook);

        if (Constant.isDevMode()) {
            tutorialServer =
                    new WebUiServer(
                            this,
                            Control.getSingleton()
                                    .getExtensionLoader()
                                    .getExtension(ExtensionNetwork.class));
        }
    }

    @Override
    public void optionsLoaded() {
        if (tutorialServer != null) {
            tutorialServer.start();
        }
    }

    @Override
    public void unload() {
        if (tutorialServer != null) {
            tutorialServer.stop();
        }
    }

    @Override
    public boolean canUnload() {
        return true;
    }

    public static File getBaseDirectory() {
        return new File(Constant.getZapHome(), BASE_DIRECTORY_NAME);
    }

    @Override
    public String getDescription() {
        return Constant.messages.getString(PREFIX + ".desc");
    }
}
