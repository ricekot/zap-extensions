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

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.parosproxy.paros.Constant;
import org.zaproxy.addon.automation.jobs.PolicyDefinition.AlertTagRuleConfig;

@SuppressWarnings("serial")
public class AlertTagRulesTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    private static final String[] columnNames = {
        Constant.messages.getString("automation.dialog.ascanpolicytagrules.table.header.name"),
    };

    private List<AlertTagRuleConfig> alertTagRules = new ArrayList<>();

    public AlertTagRulesTableModel() {
        super();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public int getRowCount() {
        return alertTagRules.size();
    }

    @Override
    public Object getValueAt(int row, int col) {
        AlertTagRuleConfig alertTagRuleConfig = this.alertTagRules.get(row);
        if (alertTagRuleConfig != null) {
            return alertTagRuleConfig.getName();
        }
        return null;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    @Override
    public Class<?> getColumnClass(int c) {
        return String.class;
    }

    public List<AlertTagRuleConfig> getAlertTagRules() {
        return alertTagRules;
    }

    public void setAlertTagRules(List<AlertTagRuleConfig> alertTagRules) {
        if (alertTagRules == null) {
            this.alertTagRules = new ArrayList<>();
        } else {
            this.alertTagRules = alertTagRules;
        }
    }

    public void clear() {
        this.alertTagRules.clear();
    }

    public void add(AlertTagRuleConfig alertTagRule) {
        this.alertTagRules.add(alertTagRule);
        this.fireTableRowsInserted(this.alertTagRules.size() - 1, this.alertTagRules.size() - 1);
    }

    public void update(int tableIndex, AlertTagRuleConfig alertTagRule) {
        this.alertTagRules.set(tableIndex, alertTagRule);
        this.fireTableRowsUpdated(tableIndex, tableIndex);
    }

    public void remove(int index) {
        if (index < this.alertTagRules.size()) {
            this.alertTagRules.remove(index);
            this.fireTableRowsDeleted(index, index);
        }
    }
}
