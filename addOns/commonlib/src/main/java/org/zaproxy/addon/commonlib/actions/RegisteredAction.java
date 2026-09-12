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
package org.zaproxy.addon.commonlib.actions;

import java.util.Objects;
import javax.swing.Action;
import javax.swing.KeyStroke;

/**
 * An application action shared by keyboard configuration and other action-based UI.
 *
 * <p>The identifier must be stable across versions and independent of the locale. Use an add-on
 * namespace for new identifiers; adapters for existing menus preserve their original identifiers.
 * {@link Action#NAME} supplies the localized display name, {@link Action#SHORT_DESCRIPTION} an
 * optional description, and {@link Action#isEnabled()} the current availability. Execution and
 * changes to the Swing action must take place on the EDT. Context-specific actions must not be
 * registered unless their callback and availability can resolve the correct context independently
 * of the caller.
 *
 * @param id the stable identifier.
 * @param action the executable Swing action, with a non-blank name.
 * @param defaultKeyStroke the default shortcut, or {@code null} if initially unbound. The registry
 *     does not use {@link Action#ACCELERATOR_KEY}, which can represent a user override.
 * @since 1.45.0
 */
public record RegisteredAction(String id, Action action, KeyStroke defaultKeyStroke) {

    public RegisteredAction {
        Objects.requireNonNull(id);
        Objects.requireNonNull(action);
        if (id.isBlank()) {
            throw new IllegalArgumentException("An action identifier must not be blank.");
        }
        if (!(action.getValue(Action.NAME) instanceof String name) || name.isBlank()) {
            throw new IllegalArgumentException("An action must have a non-blank localized name.");
        }
    }
}
