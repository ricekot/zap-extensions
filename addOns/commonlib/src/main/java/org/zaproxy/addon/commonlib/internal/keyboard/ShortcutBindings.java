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

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.KeyStroke;
import org.zaproxy.addon.commonlib.actions.ActionRegistry;
import org.zaproxy.addon.commonlib.actions.RegisteredAction;

/** Resolves bindings independently of menu or options UI. Accessed on the EDT. */
final class ShortcutBindings {

    private final ActionRegistry registry;
    private final KeyboardParam parameters;

    ShortcutBindings(ActionRegistry registry, KeyboardParam parameters) {
        this.registry = registry;
        this.parameters = parameters;
    }

    Map<String, KeyStroke> getShortcuts(boolean defaults) {
        List<RegisteredAction> actions = registry.getActions();
        Map<String, KeyStroke> result = new LinkedHashMap<>();
        Set<KeyStroke> assigned = new HashSet<>();
        // Explicit overrides take precedence over defaults, including defaults registered later.
        if (!defaults) {
            for (RegisteredAction action : actions) {
                if (parameters.hasShortcut(action.id())) {
                    putBinding(result, assigned, action.id(), parameters.getShortcut(action.id()));
                }
            }
        }
        for (RegisteredAction action : actions) {
            if (!result.containsKey(action.id())) {
                putBinding(result, assigned, action.id(), action.defaultKeyStroke());
            }
        }
        return result;
    }

    private static void putBinding(
            Map<String, KeyStroke> bindings, Set<KeyStroke> assigned, String id, KeyStroke key) {
        if (key == null || key.getKeyCode() == 0 || !assigned.add(key)) {
            bindings.put(id, null);
        } else {
            bindings.put(id, key);
        }
    }

    void setShortcut(String id, KeyStroke key) {
        if (registry.getAction(id).isEmpty()) {
            return;
        }
        if (key != null) {
            getShortcuts(false)
                    .forEach(
                            (other, current) -> {
                                if (!id.equals(other) && key.equals(current)) {
                                    parameters.setShortcut(other, null);
                                }
                            });
        }
        parameters.setShortcut(id, key);
    }

    void save(List<KeyboardShortcut> shortcuts, boolean reset) {
        if (reset) {
            // Keep preferences for actions belonging to temporarily unavailable add-ons.
            registry.getActions().forEach(action -> parameters.removeShortcut(action.id()));
        }
        shortcuts.stream()
                .filter(KeyboardShortcut::isChanged)
                .forEach(
                        shortcut -> setShortcut(shortcut.getIdentifier(), shortcut.getKeyStroke()));
        parameters.setConfigs();
    }
}
