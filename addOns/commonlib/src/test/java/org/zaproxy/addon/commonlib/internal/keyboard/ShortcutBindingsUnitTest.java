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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zaproxy.addon.commonlib.actions.ActionRegistry;
import org.zaproxy.addon.commonlib.actions.RegisteredAction;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

class ShortcutBindingsUnitTest {

    private static final KeyStroke KEY = KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0);
    private static final KeyStroke OTHER_KEY = KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0);
    private ActionRegistry registry;
    private KeyboardParam parameters;
    private ShortcutBindings bindings;

    @BeforeEach
    void setUp() {
        registry = new ActionRegistry();
        parameters = new KeyboardParam();
        parameters.load(new ZapXmlConfiguration());
        bindings = new ShortcutBindings(registry, parameters);
    }

    private void register(String id, KeyStroke key) {
        Action action = mock(Action.class);
        when(action.getValue(Action.NAME)).thenReturn(id);
        registry.register(new RegisteredAction(id, action, key));
    }

    @Test
    void shouldSuppressDuplicateDefaultsInRegistrationOrder() throws Exception {
        SwingUtilities.invokeAndWait(
                () -> {
                    register("first", KEY);
                    register("second", KEY);
                    assertThat(bindings.getShortcuts(true).get("first"), is(KEY));
                    assertThat(bindings.getShortcuts(true).get("second"), is(nullValue()));
                });
    }

    @Test
    void shouldPreferExplicitOverrideToEarlierDefault() throws Exception {
        SwingUtilities.invokeAndWait(
                () -> {
                    register("default", KEY);
                    register("custom", null);
                    parameters.setShortcut("custom", KEY);
                    assertThat(bindings.getShortcuts(false).get("default"), is(nullValue()));
                    assertThat(bindings.getShortcuts(false).get("custom"), is(KEY));
                });
    }

    @Test
    void shouldDistinguishClearedFromDefaultAfterReload() throws Exception {
        SwingUtilities.invokeAndWait(
                () -> {
                    register("default", KEY);
                    parameters.setShortcut("default", null);
                    parameters.setConfigs();
                    parameters.load(parameters.getConfig());
                    assertThat(bindings.getShortcuts(false).get("default"), is(nullValue()));
                    assertThat(bindings.getShortcuts(true).get("default"), is(KEY));
                });
    }

    @Test
    void shouldReassignShortcutAndPersistClearedSource() throws Exception {
        SwingUtilities.invokeAndWait(
                () -> {
                    register("source", KEY);
                    register("target", null);
                    bindings.setShortcut("target", KEY);
                    bindings.save(List.of(), false);
                    parameters.load(parameters.getConfig());
                    assertThat(bindings.getShortcuts(false).get("source"), is(nullValue()));
                    assertThat(bindings.getShortcuts(false).get("target"), is(KEY));
                });
    }

    @Test
    void shouldResetRegisteredActionsButRetainUnavailableOverrides() throws Exception {
        SwingUtilities.invokeAndWait(
                () -> {
                    register("example", KEY);
                    parameters.setShortcut("example", OTHER_KEY);
                    parameters.setShortcut("unavailable", OTHER_KEY);
                    bindings.save(List.of(new KeyboardShortcut("example", "Example", KEY)), true);
                    assertThat(parameters.hasShortcut("example"), is(false));
                    assertThat(parameters.getShortcut("unavailable"), is(OTHER_KEY));
                    assertThat(bindings.getShortcuts(false).get("example"), is(KEY));
                });
    }

    @Test
    void shouldSaveEditsMadeAfterResetWithoutStoringUnchangedDefaults() throws Exception {
        SwingUtilities.invokeAndWait(
                () -> {
                    register("example", KEY);
                    register("unchanged", null);
                    KeyboardShortcut row = new KeyboardShortcut("example", "Example", KEY);
                    row.setKeyStroke(OTHER_KEY);
                    bindings.save(List.of(row), true);
                    assertThat(parameters.getShortcut("example"), is(OTHER_KEY));
                    assertThat(parameters.hasShortcut("unchanged"), is(false));
                });
    }

    @Test
    void shouldNotChangeBindingsUntilOptionsAreSaved() throws Exception {
        SwingUtilities.invokeAndWait(
                () -> {
                    register("example", KEY);
                    KeyboardShortcut row = new KeyboardShortcut("example", "Example", KEY);
                    row.setKeyStroke(OTHER_KEY);
                    assertThat(bindings.getShortcuts(false).get("example"), is(KEY));
                    bindings.save(List.of(row), false);
                    assertThat(bindings.getShortcuts(false).get("example"), is(OTHER_KEY));
                });
    }
}
