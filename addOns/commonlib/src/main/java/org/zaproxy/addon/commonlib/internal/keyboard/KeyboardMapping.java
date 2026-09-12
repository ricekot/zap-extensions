/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2014 The ZAP Development Team
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

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import org.parosproxy.paros.Constant;

/** Legacy shortcut display/entry format, independent of menu items. */
final class KeyboardMapping {

    private KeyboardMapping() {}

    static String keyString(int keyCode) {
        if (keyCode >= KeyEvent.VK_F1 && keyCode <= KeyEvent.VK_F12) {
            return "F" + (keyCode - KeyEvent.VK_F1 + 1);
        }
        return switch (keyCode) {
            case KeyEvent.VK_UP -> Constant.messages.getString("keyboard.key.up");
            case KeyEvent.VK_DOWN -> Constant.messages.getString("keyboard.key.down");
            case KeyEvent.VK_LEFT -> Constant.messages.getString("keyboard.key.left");
            case KeyEvent.VK_RIGHT -> Constant.messages.getString("keyboard.key.right");
            default -> String.valueOf((char) keyCode).toUpperCase();
        };
    }

    static char keyCode(String key) {
        if (key.length() == 1) {
            return key.charAt(0);
        }
        if (key.startsWith("F")) {
            return (char) (KeyEvent.VK_F1 + Integer.parseInt(key.substring(1)) - 1);
        }
        if (key.equals(Constant.messages.getString("keyboard.key.up"))) {
            return KeyEvent.VK_UP;
        }
        if (key.equals(Constant.messages.getString("keyboard.key.down"))) {
            return KeyEvent.VK_DOWN;
        }
        if (key.equals(Constant.messages.getString("keyboard.key.left"))) {
            return KeyEvent.VK_LEFT;
        }
        if (key.equals(Constant.messages.getString("keyboard.key.right"))) {
            return KeyEvent.VK_RIGHT;
        }
        return 0;
    }

    static String modifiersString(int modifiers) {
        StringBuilder result = new StringBuilder();
        if ((modifiers & InputEvent.CTRL_DOWN_MASK) != 0) {
            result.append(Constant.messages.getString("keyboard.key.control")).append(' ');
        }
        if ((modifiers & InputEvent.ALT_DOWN_MASK) != 0) {
            result.append(Constant.messages.getString("keyboard.key.alt")).append(' ');
        }
        if ((modifiers & InputEvent.SHIFT_DOWN_MASK) != 0) {
            result.append(Constant.messages.getString("keyboard.key.shift")).append(' ');
        }
        return result.toString();
    }
}
