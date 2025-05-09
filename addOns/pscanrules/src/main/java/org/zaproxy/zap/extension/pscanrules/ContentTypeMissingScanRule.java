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
package org.zaproxy.zap.extension.pscanrules;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.htmlparser.jericho.Source;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.addon.commonlib.CommonAlertTag;
import org.zaproxy.addon.commonlib.PolicyTag;
import org.zaproxy.zap.extension.pscan.PluginPassiveScanner;

public class ContentTypeMissingScanRule extends PluginPassiveScanner
        implements CommonPassiveScanRuleInfo {

    /** Prefix for internationalised messages used by this rule */
    private static final String MESSAGE_PREFIX = "pscanrules.contenttypemissing.";

    private static final Map<String, String> ALERT_TAGS;

    static {
        Map<String, String> alertTags =
                new HashMap<>(
                        CommonAlertTag.toMap(
                                CommonAlertTag.OWASP_2021_A05_SEC_MISCONFIG,
                                CommonAlertTag.OWASP_2017_A06_SEC_MISCONFIG));
        alertTags.put(PolicyTag.PENTEST.getTag(), "");
        alertTags.put(PolicyTag.QA_STD.getTag(), "");
        ALERT_TAGS = Collections.unmodifiableMap(alertTags);
    }

    private static final int PLUGIN_ID = 10019;

    @Override
    public void scanHttpResponseReceive(HttpMessage msg, int id, Source source) {
        if (msg.getResponseBody().length() > 0) {
            List<String> contentType =
                    msg.getResponseHeader().getHeaderValues(HttpHeader.CONTENT_TYPE);
            if (!contentType.isEmpty()) {
                for (String contentTypeDirective : contentType) {
                    if (contentTypeDirective.isEmpty()) {
                        buildAlert(false).raise();
                    }
                }
            } else {
                buildAlert(true).raise();
            }
        }
    }

    private AlertBuilder buildAlert(boolean isContentTypeMissing) {
        String issue = Constant.messages.getString(MESSAGE_PREFIX + "name.empty");
        String alertRef = PLUGIN_ID + "-2";
        if (isContentTypeMissing) {
            issue = getName();
            alertRef = PLUGIN_ID + "-1";
        }

        return newAlert()
                .setName(issue)
                .setRisk(Alert.RISK_INFO)
                .setConfidence(Alert.CONFIDENCE_MEDIUM)
                .setDescription(Constant.messages.getString(MESSAGE_PREFIX + "desc"))
                .setParam(HttpHeader.CONTENT_TYPE)
                .setSolution(Constant.messages.getString(MESSAGE_PREFIX + "soln"))
                .setReference(Constant.messages.getString(MESSAGE_PREFIX + "refs"))
                .setCweId(345) // CWE Id 345 - Insufficient Verification of Data Authenticity
                .setWascId(12) // WASC Id 12 - Content Spoofing)
                .setAlertRef(alertRef);
    }

    @Override
    public String getName() {
        return Constant.messages.getString(MESSAGE_PREFIX + "name");
    }

    @Override
    public Map<String, String> getAlertTags() {
        return ALERT_TAGS;
    }

    @Override
    public int getPluginId() {
        return PLUGIN_ID;
    }

    @Override
    public List<Alert> getExampleAlerts() {
        return List.of(buildAlert(true).build(), buildAlert(false).build());
    }
}
