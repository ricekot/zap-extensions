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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.common.AbstractParam;
import org.parosproxy.paros.extension.ViewDelegate;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.OptionsParam;
import org.zaproxy.addon.commonlib.actions.ActionRegistry;
import org.zaproxy.addon.commonlib.actions.RegisteredAction;
import org.zaproxy.zap.extension.api.API;
import org.zaproxy.zap.utils.I18N;
import org.zaproxy.zap.utils.ZapXmlConfiguration;
import org.zaproxy.zap.view.ZapMenuItem;

class KeyboardShortcutsUnitTest {

    private I18N previousMessages;
    private KeyboardShortcuts shortcuts;
    private ActionRegistry registry;
    private ZapXmlConfiguration configuration;
    private JMenu menu;
    private JRootPane root;

    @BeforeEach
    void setUp() throws Exception {
        previousMessages = Constant.messages;
        Constant.messages = mock(I18N.class);
        when(Constant.messages.getString(anyString()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        configuration = new ZapXmlConfiguration();
        Model model = mock(Model.class);
        OptionsParam options = mock(OptionsParam.class);
        when(model.getOptionsParam()).thenReturn(options);
        when(options.getConfig()).thenReturn(configuration);
        doAnswer(
                        invocation -> {
                            ((AbstractParam) invocation.getArgument(0)).load(configuration);
                            return null;
                        })
                .when(options)
                .addParamSet(any());
        ViewDelegate view = mock(ViewDelegate.class, RETURNS_DEEP_STUBS);
        registry = new ActionRegistry();
        SwingUtilities.invokeAndWait(
                () -> {
                    menu = new JMenu("Test");
                    root = new JRootPane();
                    when(view.getMainFrame().getRootPane()).thenReturn(root);
                    when(view.getMainFrame().getMainMenuBar().getComponents())
                            .thenReturn(new Component[] {menu});
                    shortcuts = new KeyboardShortcuts(model, view, registry);
                });
    }

    @AfterEach
    void cleanUp() throws Exception {
        try {
            SwingUtilities.invokeAndWait(() -> shortcuts.stop());
        } finally {
            Constant.messages = previousMessages;
        }
    }

    @Test
    void shouldLoadSavedBindingForMenuAddedAfterStartup() throws Exception {
        configuration.setProperty("keyboard.shortcuts(0).menu", "late");
        configuration.setProperty("keyboard.shortcuts(0).keycode", KeyEvent.VK_F6);
        configuration.setProperty("keyboard.shortcuts(0).modifiers", 0);
        SwingUtilities.invokeAndWait(
                () -> {
                    shortcuts.start();
                    ZapMenuItem item = new ZapMenuItem("late", "Late", null);
                    menu.add(item);
                    assertThat(
                            item.getAccelerator(), is(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0)));
                    assertThat(shortcuts.getShortcutRows(false), hasSize(1));
                    menu.remove(item);
                    assertThat(shortcuts.getShortcutRows(false), hasSize(0));
                    menu.add(item);
                    assertThat(
                            item.getAccelerator(), is(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0)));
                });
    }

    @Test
    void shouldBindMenuLessActionAndRemoveWindowBindingOnUnregister() throws Exception {
        SwingUtilities.invokeAndWait(
                () -> {
                    shortcuts.start();
                    AtomicInteger invoked = new AtomicInteger();
                    AbstractAction action =
                            new AbstractAction("Menu-less") {
                                @Override
                                public void actionPerformed(ActionEvent event) {
                                    invoked.incrementAndGet();
                                }
                            };
                    var registration =
                            registry.register(new RegisteredAction("menuless", action, null));
                    KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0);
                    shortcuts.setShortcut("menuless", key);
                    var input = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
                    Object actionKey = input.get(key);
                    root.getActionMap()
                            .get(actionKey)
                            .actionPerformed(new ActionEvent(root, 0, "test"));
                    assertThat(invoked.get(), is(1));
                    assertThat(shortcuts.getShortcutRows(false), hasSize(1));
                    registration.close();
                    assertThat(input.get(key), is(nullValue()));
                    assertThat(root.getActionMap().get(actionKey), is(nullValue()));
                });
    }

    @Test
    void shouldRestoreMenuDefaultsOnStopAndReloadLatestOverridesOnRestart() throws Exception {
        SwingUtilities.invokeAndWait(
                () -> {
                    KeyStroke defaultKey = KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0);
                    KeyStroke customKey = KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0);
                    ZapMenuItem item = new ZapMenuItem("example", "Example", defaultKey);
                    menu.add(item);
                    shortcuts.start();
                    shortcuts.setShortcut("example", customKey);
                    shortcuts.stop();
                    assertThat(item.getAccelerator(), is(defaultKey));
                    assertThat(registry.getActions(), hasSize(0));
                    assertThat(
                            API.getInstance().getImplementors().get("keyboard"), is(nullValue()));
                    shortcuts.start();
                    assertThat(item.getAccelerator(), is(customKey));
                    assertThat(registry.getActions(), hasSize(1));
                    assertThat(
                            API.getInstance().getImplementors().get("keyboard")
                                    instanceof KeyboardAPI,
                            is(true));
                });
    }

    @Test
    void shouldDiscardUnsavedOptionEditsOnReopen() throws Exception {
        SwingUtilities.invokeAndWait(
                () -> {
                    KeyStroke defaultKey = KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0);
                    menu.add(new ZapMenuItem("example", "Example", defaultKey));
                    shortcuts.start();
                    OptionsKeyboardShortcutPanel panel =
                            new OptionsKeyboardShortcutPanel(shortcuts);
                    panel.initParam(null);
                    panel.getShortcuts()
                            .get(0)
                            .setKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));
                    panel.initParam(null);
                    assertThat(panel.getShortcuts().get(0).getKeyStroke(), is(defaultKey));
                    assertThat(shortcuts.getShortcut("example"), is(defaultKey));
                });
    }

    @Test
    void shouldDiscardCancelledResetButApplySavedReset() throws Exception {
        SwingUtilities.invokeAndWait(
                () -> {
                    KeyStroke defaultKey = KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0);
                    KeyStroke customKey = KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0);
                    menu.add(new ZapMenuItem("example", "Example", defaultKey));
                    shortcuts.start();
                    shortcuts.setShortcut("example", customKey);
                    OptionsKeyboardShortcutPanel panel =
                            new OptionsKeyboardShortcutPanel(shortcuts);
                    panel.initParam(null);
                    JButton reset =
                            Arrays.stream(panel.getComponents())
                                    .filter(JButton.class::isInstance)
                                    .map(JButton.class::cast)
                                    .filter(
                                            button ->
                                                    "keyboard.options.button.reset"
                                                            .equals(button.getText()))
                                    .findFirst()
                                    .orElseThrow();
                    reset.doClick(0);
                    panel.initParam(null);
                    assertDoesNotThrow(() -> panel.saveParam(null));
                    assertThat(shortcuts.getShortcut("example"), is(customKey));
                    reset.doClick(0);
                    assertDoesNotThrow(() -> panel.saveParam(null));
                    assertThat(shortcuts.getShortcut("example"), is(defaultKey));
                    KeyboardParam saved = new KeyboardParam();
                    saved.load(configuration);
                    assertThat(saved.hasShortcut("example"), is(false));
                });
    }

    @Test
    void shouldIgnoreLateLegacyCallbacksAfterStop() throws Exception {
        SwingUtilities.invokeAndWait(
                () -> {
                    menu.add(new ZapMenuItem("example", "Example", null));
                    shortcuts.start();
                    shortcuts.stop();
                });
        // A caller can have captured the old provider before it was removed from core.
        shortcuts.registerMenuItem(new ZapMenuItem("late", "Late", null));
        shortcuts.setShortcut("example", KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0));
        assertThat(shortcuts.getShortcuts(false), hasSize(0));
        SwingUtilities.invokeAndWait(() -> assertThat(registry.getActions(), hasSize(0)));
    }

    @Test
    void shouldSupportLegacyAccessFromOutsideEdt() throws Exception {
        SwingUtilities.invokeAndWait(
                () -> {
                    menu.add(new ZapMenuItem("example", "Example", null));
                    shortcuts.start();
                });
        KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0);
        shortcuts.setShortcut("example", key);
        assertThat(shortcuts.getShortcut("example"), is(key));
        assertThat(shortcuts.getShortcuts(false), hasSize(1));
        assertThat(shortcuts.getShortcuts(false).get(0).getKeyStroke(), is(key));
    }
}
