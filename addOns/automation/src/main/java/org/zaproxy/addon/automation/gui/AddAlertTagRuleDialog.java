/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2025 The ZAP Development Team
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
package org.zaproxy.addon.automation.gui;

import static java.util.function.Predicate.not;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.Plugin;
import org.parosproxy.paros.core.scanner.Plugin.AlertThreshold;
import org.parosproxy.paros.core.scanner.Plugin.AttackStrength;
import org.zaproxy.addon.automation.jobs.JobUtils;
import org.zaproxy.addon.automation.jobs.PolicyDefinition.AlertTagRuleConfig;
import org.zaproxy.zap.extension.ascan.ScanPolicy;
import org.zaproxy.zap.utils.DisplayUtils;
import org.zaproxy.zap.view.StandardFieldsDialog;

@SuppressWarnings("serial")
public class AddAlertTagRuleDialog extends StandardFieldsDialog {

    private static final long serialVersionUID = 1L;

    private static final String[] TAB_LABELS = {
        "automation.dialog.ascanpolicytagrules.tab.config",
        "automation.dialog.ascanpolicytagrules.tab.simple",
        "automation.dialog.ascanpolicytagrules.tab.advanced"
    };

    private static final String TITLE = "automation.dialog.ascanpolicytagrules.title";

    private static final String NAME_PARAM = "automation.dialog.ascanpolicytagrules.name";
    private static final String STRENGTH_PARAM = "automation.dialog.ascanpolicytagrules.strength";
    private static final String THRESHOLD_PARAM = "automation.dialog.ascanpolicytagrules.threshold";
    private static final String INCLUDE_PARAM = "automation.dialog.ascanpolicytagrules.include";
    private static final String EXCLUDE_PARAM = "automation.dialog.ascanpolicytagrules.exclude";

    private ActiveScanPolicyDialog ascanPolicyDialog;
    private AlertTagRuleConfig alertTagRule;
    private AlertTagIncludeExcludePanel simpleIncludeExcludePanel;
    private int tableIndex;

    public AddAlertTagRuleDialog(
            ActiveScanPolicyDialog parent, AlertTagRuleConfig alertTagRule, int tableIndex) {
        super(parent, TITLE, DisplayUtils.getScaledDimension(400, 300), TAB_LABELS);
        this.ascanPolicyDialog = parent;
        if (alertTagRule == null) {
            alertTagRule = new AlertTagRuleConfig();
        }
        this.alertTagRule = alertTagRule;
        this.tableIndex = tableIndex;

        int tab = 0;
        this.addTextField(
                tab,
                NAME_PARAM,
                alertTagRule.getName() != null
                        ? alertTagRule.getName()
                        : "Rule #" + (tableIndex + 1));
        this.addComboField(
                tab,
                THRESHOLD_PARAM,
                Arrays.stream(AlertThreshold.values())
                        .map(AddAlertTagRuleDialog::getI18nThreshold)
                        .toList(),
                getI18nThreshold(alertTagRule.getThreshold()));
        this.addComboField(
                tab,
                STRENGTH_PARAM,
                Arrays.stream(AttackStrength.values())
                        .map(AddAlertTagRuleDialog::getI18nStrength)
                        .toList(),
                getI18nStrength(alertTagRule.getStrength()));
        this.addPadding(tab);

        tab++;
        Set<String> allActiveAlertTags =
                new ScanPolicy()
                        .getPluginFactory().getAllPlugin().stream()
                                .map(Plugin::getAlertTags)
                                .filter(Objects::nonNull)
                                .filter(not(Map::isEmpty))
                                .map(Map::keySet)
                                .flatMap(Set::stream)
                                .collect(Collectors.toCollection(TreeSet::new));
        Map<Boolean, Set<String>> includeTags =
                alertTagRule.getIncludePatterns().stream()
                        .map(Pattern::pattern)
                        .collect(
                                Collectors.partitioningBy(
                                        allActiveAlertTags::contains,
                                        Collectors.toCollection(TreeSet::new)));
        Map<Boolean, Set<String>> excludeTags =
                alertTagRule.getExcludePatterns().stream()
                        .map(Pattern::pattern)
                        .collect(
                                Collectors.partitioningBy(
                                        allActiveAlertTags::contains,
                                        Collectors.toCollection(TreeSet::new)));
        Set<String> availableTags =
                allActiveAlertTags.stream()
                        .filter(
                                tag ->
                                        !includeTags.get(true).contains(tag)
                                                && !excludeTags.get(true).contains(tag))
                        .collect(Collectors.toCollection(TreeSet::new));
        this.simpleIncludeExcludePanel =
                new AlertTagIncludeExcludePanel(
                        availableTags, includeTags.get(true), excludeTags.get(true));
        this.addCustomComponent(tab, simpleIncludeExcludePanel);

        tab++;
        this.addMultilineField(tab, INCLUDE_PARAM, String.join("\n", includeTags.get(false)));
        this.addMultilineField(tab, EXCLUDE_PARAM, String.join("\n", excludeTags.get(false)));
    }

    private List<Pattern> stringParamToPatternList(String param) {
        return Arrays.stream(this.getStringValue(param).split("\n"))
                .map(String::trim)
                .filter(not(String::isEmpty))
                .map(Pattern::compile)
                .toList();
    }

    private List<String> stringParamToList(String param) {
        return Arrays.stream(this.getStringValue(param).split("\n"))
                .map(String::trim)
                .filter(not(String::isEmpty))
                .toList();
    }

    private static String getI18nThreshold(AlertThreshold threshold) {
        return JobUtils.thresholdToI18n(threshold.name());
    }

    private static String getI18nStrength(AttackStrength strength) {
        return JobUtils.strengthToI18n(strength.name());
    }

    @Override
    public void save() {
        this.alertTagRule.setName(this.getStringValue(NAME_PARAM).trim());
        this.alertTagRule.setThreshold(
                AlertThreshold.valueOf(
                        JobUtils.i18nToThreshold(this.getStringValue(THRESHOLD_PARAM))
                                .toUpperCase(Locale.ROOT)));
        this.alertTagRule.setStrength(
                AttackStrength.valueOf(
                        JobUtils.i18nToStrength(this.getStringValue(STRENGTH_PARAM))
                                .toUpperCase(Locale.ROOT)));

        List<Pattern> includePatterns = new ArrayList<>(stringParamToPatternList(INCLUDE_PARAM));
        includePatterns.addAll(
                simpleIncludeExcludePanel.getIncludedTags().stream()
                        .map(Pattern::compile)
                        .toList());
        this.alertTagRule.setIncludePatterns(includePatterns);

        List<Pattern> excludePatterns = new ArrayList<>(stringParamToPatternList(EXCLUDE_PARAM));
        excludePatterns.addAll(
                simpleIncludeExcludePanel.getExcludedTags().stream()
                        .map(Pattern::compile)
                        .toList());
        this.alertTagRule.setExcludePatterns(excludePatterns);

        if (this.tableIndex == ascanPolicyDialog.getAlertTagRulesModel().getRowCount()) {
            ascanPolicyDialog.getAlertTagRulesModel().add(this.alertTagRule);
        } else {
            ascanPolicyDialog.getAlertTagRulesModel().update(this.tableIndex, this.alertTagRule);
        }
    }

    @Override
    public String validateFields() {
        if (this.getStringValue(NAME_PARAM).trim().isEmpty()) {
            return Constant.messages.getString("automation.dialog.context.error.badname");
        }
        if (JobUtils.i18nToThreshold(this.getStringValue(THRESHOLD_PARAM)).equals("default")
                && JobUtils.i18nToStrength(this.getStringValue(STRENGTH_PARAM)).equals("default")) {
            return Constant.messages.getString("automation.dialog.addrule.error.defaults");
        }
        for (String str : stringParamToList(INCLUDE_PARAM)) {
            if (!JobUtils.containsVars(str)) {
                // Can only validate strings that dont contain env vars
                try {
                    Pattern.compile(str);
                } catch (Exception e) {
                    return Constant.messages.getString(
                            "automation.dialog.ascanpolicytagrules.error.incregex", str);
                }
            }
        }
        for (String str : stringParamToList(EXCLUDE_PARAM)) {
            if (!JobUtils.containsVars(str)) {
                // Can only validate strings that dont contain env vars
                try {
                    Pattern.compile(str);
                } catch (Exception e) {
                    return Constant.messages.getString(
                            "automation.dialog.ascanpolicytagrules.error.excregex", str);
                }
            }
        }
        return null;
    }
}
