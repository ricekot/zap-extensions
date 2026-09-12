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
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Map;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import org.zaproxy.addon.commonlib.actions.ActionRegistry;
import org.zaproxy.addon.commonlib.actions.RegisteredAction;
import org.zaproxy.zap.view.ZapMenuItem;

class WindowActionBindingsUnitTest {

    @Test
    void shouldInvokeMenuLessActionAndRestorePreviousWindowBinding() throws Exception {
        SwingUtilities.invokeAndWait(
                () -> {
                    ActionRegistry registry = new ActionRegistry();
                    Action action = mock(Action.class);
                    when(action.getValue(Action.NAME)).thenReturn("Example");
                    when(action.isEnabled()).thenReturn(true);
                    registry.register(new RegisteredAction("example", action, null));
                    JRootPane root = new JRootPane();
                    KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0);
                    Object previous = new Object();
                    var input = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
                    input.put(key, previous);
                    WindowActionBindings bindings = new WindowActionBindings(root, registry);
                    bindings.update(Map.of("example", key), new MenuActionAdapter(registry));
                    Object installed = input.get(key);
                    ActionEvent event = new ActionEvent(root, 0, "test");
                    root.getActionMap().get(installed).actionPerformed(event);
                    verify(action).actionPerformed(event);
                    bindings.close();
                    assertThat(input.get(key), is(sameInstance(previous)));
                    assertThat(root.getActionMap().get(installed), is(nullValue()));
                });
    }

    @Test
    void shouldNotInstallSecondBindingForMenuAction() throws Exception {
        SwingUtilities.invokeAndWait(
                () -> {
                    ActionRegistry registry = new ActionRegistry();
                    MenuActionAdapter menus = new MenuActionAdapter(registry);
                    KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0);
                    menus.registerMenuItem(new ZapMenuItem("example", "Example", key));
                    JRootPane root = new JRootPane();
                    WindowActionBindings bindings = new WindowActionBindings(root, registry);
                    bindings.update(Map.of("example", key), menus);
                    assertThat(
                            root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).get(key),
                            is(nullValue()));
                    bindings.close();
                    menus.close();
                });
    }

    @Test
    void shouldNotOverwriteBindingInstalledByAnotherOwnerDuringCleanup() throws Exception {
        SwingUtilities.invokeAndWait(
                () -> {
                    ActionRegistry registry = new ActionRegistry();
                    JRootPane root = new JRootPane();
                    KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0);
                    WindowActionBindings bindings = new WindowActionBindings(root, registry);
                    bindings.update(Map.of("example", key), new MenuActionAdapter(registry));
                    Object replacement = new Object();
                    var input = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
                    input.put(key, replacement);
                    bindings.close();
                    assertThat(input.get(key), is(sameInstance(replacement)));
                });
    }
}
