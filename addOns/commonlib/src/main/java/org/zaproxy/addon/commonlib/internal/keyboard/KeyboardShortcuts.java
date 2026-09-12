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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.extension.ViewDelegate;
import org.parosproxy.paros.model.Model;
import org.zaproxy.addon.commonlib.actions.ActionRegistry;
import org.zaproxy.zap.extension.api.API;
import org.zaproxy.zap.extension.keyboard.KeyboardShortcutProvider;
import org.zaproxy.zap.utils.DesktopUtils;
import org.zaproxy.zap.view.ZapMenuItem;

/** Owns the keyboard UI and bindings; the core interface is only a compatibility adapter. */
public final class KeyboardShortcuts implements KeyboardShortcutProvider {

    private static final Logger LOGGER = LogManager.getLogger(KeyboardShortcuts.class);

    private final Model model;
    private final ViewDelegate view;
    private final ActionRegistry registry;
    private final KeyboardParam parameters = new KeyboardParam();
    private final ShortcutBindings bindings;
    private final MenuActionAdapter menus;
    private final KeyboardAPI api;
    private OptionsKeyboardShortcutPanel panel;
    private WindowActionBindings windowBindings;
    private ActionRegistry.Registration observer;
    private boolean parametersInstalled;

    public KeyboardShortcuts(Model model, ViewDelegate view, ActionRegistry registry) {
        this.model = model;
        this.view = view;
        this.registry = registry;
        bindings = new ShortcutBindings(registry, parameters);
        menus = new MenuActionAdapter(registry);
        api = new KeyboardAPI(this);
    }

    @Override
    public void start() {
        if (parametersInstalled) {
            throw new IllegalStateException("Keyboard shortcuts are already started.");
        }
        model.getOptionsParam().addParamSet(parameters);
        parametersInstalled = true;
        windowBindings = new WindowActionBindings(view.getMainFrame().getRootPane(), registry);
        observer = registry.addChangeListener(this::applyBindings);
        menus.start(view.getMainFrame().getMainMenuBar());
        applyBindings();
        panel = new OptionsKeyboardShortcutPanel(this);
        view.getOptionsDialog().addParamPanel(new String[0], panel, true);
        API.getInstance().registerApiImplementor(api);
    }

    @Override
    public void stop() {
        boolean installed = parametersInstalled;
        if (installed) {
            parameters.setConfigs();
        }
        parametersInstalled = false;
        if (observer != null) {
            observer.close();
            observer = null;
        }
        menus.close();
        if (windowBindings != null) {
            windowBindings.close();
            windowBindings = null;
        }
        if (panel != null) {
            view.getOptionsDialog().removeParamPanel(panel);
            panel = null;
        }
        if (API.getInstance().getImplementors().get(api.getPrefix()) == api) {
            API.getInstance().removeApiImplementor(api);
        }
        if (installed) {
            model.getOptionsParam().removeParamSet(parameters);
        }
    }

    private void applyBindings() {
        var shortcuts = bindings.getShortcuts(false);
        shortcuts.forEach(
                (id, key) -> {
                    ZapMenuItem menu = menus.getMenuItem(id);
                    if (menu != null) {
                        menu.setAccelerator(key);
                    }
                });
        windowBindings.update(shortcuts, menus);
    }

    @Override
    public void registerMenuItem(ZapMenuItem menuItem) {
        onEdt(
                () -> {
                    if (parametersInstalled) {
                        menus.registerMenuItem(menuItem);
                    }
                    return null;
                });
    }

    @Override
    public KeyStroke getShortcut(String identifier) {
        return onEdt(
                () -> parametersInstalled ? bindings.getShortcuts(false).get(identifier) : null);
    }

    @Override
    public void setShortcut(String identifier, KeyStroke shortcut) {
        onEdt(
                () -> {
                    if (!parametersInstalled) {
                        return null;
                    }
                    bindings.setShortcut(identifier, shortcut);
                    parameters.setConfigs();
                    applyBindings();
                    return null;
                });
    }

    void saveShortcuts(List<KeyboardShortcut> shortcuts, boolean reset) {
        if (!parametersInstalled) {
            return;
        }
        bindings.save(shortcuts, reset);
        applyBindings();
    }

    List<KeyboardShortcut> getShortcutRows(boolean defaults) {
        return onEdt(
                () -> {
                    if (!parametersInstalled) {
                        return new ArrayList<KeyboardShortcut>();
                    }
                    var shortcuts = bindings.getShortcuts(defaults);
                    return registry.getActions().stream()
                            .map(
                                    action ->
                                            new KeyboardShortcut(
                                                    action.id(),
                                                    String.valueOf(
                                                            action.action().getValue(Action.NAME)),
                                                    shortcuts.get(action.id())))
                            .collect(Collectors.toCollection(ArrayList::new));
                });
    }

    @Override
    public List<org.zaproxy.zap.extension.keyboard.KeyboardShortcut> getShortcuts(
            boolean defaults) {
        return getShortcutRows(defaults).stream()
                .map(
                        shortcut ->
                                new org.zaproxy.zap.extension.keyboard.KeyboardShortcut(
                                        shortcut.getIdentifier(),
                                        shortcut.getName(),
                                        shortcut.getKeyStroke()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    void displayCheatsheetSortedByAction() {
        try {
            DesktopUtils.openUrlInBrowser(api.getCheatSheetActionURI());
        } catch (Exception e) {
            LOGGER.error("Unable to open the keyboard cheatsheet", e);
        }
    }

    void displayCheatsheetSortedByKey() {
        try {
            DesktopUtils.openUrlInBrowser(api.getCheatSheetKeyURI());
        } catch (Exception e) {
            LOGGER.error("Unable to open the keyboard cheatsheet", e);
        }
    }

    private static <T> T onEdt(Supplier<T> operation) {
        if (SwingUtilities.isEventDispatchThread()) {
            return operation.get();
        }
        FutureTask<T> task = new FutureTask<>(operation::get);
        try {
            SwingUtilities.invokeAndWait(task);
            return task.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while accessing keyboard shortcuts", e);
        } catch (InvocationTargetException | ExecutionException e) {
            throw new IllegalStateException("Unable to access keyboard shortcuts", e.getCause());
        }
    }
}
