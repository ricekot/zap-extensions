/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2026 The ZAP Development Team
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
package org.zaproxy.addon.commonlib.internal.keyboard;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import org.zaproxy.addon.commonlib.actions.ActionRegistry;

/** Main-window bindings for actions which do not already have Swing menu accelerators. */
final class WindowActionBindings implements AutoCloseable {

    private final JRootPane root;
    private final ActionRegistry registry;
    private final List<InstalledBinding> installed = new ArrayList<>();

    WindowActionBindings(JRootPane root, ActionRegistry registry) {
        this.root = root;
        this.registry = registry;
    }

    void update(Map<String, KeyStroke> shortcuts, MenuActionAdapter menus) {
        close();
        InputMap input = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        shortcuts.forEach(
                (id, key) -> {
                    if (key == null || menus.getMenuItem(id) != null) {
                        return;
                    }
                    Object actionKey = new Object();
                    boolean local =
                            input.keys() != null && Arrays.asList(input.keys()).contains(key);
                    installed.add(
                            new InstalledBinding(key, actionKey, local ? input.get(key) : null));
                    input.put(key, actionKey);
                    root.getActionMap()
                            .put(
                                    actionKey,
                                    new AbstractAction() {
                                        @Override
                                        public boolean isEnabled() {
                                            return registry.getAction(id)
                                                    .map(action -> action.action().isEnabled())
                                                    .orElse(false);
                                        }

                                        @Override
                                        public void actionPerformed(ActionEvent event) {
                                            registry.invoke(id, event);
                                        }
                                    });
                });
    }

    @Override
    public void close() {
        InputMap input = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        for (InstalledBinding binding : installed) {
            if (input.get(binding.key()) == binding.actionKey()) {
                if (binding.previous() == null) {
                    input.remove(binding.key());
                } else {
                    input.put(binding.key(), binding.previous());
                }
            }
            root.getActionMap().remove(binding.actionKey());
        }
        installed.clear();
    }

    private record InstalledBinding(KeyStroke key, Object actionKey, Object previous) {}
}
