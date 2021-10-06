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
package org.zaproxy.addon.nuclei;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.ImageIcon;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.extension.Extension;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.script.ExtensionScript;

public class ExtensionNuclei extends ExtensionAdaptor {

    private static final String NAME = ExtensionNuclei.class.getSimpleName();
    private static final Logger LOGGER = LogManager.getLogger(ExtensionNuclei.class);
    private static final List<Class<? extends Extension>> DEPENDENCIES;
    static final ImageIcon NUCLEI_ICON;

    static {
        NUCLEI_ICON =
                View.isInitialised()
                        ? new ImageIcon(
                                ExtensionNuclei.class.getResource(
                                        "/org/zaproxy/addon/nuclei/resources/icons/nuclei.png"))
                        : null;

        List<Class<? extends Extension>> dependencies = new ArrayList<>(1);
        dependencies.add(ExtensionScript.class);
        DEPENDENCIES = Collections.unmodifiableList(dependencies);
    }

    public ExtensionNuclei() {
        super(NAME);
    }

    @Override
    public List<Class<? extends Extension>> getDependencies() {
        return DEPENDENCIES;
    }
}
