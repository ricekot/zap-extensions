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

import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.control.Control;
import org.zaproxy.addon.commonlib.scanrules.ScanRuleMetadataProvider;
import org.zaproxy.zap.extension.pscan.ExtensionPassiveScan;
import org.zaproxy.zap.extension.script.ExtensionScript;
import org.zaproxy.zap.extension.script.ScriptWrapper;

public class PassiveScriptSynchronizer {

    private static final Logger LOGGER = LogManager.getLogger(PassiveScriptSynchronizer.class);

    private ExtensionScript extScript;
    private ExtensionPassiveScan extPscan;

    private final Map<Integer, PassiveScriptScanRule> scriptIdToScanRuleMap = new HashMap<>();

    public void scriptAdded(ScriptWrapper script) {
        try {
            var metadataProvider =
                    getExtScript().getInterface(script, ScanRuleMetadataProvider.class);
            if (metadataProvider == null) {
                return;
            }
            var metadata = metadataProvider.getMetadata();

            // TODO: Should we check non-script scan rules as well?
            // Check if a passive script with this ID already exists
            PassiveScriptScanRule scanRule = scriptIdToScanRuleMap.get(metadata.getId());
            if (scanRule != null) {
                if (scanRule.getScript() == script) {
                    scanRule.setMetadata(metadataProvider.getMetadata());
                } else {
                    LOGGER.error(
                            "A passive script with the ID ({}) already exists: \"{}\"",
                            metadata.getId(),
                            scanRule.getScript().getName());
                }
                return;
            }

            scanRule = new PassiveScriptScanRule(script, metadata);
            if (!getExtPscan().addPluginPassiveScanner(scanRule)) {
                LOGGER.error("Failed to install script scan rule: {}", script.getName());
                return;
            }
            scriptIdToScanRuleMap.put(metadata.getId(), scanRule);
        } catch (Exception e) {
            getExtScript().handleScriptException(script, e);
        }
    }

    public void scriptRemoved(ScriptWrapper script) {
        try {
            var metadataProvider =
                    getExtScript().getInterface(script, ScanRuleMetadataProvider.class);
            if (metadataProvider == null) {
                return;
            }
            var metadata = metadataProvider.getMetadata();

            PassiveScriptScanRule scanRule = scriptIdToScanRuleMap.get(metadata.getId());
            if (scanRule == null) {
                return;
            }
            if (!getExtPscan().removePluginPassiveScanner(scanRule)) {
                LOGGER.error("Failed to uninstall script scan rule: {}", scanRule.getName());
                return;
            }
            scriptIdToScanRuleMap.remove(metadata.getId());
        } catch (Exception e) {
            extScript.handleScriptException(script, e);
        }
    }

    public void unload() {
        for (var scanRule : scriptIdToScanRuleMap.values()) {
            if (!getExtPscan().removePluginPassiveScanner(scanRule)) {
                LOGGER.error("Failed to uninstall script scan rule: {}", scanRule.getName());
                return;
            }
        }
    }

    private ExtensionScript getExtScript() {
        if (extScript == null) {
            extScript =
                    Control.getSingleton().getExtensionLoader().getExtension(ExtensionScript.class);
        }
        return extScript;
    }

    private ExtensionPassiveScan getExtPscan() {
        if (extPscan == null) {
            extPscan =
                    Control.getSingleton()
                            .getExtensionLoader()
                            .getExtension(ExtensionPassiveScan.class);
        }
        return extPscan;
    }
}
