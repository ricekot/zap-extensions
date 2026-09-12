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

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.swing.SwingUtilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The shared application-action registry. Obtain the GUI instance from {@link
 * org.zaproxy.addon.commonlib.ExtensionCommonlib#getActionRegistry()}.
 *
 * <p>All operations, including disposing registrations and changing registered Swing actions, must
 * occur on the EDT. Add-ons must close their registration handles on unload. Registration does not
 * require a menu item or a default shortcut.
 *
 * @since 1.45.0
 */
public final class ActionRegistry {

    private static final Logger LOGGER = LogManager.getLogger(ActionRegistry.class);

    private final Map<String, Entry> actions = new LinkedHashMap<>();
    private final List<Runnable> listeners = new ArrayList<>();

    /** An idempotent, EDT-only registration handle. */
    @FunctionalInterface
    public interface Registration extends AutoCloseable {
        @Override
        void close();
    }

    /**
     * Registers an action. Duplicate identifiers are rejected; close the previous registration
     * before replacing an action.
     *
     * @param action the action.
     * @return a handle which removes this registration, not any subsequent reuse of the identifier.
     */
    public Registration register(RegisteredAction action) {
        requireEdt();
        Objects.requireNonNull(action);
        if (actions.containsKey(action.id())) {
            throw new IllegalArgumentException("Action already registered: " + action.id());
        }
        PropertyChangeListener listener = event -> notifyListeners();
        Entry entry = new Entry(action, listener);
        actions.put(action.id(), entry);
        action.action().addPropertyChangeListener(listener);
        notifyListeners();
        return () -> {
            requireEdt();
            if (actions.get(action.id()) == entry) {
                actions.remove(action.id());
                action.action().removePropertyChangeListener(listener);
                notifyListeners();
            }
        };
    }

    /**
     * Gets an immutable snapshot in registration order. The enclosed Swing actions remain live.
     *
     * @return the registered actions.
     */
    public List<RegisteredAction> getActions() {
        requireEdt();
        return actions.values().stream().map(Entry::action).toList();
    }

    /**
     * Gets an action by its stable identifier.
     *
     * @param id the identifier.
     * @return the action, if registered.
     */
    public Optional<RegisteredAction> getAction(String id) {
        requireEdt();
        Entry entry = actions.get(id);
        return entry == null ? Optional.empty() : Optional.of(entry.action());
    }

    /**
     * Invokes an action only if it is still registered and currently enabled.
     *
     * @param id the identifier.
     * @param event the invocation event.
     * @return whether the action was invoked.
     */
    public boolean invoke(String id, ActionEvent event) {
        requireEdt();
        Objects.requireNonNull(event);
        Entry entry = actions.get(id);
        if (entry == null || !entry.action().action().isEnabled()) {
            return false;
        }
        entry.action().action().actionPerformed(event);
        return true;
    }

    /**
     * Observes registration, removal, and Swing action property changes. Callbacks run on the EDT.
     *
     * @param listener the observer.
     * @return a handle which removes the observer.
     */
    public Registration addChangeListener(Runnable listener) {
        requireEdt();
        Objects.requireNonNull(listener);
        // A distinct wrapper lets the same listener be independently registered more than once.
        Runnable registered =
                new Runnable() {
                    @Override
                    public void run() {
                        listener.run();
                    }
                };
        listeners.add(registered);
        return () -> {
            requireEdt();
            listeners.remove(registered);
        };
    }

    /** Removes all actions and detaches their property listeners. Observers remain registered. */
    public void clear() {
        requireEdt();
        actions.values()
                .forEach(
                        entry ->
                                entry.action()
                                        .action()
                                        .removePropertyChangeListener(entry.listener()));
        actions.clear();
        notifyListeners();
    }

    private void notifyListeners() {
        requireEdt();
        for (Runnable listener : List.copyOf(listeners)) {
            try {
                listener.run();
            } catch (RuntimeException e) {
                LOGGER.error("Error notifying an action registry observer", e);
            }
        }
    }

    private static void requireEdt() {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("Action registry operations must run on the EDT.");
        }
    }

    private record Entry(RegisteredAction action, PropertyChangeListener listener) {}
}
