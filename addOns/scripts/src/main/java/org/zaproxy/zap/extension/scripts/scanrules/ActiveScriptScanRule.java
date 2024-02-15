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
package org.zaproxy.zap.extension.scripts.scanrules;

import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.core.scanner.Plugin;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.addon.commonlib.scanrules.ScanRuleMetadata;
import org.zaproxy.zap.control.AddOn;
import org.zaproxy.zap.extension.script.ExtensionScript;
import org.zaproxy.zap.extension.script.ScriptWrapper;

public class ActiveScriptScanRule extends ActiveScriptHelper {

    private static final Logger LOGGER = LogManager.getLogger(ActiveScriptScanRule.class);
    private ExtensionScript extScript;
    private ScriptWrapper script;
    private ScanRuleMetadata metadata;

    /** Used via reflection in {@link org.parosproxy.paros.core.scanner.PluginFactory} */
    public ActiveScriptScanRule() {}

    public ActiveScriptScanRule(ScriptWrapper script, ScanRuleMetadata metadata) {
        this.script = script;
        this.metadata = metadata;
    }

    @Override
    public void scan() {
        if (!script.isEnabled()) {
            return;
        }
        try {
            ActiveScript2 s = getExtScript().getInterface(script, ActiveScript2.class);
            if (s != null) {
                HttpMessage msg = getNewMsg();
                LOGGER.debug(
                        "Calling script {} scanNode for {}",
                        script.getName(),
                        msg.getRequestHeader().getURI());
                s.scanNode(this, msg);
            }
        } catch (Exception e) {
            getExtScript().handleScriptException(script, e);
        }
        super.scan();
    }

    @Override
    public void scan(HttpMessage msg, String param, String value) {
        if (!script.isEnabled()) {
            return;
        }
        try {
            // TODO: Cache the interface?
            ActiveScript s = getExtScript().getInterface(script, ActiveScript.class);
            if (s != null) {
                s.scan(this, msg, param, value);
            }
        } catch (Exception e) {
            getExtScript().handleScriptException(script, e);
        }
    }

    @Override
    public void cloneInto(Plugin other) {
        if (!(other instanceof ActiveScriptScanRule)) {
            throw new IllegalArgumentException(
                    "Expected a ActiveScriptScanRule, but got " + other.getClass().getName());
        }
        var otherRule = (ActiveScriptScanRule) other;
        otherRule.script = script;
        otherRule.metadata = metadata;
    }

    final ScriptWrapper getScript() {
        return script;
    }

    void setMetadata(ScanRuleMetadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public int getId() {
        return metadata.getId();
    }

    @Override
    public String getName() {
        return metadata.getName();
    }

    @Override
    public String getDescription() {
        return metadata.getDescription();
    }

    @Override
    public int getCategory() {
        return metadata.getCategory().getValue();
    }

    @Override
    public String getSolution() {
        return metadata.getSolution();
    }

    @Override
    public String getReference() {
        return String.join("\n", metadata.getReferences());
    }

    @Override
    public int getRisk() {
        return metadata.getRisk().getValue();
    }

    @Override
    public int getCweId() {
        return metadata.getCweId();
    }

    @Override
    public int getWascId() {
        return metadata.getWascId();
    }

    @Override
    public Map<String, String> getAlertTags() {
        return metadata.getAlertTags();
    }

    @Override
    public AddOn.Status getStatus() {
        return metadata.getStatus();
    }

    private ExtensionScript getExtScript() {
        if (extScript == null) {
            extScript =
                    Control.getSingleton().getExtensionLoader().getExtension(ExtensionScript.class);
        }
        return extScript;
    }
}
