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
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import org.zaproxy.addon.commonlib.actions.ActionRegistry;
import org.zaproxy.zap.view.ZapMenuItem;

class MenuActionAdapterUnitTest {

    @Test
    void shouldDiscoverNestedAndHotAddedMenusAndRemoveTheirActions() throws Exception {
        SwingUtilities.invokeAndWait(
                () -> {
                    ActionRegistry registry = new ActionRegistry();
                    MenuActionAdapter adapter = new MenuActionAdapter(registry);
                    JMenuBar bar = new JMenuBar();
                    JMenu parent = new JMenu("Parent");
                    bar.add(parent);
                    parent.add(new ZapMenuItem("initial", "Initial", null));
                    adapter.start(bar);
                    assertThat(registry.getActions(), hasSize(1));
                    JMenu nested = new JMenu("Nested");
                    parent.add(nested);
                    nested.add(new ZapMenuItem("late", "Late", null));
                    assertThat(registry.getActions(), hasSize(2));
                    parent.remove(nested);
                    assertThat(registry.getAction("late").isEmpty(), is(true));
                    nested.add(new ZapMenuItem("detached", "Detached", null));
                    assertThat(registry.getActions(), hasSize(1));
                    adapter.close();
                    assertThat(registry.getActions(), is(empty()));
                });
    }

    @Test
    void shouldReplaceRecreatedMenuWithoutRetainingOldCallback() throws Exception {
        SwingUtilities.invokeAndWait(
                () -> {
                    ActionRegistry registry = new ActionRegistry();
                    MenuActionAdapter adapter = new MenuActionAdapter(registry);
                    AtomicInteger originalCalls = new AtomicInteger();
                    AtomicInteger replacementCalls = new AtomicInteger();
                    ZapMenuItem original = new ZapMenuItem("tab", "Original", null);
                    original.addActionListener(event -> originalCalls.incrementAndGet());
                    ZapMenuItem replacement = new ZapMenuItem("tab", "Replacement", null);
                    replacement.addActionListener(event -> replacementCalls.incrementAndGet());
                    adapter.registerMenuItem(original);
                    adapter.registerMenuItem(replacement);
                    adapter.registerMenuItem(replacement);
                    registry.invoke("tab", new ActionEvent(this, 0, "test"));
                    assertThat(registry.getActions(), hasSize(1));
                    assertThat(originalCalls.get(), is(0));
                    assertThat(replacementCalls.get(), is(1));
                    adapter.close();
                });
    }

    @Test
    void shouldRespectMenuAndAncestorAvailabilityAndReflectRenames() throws Exception {
        SwingUtilities.invokeAndWait(
                () -> {
                    ActionRegistry registry = new ActionRegistry();
                    MenuActionAdapter adapter = new MenuActionAdapter(registry);
                    JMenu parent = new JMenu("Parent");
                    ZapMenuItem item = new ZapMenuItem("example", "Example", null);
                    parent.add(item);
                    adapter.registerMenuItem(item);
                    ActionEvent event = new ActionEvent(this, 0, "test");
                    parent.setEnabled(false);
                    assertThat(registry.invoke("example", event), is(false));
                    parent.setEnabled(true);
                    item.setEnabled(false);
                    assertThat(registry.invoke("example", event), is(false));
                    item.setEnabled(true);
                    item.setVisible(false);
                    assertThat(registry.invoke("example", event), is(false));
                    item.setVisible(true);
                    item.setText("Renamed");
                    assertThat(
                            registry.getAction("example")
                                    .orElseThrow()
                                    .action()
                                    .getValue(Action.NAME),
                            is("Renamed"));
                    assertThat(registry.invoke("example", event), is(true));
                    adapter.close();
                });
    }

    @Test
    void shouldRestoreDefaultsAndDetachListenersOnClose() throws Exception {
        SwingUtilities.invokeAndWait(
                () -> {
                    ActionRegistry registry = new ActionRegistry();
                    MenuActionAdapter adapter = new MenuActionAdapter(registry);
                    JMenuBar bar = new JMenuBar();
                    JMenu parent = new JMenu("Parent");
                    bar.add(parent);
                    KeyStroke original = KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0);
                    ZapMenuItem item = new ZapMenuItem("example", "Example", original);
                    parent.add(item);
                    int listeners = item.getPropertyChangeListeners().length;
                    int containerListeners = parent.getPopupMenu().getContainerListeners().length;
                    adapter.start(bar);
                    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));
                    adapter.close();
                    assertThat(item.getAccelerator(), is(original));
                    assertThat(item.getPropertyChangeListeners().length, is(listeners));
                    assertThat(
                            parent.getPopupMenu().getContainerListeners().length,
                            is(containerListeners));
                    parent.add(new ZapMenuItem("late", "Late", null));
                    assertThat(registry.getActions(), is(empty()));
                });
    }
}
