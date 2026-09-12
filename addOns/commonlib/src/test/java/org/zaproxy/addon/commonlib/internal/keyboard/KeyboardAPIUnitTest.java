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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.KeyStroke;
import net.sf.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.utils.I18N;

class KeyboardAPIUnitTest {

    private I18N previousMessages;
    private KeyboardShortcuts shortcuts;
    private KeyboardAPI api;

    @BeforeEach
    void setUp() {
        previousMessages = Constant.messages;
        Constant.messages = mock(I18N.class);
        when(Constant.messages.getString("keyboard.api.cheatsheet.header"))
                .thenReturn("<html>操作<table>");
        when(Constant.messages.getString("keyboard.api.cheatsheet.footer"))
                .thenReturn("</table></html>");
        when(Constant.messages.getString(
                        eq("keyboard.api.cheatsheet.tablerow"), any(), any(), any()))
                .thenAnswer(
                        invocation ->
                                "<tr><td>"
                                        + invocation.getArgument(1)
                                        + "</td><td>"
                                        + invocation.getArgument(2)
                                        + "</td><td>"
                                        + invocation.getArgument(3)
                                        + "</td></tr>");
        shortcuts = mock(KeyboardShortcuts.class);
        api = new KeyboardAPI(shortcuts);
    }

    @AfterEach
    void cleanUp() {
        Constant.messages = previousMessages;
    }

    @Test
    void shouldKeepActionOrderedEndpointAndExcludeUnsetByDefault() throws Exception {
        when(shortcuts.getShortcutRows(false))
                .thenReturn(
                        new ArrayList<>(
                                List.of(
                                        new KeyboardShortcut(
                                                "z",
                                                "Zulu",
                                                KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0)),
                                        new KeyboardShortcut("unset", "Unset", null),
                                        new KeyboardShortcut(
                                                "a",
                                                "Alpha",
                                                KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0)))));
        HttpMessage message =
                api.handleApiOther(new HttpMessage(), "cheatsheetActionOrder", new JSONObject());
        String body = message.getResponseBody().toString();
        assertThat(api.getPrefix(), is("keyboard"));
        assertThat(body.indexOf("Alpha") < body.indexOf("Zulu"), is(true));
        assertThat(body, not(containsString("Unset")));
        assertThat(
                message.getResponseHeader().getHeader("Content-Length"),
                is(Integer.toString(message.getResponseBody().length())));
    }

    @Test
    void shouldKeepKeyOrderedEndpointAndIncludeUnsetWhenRequested() throws Exception {
        when(shortcuts.getShortcutRows(false))
                .thenReturn(
                        new ArrayList<>(
                                List.of(
                                        new KeyboardShortcut(
                                                "later",
                                                "Later",
                                                KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0)),
                                        new KeyboardShortcut(
                                                "earlier",
                                                "Earlier",
                                                KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0)),
                                        new KeyboardShortcut("unset", "Unset", null))));
        JSONObject parameters = new JSONObject();
        parameters.put("incUnset", true);
        HttpMessage message =
                api.handleApiOther(new HttpMessage(), "cheatsheetKeyOrder", parameters);
        String body = message.getResponseBody().toString();
        assertThat(body, containsString("Unset"));
        assertThat(body.indexOf("Earlier") < body.indexOf("Later"), is(true));
    }

    @Test
    void shouldEscapeActionNamesInHtml() throws Exception {
        when(shortcuts.getShortcutRows(false))
                .thenReturn(
                        new ArrayList<>(
                                List.of(
                                        new KeyboardShortcut(
                                                "example",
                                                "<script>alert(1)</script>",
                                                KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0)))));
        HttpMessage message =
                api.handleApiOther(new HttpMessage(), "cheatsheetActionOrder", new JSONObject());
        assertThat(message.getResponseBody().toString(), containsString("&lt;script&gt;"));
        assertThat(message.getResponseBody().toString(), not(containsString("<script>")));
    }
}
