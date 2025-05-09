/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2016 The ZAP Development Team
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
import java.util.Set;
import net.htmlparser.jericho.Source;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.core.scanner.Plugin.AlertThreshold;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.addon.commonlib.CommonAlertTag;
import org.zaproxy.addon.commonlib.CookieUtils;
import org.zaproxy.addon.commonlib.PolicyTag;
import org.zaproxy.zap.extension.pscan.PluginPassiveScanner;

public class CookieSameSiteScanRule extends PluginPassiveScanner
        implements CommonPassiveScanRuleInfo {

    /** Prefix for internationalised messages used by this rule */
    private static final String MESSAGE_PREFIX = "pscanrules.cookiesamesite.";

    private static final Map<String, String> ALERT_TAGS;

    static {
        Map<String, String> alertTags =
                new HashMap<>(
                        CommonAlertTag.toMap(
                                CommonAlertTag.OWASP_2021_A01_BROKEN_AC,
                                CommonAlertTag.OWASP_2017_A05_BROKEN_AC,
                                CommonAlertTag.WSTG_V42_SESS_02_COOKIE_ATTRS));
        alertTags.put(PolicyTag.PENTEST.getTag(), "");
        alertTags.put(PolicyTag.DEV_STD.getTag(), "");
        alertTags.put(PolicyTag.QA_STD.getTag(), "");
        ALERT_TAGS = Collections.unmodifiableMap(alertTags);
    }

    private static final int PLUGIN_ID = 10054;

    private static final String SAME_SITE_COOKIE_ATTRIBUTE = "SameSite";
    private static final String SAME_SITE_COOKIE_VALUE_STRICT = "Strict";
    private static final String SAME_SITE_COOKIE_VALUE_LAX = "Lax";
    private static final String SAME_SITE_COOKIE_VALUE_NONE = "None";

    private Model model = null;

    @Override
    public void scanHttpResponseReceive(HttpMessage msg, int id, Source source) {
        checkCookies(msg, HttpHeader.SET_COOKIE);
        checkCookies(msg, HttpHeader.SET_COOKIE2);
    }

    private void checkCookies(HttpMessage msg, String cookieHeader) {
        List<String> cookies = msg.getResponseHeader().getHeaderValues(cookieHeader);

        if (cookies.isEmpty()) {
            return;
        }

        Set<String> ignoreList = CookieUtils.getCookieIgnoreList(getModel());

        for (String cookie : cookies) {
            if (ignoreList.contains(CookieUtils.getCookieName(cookie))
                    || CookieUtils.isExpired(cookie)) {
                continue;
            }
            String sameSiteVal = CookieUtils.getAttributeValue(cookie, SAME_SITE_COOKIE_ATTRIBUTE);
            if (sameSiteVal == null) {
                // Its missing
                buildMissingAlert(msg.getResponseHeader().toString(), cookie).raise();
            } else if (sameSiteVal.equalsIgnoreCase(SAME_SITE_COOKIE_VALUE_NONE)
                    && !AlertThreshold.HIGH.equals(this.getAlertThreshold())) {
                buildNoneAlert(msg.getResponseHeader().toString(), cookie).raise();
            } else if (!(sameSiteVal.equalsIgnoreCase(SAME_SITE_COOKIE_VALUE_STRICT)
                    || sameSiteVal.equalsIgnoreCase(SAME_SITE_COOKIE_VALUE_LAX)
                    || sameSiteVal.equalsIgnoreCase(SAME_SITE_COOKIE_VALUE_NONE))) {
                // Its present but with an illegal value
                buildBadValueAlert(msg.getResponseHeader().toString(), cookie).raise();
            }
        }
    }

    private AlertBuilder buildAlert(String responseHeader, String cookieHeaderValue) {
        return newAlert()
                .setRisk(Alert.RISK_LOW)
                .setConfidence(Alert.CONFIDENCE_MEDIUM)
                .setParam(CookieUtils.getCookieName(cookieHeaderValue))
                .setSolution(Constant.messages.getString(MESSAGE_PREFIX + "soln"))
                .setReference(Constant.messages.getString(MESSAGE_PREFIX + "refs"))
                .setEvidence(CookieUtils.getSetCookiePlusName(responseHeader, cookieHeaderValue))
                .setCweId(1275) // CWE-1275: Sensitive Cookie with Improper SameSite Attribute
                .setWascId(13); // WASC Id - Info leakage
    }

    private AlertBuilder buildMissingAlert(String responseHeader, String cookieHeaderValue) {
        return buildAlert(responseHeader, cookieHeaderValue)
                .setName(getName())
                .setDescription(Constant.messages.getString(MESSAGE_PREFIX + "desc"))
                .setAlertRef(PLUGIN_ID + "-1");
    }

    private AlertBuilder buildNoneAlert(String responseHeader, String cookieHeaderValue) {
        return buildAlert(responseHeader, cookieHeaderValue)
                .setName(Constant.messages.getString(MESSAGE_PREFIX + "none.name"))
                .setDescription(Constant.messages.getString(MESSAGE_PREFIX + "none.desc"))
                .setAlertRef(PLUGIN_ID + "-2");
    }

    private AlertBuilder buildBadValueAlert(String responseHeader, String cookieHeaderValue) {
        return buildAlert(responseHeader, cookieHeaderValue)
                .setName(Constant.messages.getString(MESSAGE_PREFIX + "badval.name"))
                .setDescription(Constant.messages.getString(MESSAGE_PREFIX + "badval.desc"))
                .setAlertRef(PLUGIN_ID + "-3");
    }

    @Override
    public int getPluginId() {
        return PLUGIN_ID;
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
    public List<Alert> getExampleAlerts() {
        return List.of(
                buildMissingAlert(
                                "HTTP/1.1 200 OK\n"
                                        + "Server: Apache-Coyote/1.1\n"
                                        + "Content-Type: text/html;charset=ISO-8859-1\n"
                                        + "Content-Length: 13\n"
                                        + "set-cookie: test=123; Path=/",
                                "test=123; Path=/")
                        .build(),
                buildNoneAlert(
                                "HTTP/1.1 200 OK\n"
                                        + "Server: Apache-Coyote/1.1\n"
                                        + "Content-Type: text/html;charset=ISO-8859-1\n"
                                        + "Content-Length: 13\n"
                                        + "set-cookie: test=123; Path=/; SameSite=none",
                                "test=123; Path=/; SameSite=none")
                        .build(),
                buildBadValueAlert(
                                "HTTP/1.1 200 OK\n"
                                        + "Server: Apache-Coyote/1.1\n"
                                        + "Content-Type: text/html;charset=ISO-8859-1\n"
                                        + "Content-Length: 13\n"
                                        + "set-cookie: test=123; Path=/; SameSite=badVal",
                                "test=123; Path=/; SameSite=badVal")
                        .build());
    }

    private Model getModel() {
        if (this.model == null) {
            this.model = Model.getSingleton();
        }
        return this.model;
    }

    /*
     * Just for use in the unit tests
     */
    protected void setModel(Model model) {
        this.model = model;
    }
}
