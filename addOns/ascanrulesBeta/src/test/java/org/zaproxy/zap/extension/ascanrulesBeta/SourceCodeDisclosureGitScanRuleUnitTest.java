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
package org.zaproxy.zap.extension.ascanrulesBeta;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.core.scanner.Alert;
import org.zaproxy.addon.commonlib.CommonAlertTag;
import org.zaproxy.addon.commonlib.PolicyTag;

class SourceCodeDisclosureGitScanRuleUnitTest
        extends ActiveScannerTest<SourceCodeDisclosureGitScanRule> {

    @Override
    protected SourceCodeDisclosureGitScanRule createScanner() {
        return new SourceCodeDisclosureGitScanRule();
    }

    @Test
    void shouldReturnExpectedMappings() {
        // Given / When
        int cwe = rule.getCweId();
        int wasc = rule.getWascId();
        Map<String, String> tags = rule.getAlertTags();
        // Then
        assertThat(cwe, is(equalTo(541)));
        assertThat(wasc, is(equalTo(34)));
        assertThat(tags.size(), is(equalTo(4)));
        assertThat(
                tags.containsKey(CommonAlertTag.OWASP_2021_A05_SEC_MISCONFIG.getTag()),
                is(equalTo(true)));
        assertThat(
                tags.containsKey(CommonAlertTag.OWASP_2017_A06_SEC_MISCONFIG.getTag()),
                is(equalTo(true)));
        assertThat(tags.containsKey(PolicyTag.QA_FULL.getTag()), is(equalTo(true)));
        assertThat(tags.containsKey(PolicyTag.PENTEST.getTag()), is(equalTo(true)));
        assertThat(
                tags.get(CommonAlertTag.OWASP_2021_A05_SEC_MISCONFIG.getTag()),
                is(equalTo(CommonAlertTag.OWASP_2021_A05_SEC_MISCONFIG.getValue())));
        assertThat(
                tags.get(CommonAlertTag.OWASP_2017_A06_SEC_MISCONFIG.getTag()),
                is(equalTo(CommonAlertTag.OWASP_2017_A06_SEC_MISCONFIG.getValue())));
    }

    @Test
    void shouldReturnExpectedExampleAlert() {
        // Given / When
        List<Alert> alerts = rule.getExampleAlerts();
        // Then
        assertThat(alerts.size(), is(equalTo(1)));
        Alert alert = alerts.get(0);
        Map<String, String> tags = alert.getTags();
        assertThat(tags.size(), is(equalTo(5)));
        assertThat(tags, hasKey("CWE-541"));
        assertThat(tags, hasKey(CommonAlertTag.OWASP_2017_A06_SEC_MISCONFIG.getTag()));
        assertThat(tags, hasKey(CommonAlertTag.OWASP_2021_A05_SEC_MISCONFIG.getTag()));
        assertThat(tags, hasKey(PolicyTag.QA_FULL.getTag()));
        assertThat(tags, hasKey(PolicyTag.PENTEST.getTag()));
        assertThat(alert.getRisk(), is(equalTo(Alert.RISK_HIGH)));
        assertThat(alert.getConfidence(), is(equalTo(Alert.CONFIDENCE_MEDIUM)));
    }
}
