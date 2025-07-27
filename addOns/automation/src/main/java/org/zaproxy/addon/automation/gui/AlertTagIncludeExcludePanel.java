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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.zaproxy.zap.view.LayoutHelper;

@SuppressWarnings("serial")
public class AlertTagIncludeExcludePanel extends JXPanel {

    private final DefaultListModel<String> includeModel = new DefaultListModel<>();
    private final DefaultListModel<String> availableModel = new DefaultListModel<>();
    private final DefaultListModel<String> excludeModel = new DefaultListModel<>();

    private final JXList includeList = new JXList(includeModel);
    private final JXList availableList = new JXList(availableModel);
    private final JXList excludeList = new JXList(excludeModel);

    public AlertTagIncludeExcludePanel(
            Set<String> availableTags, Set<String> includeTags, Set<String> excludeTags) {
        availableModel.addAll(availableTags);
        includeModel.addAll(includeTags);
        excludeModel.addAll(excludeTags);

        configureList(includeList);
        configureList(availableList);
        configureList(excludeList);

        setLayout(new GridBagLayout());

        int column = -1;
        add(
                makeColumn("Include", includeList),
                LayoutHelper.getGBC(++column, 0, 1, 0.4, 1.0, GridBagConstraints.BOTH));
        add(
                makeArrowPanel(
                        makeButton("<", availableList, availableModel, includeModel),
                        makeButton(">", includeList, includeModel, availableModel)),
                LayoutHelper.getGBC(++column, 0, 1, 0));
        add(
                makeColumn("Available", availableList),
                LayoutHelper.getGBC(++column, 0, 1, 0.4, 1.0, GridBagConstraints.BOTH));
        add(
                makeArrowPanel(
                        makeButton(">", availableList, availableModel, excludeModel),
                        makeButton("<", excludeList, excludeModel, availableModel)),
                LayoutHelper.getGBC(++column, 0, 1, 0));
        add(
                makeColumn("Exclude", excludeList),
                LayoutHelper.getGBC(++column, 0, 1, 0.4, 1.0, GridBagConstraints.BOTH));
    }

    private void configureList(JXList list) {
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.setHighlighters(HighlighterFactory.createSimpleStriping());
        // list.setDragEnabled(true);
        // list.setDropMode(DropMode.INSERT);
        // list.setTransferHandler(new ListItemTransferHandler());
        // list.setVisibleRowCount(15);
    }

    private JXPanel makeColumn(String title, JXList list) {
        JXPanel p = new JXPanel(new BorderLayout(5, 5));
        p.setBorder(BorderFactory.createTitledBorder(title));
        var scrollPane = new JScrollPane(list);
        // Ensure that it fills all available space regardless of parent size
        scrollPane.setMinimumSize(new Dimension(80, 210));
        p.add(scrollPane, BorderLayout.CENTER);
        return p;
    }

    private JPanel makeArrowPanel(JButton top, JButton bottom) {
        JPanel p = new JPanel(new GridLayout(2, 1, 3, 3));
        p.add(top);
        p.add(bottom);
        return p;
    }

    private JButton makeButton(
            String text,
            JXList sourceList,
            DefaultListModel<String> sourceListModel,
            DefaultListModel<String> destinationListModel) {
        JButton b = new JButton(text);
        b.setMargin(new Insets(1, 6, 1, 6));
        b.setFocusable(false);
        b.addActionListener(
                e -> {
                    for (var selectedValue : sourceList.getSelectedValuesList()) {
                        sourceListModel.removeElement(selectedValue);

                        // Find the correct position to insert the element to maintain sorted order
                        String valueToInsert = (String) selectedValue;
                        int insertIndex = 0;
                        for (int i = 0; i < destinationListModel.size(); i++) {
                            if (destinationListModel.getElementAt(i).compareTo(valueToInsert) > 0) {
                                insertIndex = i;
                                break;
                            }
                            insertIndex = i + 1;
                        }
                        destinationListModel.add(insertIndex, valueToInsert);
                    }
                });
        return b;
    }

    public List<String> getIncludedTags() {
        return Collections.list(includeModel.elements());
    }

    public List<String> getExcludedTags() {
        return Collections.list(excludeModel.elements());
    }
}
