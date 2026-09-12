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

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zaproxy.addon.commonlib.actions.ActionRegistry;
import org.zaproxy.addon.commonlib.actions.RegisteredAction;
import org.zaproxy.zap.view.ZapMenuItem;

/** Registers existing and dynamically added main-menu actions. All operations run on the EDT. */
final class MenuActionAdapter extends ContainerAdapter implements AutoCloseable {

    private static final Logger LOGGER = LogManager.getLogger(MenuActionAdapter.class);

    private final ActionRegistry registry;
    private final Map<String, MenuAction> actions = new LinkedHashMap<>();
    private final Set<Container> containers = Collections.newSetFromMap(new IdentityHashMap<>());

    MenuActionAdapter(ActionRegistry registry) {
        this.registry = registry;
    }

    void start(JMenuBar menuBar) {
        observeContainer(menuBar);
    }

    private void observeContainer(Container container) {
        if (containers.add(container)) {
            container.addContainerListener(this);
            for (Component child : container.getComponents()) {
                observe(child);
            }
        }
    }

    private void observe(Component component) {
        if (component instanceof JMenu menu) {
            observeContainer(menu.getPopupMenu());
        } else if (component instanceof ZapMenuItem menuItem) {
            registerMenuItem(menuItem);
        }
    }

    void registerMenuItem(ZapMenuItem menuItem) {
        String id = menuItem.getIdentifier();
        if (id == null
                || id.isBlank()
                || menuItem.getText() == null
                || menuItem.getText().isBlank()) {
            LOGGER.warn(
                    "Ignoring menu without an action identifier or name: {}", menuItem.getName());
            return;
        }
        MenuAction existing = actions.get(id);
        if (existing != null) {
            if (existing.menuItem == menuItem) {
                return;
            }
            actions.remove(id);
            existing.close();
        } else if (registry.getAction(id).isPresent()) {
            LOGGER.warn("Menu identifier already registered by another action: {}", id);
            return;
        }
        MenuAction action = new MenuAction(menuItem);
        // Make the menu available to binding observers before register notifies them.
        actions.put(id, action);
        action.registration =
                registry.register(
                        new RegisteredAction(id, action, menuItem.getDefaultAccelerator()));
        menuItem.addPropertyChangeListener(action.listener);
    }

    ZapMenuItem getMenuItem(String id) {
        MenuAction action = actions.get(id);
        return action == null ? null : action.menuItem;
    }

    @Override
    public void componentAdded(ContainerEvent event) {
        observe(event.getChild());
    }

    @Override
    public void componentRemoved(ContainerEvent event) {
        unobserve(event.getChild());
    }

    private void unobserve(Component component) {
        if (component instanceof JMenu menu) {
            Container popup = menu.getPopupMenu();
            popup.removeContainerListener(this);
            containers.remove(popup);
            for (Component child : popup.getComponents()) {
                unobserve(child);
            }
        } else if (component instanceof ZapMenuItem menuItem) {
            MenuAction action = actions.get(menuItem.getIdentifier());
            if (action != null && action.menuItem == menuItem) {
                actions.remove(menuItem.getIdentifier());
                action.close();
            }
        }
    }

    @Override
    public void close() {
        containers.forEach(container -> container.removeContainerListener(this));
        containers.clear();
        List<MenuAction> removed = List.copyOf(actions.values());
        actions.clear();
        removed.forEach(MenuAction::close);
    }

    @SuppressWarnings("serial")
    private static final class MenuAction extends AbstractAction implements AutoCloseable {
        private final ZapMenuItem menuItem;
        private ActionRegistry.Registration registration;
        private final PropertyChangeListener listener;

        MenuAction(ZapMenuItem menuItem) {
            super(menuItem.getText());
            this.menuItem = menuItem;
            listener =
                    event -> {
                        switch (event.getPropertyName()) {
                            case "text" -> putValue(NAME, menuItem.getText());
                            case "enabled", "visible" ->
                                    firePropertyChange("enabled", null, isEnabled());
                            default -> {
                                // Accelerator changes are an output of binding resolution, not a
                                // new action.
                            }
                        }
                    };
        }

        @Override
        public boolean isEnabled() {
            Component component = menuItem;
            while (component != null) {
                if (component instanceof JMenuItem item
                        && (!item.isEnabled() || !item.isVisible())) {
                    return false;
                }
                component =
                        component instanceof JPopupMenu popup
                                ? popup.getInvoker()
                                : component.getParent();
            }
            return true;
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            if (isEnabled()) {
                menuItem.doClick(0);
            }
        }

        @Override
        public void close() {
            menuItem.removePropertyChangeListener(listener);
            if (registration != null) {
                registration.close();
            }
            menuItem.resetAccelerator();
        }
    }
}
