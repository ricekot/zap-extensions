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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.event.ActionEvent;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ActionRegistryUnitTest {

    private ActionRegistry registry;
    private AbstractAction action;
    private AtomicInteger invocations;
    private RegisteredAction registered;

    @BeforeEach
    void setUp() {
        registry = new ActionRegistry();
        invocations = new AtomicInteger();
        action =
                new AbstractAction("Example") {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        invocations.incrementAndGet();
                    }
                };
        registered = new RegisteredAction("example.action", action, null);
    }

    @Test
    void shouldRegisterAndInvokeActionWithoutMenuOrShortcut() throws Exception {
        SwingUtilities.invokeAndWait(
                () -> {
                    registry.register(registered);
                    assertThat(registry.getActions(), contains(registered));
                    assertThat(
                            registry.invoke(registered.id(), new ActionEvent(this, 0, "test")),
                            is(true));
                    assertThat(invocations.get(), is(1));
                });
    }

    @Test
    void shouldRecheckAvailabilityOnInvocation() throws Exception {
        SwingUtilities.invokeAndWait(
                () -> {
                    registry.register(registered);
                    action.setEnabled(false);
                    assertThat(
                            registry.invoke(registered.id(), new ActionEvent(this, 0, "test")),
                            is(false));
                    action.setEnabled(true);
                    assertThat(
                            registry.invoke(registered.id(), new ActionEvent(this, 0, "test")),
                            is(true));
                    assertThat(invocations.get(), is(1));
                });
    }

    @Test
    void shouldRejectDuplicateIdentifiers() throws Exception {
        SwingUtilities.invokeAndWait(
                () -> {
                    registry.register(registered);
                    assertThrows(
                            IllegalArgumentException.class, () -> registry.register(registered));
                    assertThat(registry.getActions(), contains(registered));
                });
    }

    @Test
    void shouldNotRemoveReplacementWhenOldHandleIsClosedAgain() throws Exception {
        SwingUtilities.invokeAndWait(
                () -> {
                    var first = registry.register(registered);
                    first.close();
                    registry.register(registered);
                    first.close();
                    assertThat(registry.getActions(), contains(registered));
                });
    }

    @Test
    void shouldNotifyRegistrationRemovalAndPropertyChanges() throws Exception {
        SwingUtilities.invokeAndWait(
                () -> {
                    AtomicInteger changes = new AtomicInteger();
                    var observer = registry.addChangeListener(changes::incrementAndGet);
                    var handle = registry.register(registered);
                    action.putValue(Action.NAME, "Renamed");
                    action.setEnabled(false);
                    handle.close();
                    assertThat(changes.get(), is(4));
                    assertThat(action.getPropertyChangeListeners().length, is(0));
                    observer.close();
                    registry.register(registered);
                    assertThat(changes.get(), is(4));
                });
    }

    @Test
    void shouldKeepDuplicateObserverHandlesIndependent() throws Exception {
        SwingUtilities.invokeAndWait(
                () -> {
                    AtomicInteger changes = new AtomicInteger();
                    Runnable listener = changes::incrementAndGet;
                    var first = registry.addChangeListener(listener);
                    var second = registry.addChangeListener(listener);
                    first.close();
                    first.close();
                    registry.register(registered);
                    assertThat(changes.get(), is(1));
                    second.close();
                    registry.clear();
                    assertThat(changes.get(), is(1));
                });
    }

    @Test
    void shouldClearActionsAndDetachPropertyListeners() throws Exception {
        SwingUtilities.invokeAndWait(
                () -> {
                    registry.register(registered);
                    registry.clear();
                    assertThat(registry.getActions(), is(empty()));
                    assertThat(action.getPropertyChangeListeners().length, is(0));
                    assertThat(
                            registry.invoke(registered.id(), new ActionEvent(this, 0, "test")),
                            is(false));
                    assertThat(invocations.get(), is(0));
                });
    }

    @Test
    void shouldReturnImmutableSnapshot() throws Exception {
        SwingUtilities.invokeAndWait(
                () -> {
                    registry.register(registered);
                    var snapshot = registry.getActions();
                    registry.clear();
                    assertThat(snapshot, contains(registered));
                    assertThrows(UnsupportedOperationException.class, snapshot::clear);
                });
    }

    @Test
    void shouldRejectOperationsOffEdt() {
        assertThrows(IllegalStateException.class, () -> registry.register(registered));
        assertThrows(IllegalStateException.class, registry::getActions);
    }

    @Test
    void shouldRejectUnstableOrUnnamedDefinitions() {
        assertThrows(IllegalArgumentException.class, () -> new RegisteredAction(" ", action, null));
        action.putValue(Action.NAME, "");
        assertThrows(
                IllegalArgumentException.class,
                () -> new RegisteredAction("example", action, null));
    }
}
