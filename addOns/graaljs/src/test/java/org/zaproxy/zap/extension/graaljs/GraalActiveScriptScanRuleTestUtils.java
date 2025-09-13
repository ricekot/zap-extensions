/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2025 The ZAP Development Team
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
package org.zaproxy.zap.extension.graaljs;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.model.Model;
import org.zaproxy.addon.commonlib.scanrules.ScanRuleMetadata;
import org.zaproxy.addon.commonlib.scanrules.ScanRuleMetadataProvider;
import org.zaproxy.zap.extension.script.ScriptWrapper;
import org.zaproxy.zap.extension.scripts.scanrules.ActiveScriptScanRule;
import org.zaproxy.zap.testutils.ActiveScannerTestUtils;

public abstract class GraalActiveScriptScanRuleTestUtils
        extends ActiveScannerTestUtils<ActiveScriptScanRule> {

    protected abstract Path getScriptPath();

    @Override
    protected void setUpMessages() {
        mockMessages(new ExtensionGraalJs());
    }

    @Override
    protected ActiveScriptScanRule createScanner() {
        ScriptEngine scriptEngine = parseScript(getScriptPath());
        var metadataProvider =
                ((Invocable) scriptEngine).getInterface(ScanRuleMetadataProvider.class);
        ScanRuleMetadata metadata = metadataProvider.getMetadata();
        var scriptWrapper = new ScriptWrapper();
        scriptWrapper.setFile(getScriptPath().toFile());
        return new ActiveScriptScanRule(scriptWrapper, metadata);
    }

    private ScriptEngine parseScript(Path scriptPath) {
        ScriptEngine se =
                new GraalJsEngineWrapper(
                                VerifyScriptTemplates.class.getClassLoader(), List.of(), null)
                        .getEngine();
        se.put("control", Control.getSingleton());
        se.put("model", Model.getSingleton());

        try (Reader reader = Files.newBufferedReader(scriptPath, StandardCharsets.UTF_8)) {
            Compilable c = (Compilable) se;
            CompiledScript cs = c.compile(reader);
            cs.eval();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return se;
    }
}
