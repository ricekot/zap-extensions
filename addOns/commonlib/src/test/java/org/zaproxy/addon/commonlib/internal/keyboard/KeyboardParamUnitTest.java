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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

/**
 * Regression tests for the keyboard configuration format shared with the replacement
 * implementation.
 */
class KeyboardParamUnitTest {

    private KeyboardParam param;
    private ZapXmlConfiguration configuration;

    @BeforeEach
    void setUp() {
        param = new KeyboardParam();
        configuration = new ZapXmlConfiguration();
        param.load(configuration);
    }

    @Test
    void shouldLeaveUnconfiguredShortcutAbsent() {
        assertThat(param.getShortcut("menu.example"), is(nullValue()));
    }

    @Test
    void shouldLoadExistingShortcutFormat() {
        // Given
        configuration.setProperty("keyboard.shortcuts(0).menu", "menu.example");
        configuration.setProperty("keyboard.shortcuts(0).keycode", KeyEvent.VK_E);
        configuration.setProperty("keyboard.shortcuts(0).modifiers", InputEvent.CTRL_DOWN_MASK);
        // When
        param.load(configuration);
        // Then
        assertThat(
                param.getShortcut("menu.example"),
                is(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK)));
    }

    @Test
    void shouldLoadExplicitlyClearedShortcut() {
        // Given
        configuration.setProperty("keyboard.shortcuts(0).menu", "menu.example");
        configuration.setProperty("keyboard.shortcuts(0).keycode", 0);
        configuration.setProperty("keyboard.shortcuts(0).modifiers", 0);
        // When
        param.load(configuration);
        // Then
        assertThat(param.getShortcut("menu.example"), is(KeyStroke.getKeyStroke(0, 0)));
    }

    @Test
    void shouldSaveShortcutUsingExistingFormat() {
        // Given
        KeyStroke shortcut =
                KeyStroke.getKeyStroke(
                        KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK);
        param.setShortcut("menu.example", shortcut);
        // When
        param.setConfigs();
        // Then
        assertThat(configuration.getString("keyboard.shortcuts.menu"), is("menu.example"));
        assertThat(configuration.getInt("keyboard.shortcuts.keycode"), is(shortcut.getKeyCode()));
        assertThat(
                configuration.getInt("keyboard.shortcuts.modifiers"), is(shortcut.getModifiers()));
        KeyboardParam reloaded = new KeyboardParam();
        reloaded.load(configuration);
        assertThat(reloaded.getShortcut("menu.example"), is(equalTo(shortcut)));
    }

    @Test
    void shouldPersistExplicitlyClearedShortcutRatherThanRemoveOverride() {
        // Given
        param.setShortcut("menu.example", null);
        // When
        param.setConfigs();
        // Then
        assertThat(configuration.getString("keyboard.shortcuts.menu"), is("menu.example"));
        assertThat(configuration.getInt("keyboard.shortcuts.keycode"), is(0));
        assertThat(configuration.getInt("keyboard.shortcuts.modifiers"), is(0));
        KeyboardParam reloaded = new KeyboardParam();
        reloaded.load(configuration);
        assertThat(reloaded.getShortcut("menu.example"), is(KeyStroke.getKeyStroke(0, 0)));
    }

    @Test
    void shouldRetainOverridesForUnavailableActionsWhenSavingOtherBindings() {
        // Given
        KeyStroke unavailableShortcut = KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0);
        configuration.setProperty("keyboard.shortcuts(0).menu", "unavailable.action");
        configuration.setProperty(
                "keyboard.shortcuts(0).keycode", unavailableShortcut.getKeyCode());
        configuration.setProperty("keyboard.shortcuts(0).modifiers", 0);
        param.load(configuration);
        KeyStroke shortcut = KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0);
        param.setShortcut("menu.example", shortcut);
        // When
        param.setConfigs();
        // Then
        KeyboardParam reloaded = new KeyboardParam();
        reloaded.load(configuration);
        assertThat(reloaded.getShortcut("unavailable.action"), is(unavailableShortcut));
        assertThat(reloaded.getShortcut("menu.example"), is(shortcut));
    }

    @Test
    void shouldReplacePersistedEntriesWithoutDuplicatingThemOrChangingOtherSettings() {
        // Given
        configuration.setProperty("other.setting", "preserved");
        param.setShortcut("menu.example", KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0));
        param.setConfigs();
        KeyStroke replacement = KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0);
        param.setShortcut("menu.example", replacement);
        // When
        param.setConfigs();
        // Then
        assertThat(configuration.configurationsAt("keyboard.shortcuts"), hasSize(1));
        assertThat(
                configuration.getInt("keyboard.shortcuts.keycode"), is(replacement.getKeyCode()));
        assertThat(configuration.getString("other.setting"), is("preserved"));
    }
}
