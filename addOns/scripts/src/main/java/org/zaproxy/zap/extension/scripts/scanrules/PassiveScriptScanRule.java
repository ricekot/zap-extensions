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
import net.htmlparser.jericho.Source;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.addon.commonlib.scanrules.ScanRuleMetadata;
import org.zaproxy.zap.control.AddOn;
import org.zaproxy.zap.extension.pscan.PluginPassiveScanner;
import org.zaproxy.zap.extension.script.ExtensionScript;
import org.zaproxy.zap.extension.script.ScriptWrapper;

public class PassiveScriptScanRule extends PassiveScriptHelper {

    private static final Logger LOGGER = LogManager.getLogger(PassiveScriptScanRule.class);
    private ExtensionScript extScript;
    private ScriptWrapper script;
    private ScanRuleMetadata metadata;

    /** Used via reflection in {@link org.parosproxy.paros.core.scanner.PluginFactory} */
    //    public ActiveScriptScanRule() {}

    public PassiveScriptScanRule(ScriptWrapper script, ScanRuleMetadata metadata) {
        this.script = script;
        this.metadata = metadata;
    }

    @Override
    public void scanHttpResponseReceive(HttpMessage msg, int id, Source source) {
        try {
            // TODO: Cache the interface?
            var s = getExtScript().getInterface(script, PassiveScript.class);
            if (s != null) {
                s.scan(this, msg, source);
            }
        } catch (Exception e) {
            getExtScript().handleScriptException(script, e);
        }
    }

    @Override
    public boolean appliesToHistoryType(int historyType) {
        try {
            // TODO: Cache the interface?
            var s = getExtScript().getInterface(script, PassiveScript.class);
            if (s != null) {
                return s.appliesToHistoryType(historyType);
            }
        } catch (Exception e) {
            getExtScript().handleScriptException(script, e);
        }
        return false;
    }

    @Override
    public PluginPassiveScanner copy() {
        return new PassiveScriptScanRule(script, metadata);
    }

    @Override
    public String getName() {
        return metadata.getName();
    }

    @Override
    public int getPluginId() {
        return metadata.getId();
    }

    @Override
    public AddOn.Status getStatus() {
        return metadata.getStatus();
    }

    @Override
    public Map<String, String> getAlertTags() {
        return metadata.getAlertTags();
    }

    @Override
    public void setEnabled(boolean enabled) {
        script.setEnabled(enabled);
    }

    @Override
    public boolean isEnabled() {
        return script.isEnabled();
    }

    final ScriptWrapper getScript() {
        return script;
    }

    void setMetadata(ScanRuleMetadata metadata) {
        this.metadata = metadata;
    }

    private ExtensionScript getExtScript() {
        if (extScript == null) {
            extScript =
                    Control.getSingleton().getExtensionLoader().getExtension(ExtensionScript.class);
        }
        return extScript;
    }
}
