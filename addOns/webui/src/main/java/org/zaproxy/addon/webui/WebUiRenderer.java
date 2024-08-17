/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2024 The ZAP Development Team
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
package org.zaproxy.addon.webui;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.httpclient.URI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMessage;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.AbstractConfigurableTemplateResolver;
import org.thymeleaf.templateresource.FileTemplateResource;
import org.thymeleaf.templateresource.ITemplateResource;
import org.zaproxy.zap.extension.script.ExtensionScript;
import org.zaproxy.zap.extension.script.ScriptType;
import org.zaproxy.zap.extension.script.ScriptWrapper;

public class WebUiRenderer {

    private static final Logger LOGGER = LogManager.getLogger(WebUiRenderer.class);

    private final TemplateEngine templateEngine;

    public WebUiRenderer() {
        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(new WebUiTemplateResolver());
    }

    public void handleMessage(HttpMessage msg) {
        try {
            String path = msg.getRequestHeader().getURI().getEscapedPath();
            if (!path.startsWith("/render/")) {
                return;
            }
            String result = templateEngine.process(path, getContextForMessage(msg));
            msg.setResponseBody(result);
            msg.setResponseHeader(
                    WebUiServer.getDefaultResponseHeader("text/html", result.length(), shouldCacheResponse(msg)));
        } catch (Exception e) {
            LOGGER.error("An error occurred while rendering the response: {}", e.getMessage(), e);
        }
    }

    private boolean shouldCacheResponse(HttpMessage msg) {
        URI uri = msg.getRequestHeader().getURI();
        String path = uri.getEscapedPath();
        switch(path) {
            case "/render/workbench/requestResponseViewer":
            case "/render/sidebar/scriptsList":
                return true;
            case "/render/sidebar/sitesTree":
            case "/render/view/scriptContent":
            case "/render/workbench/scriptEditor":
            default:
                return false;
        }
    }

    @SuppressWarnings("fallthrough")
    private Context getContextForMessage(HttpMessage msg) {
        URI uri = msg.getRequestHeader().getURI();
        String path = uri.getEscapedPath();
        var session = Model.getSingleton().getSession();
        try {
            switch (path) {
                case "/render/sidebar/sitesTree": {
                    var sitesTree = session.getSiteTree();
                    return new Context(Locale.getDefault(), Map.of("tree", sitesTree));
                }
                case "/render/workbench/requestResponseViewer": {
                    var queryParams = session.getUrlParameters(uri);
                    for (var param : queryParams) {
                        if (param.getName().equals("hrefId")) {
                            int hrefId = Integer.parseInt(param.getValue());
                            var foundMsg =
                                    session.getSiteTree()
                                            .getSiteNode(hrefId)
                                            .getHistoryReference()
                                            .getHttpMessage();
                            return new Context(Locale.getDefault(), Map.of("msg", foundMsg));
                        }
                    }
                }
                case "/render/sidebar/scriptsList": {
                    var extScript = Control.getSingleton().getExtensionLoader().getExtension(ExtensionScript.class);
                    var scriptTypes = extScript.getScriptTypes();
                    Map<String, List<String>> scriptsMap = new TreeMap<>();
                    for (ScriptType type : scriptTypes) {
                        List<String> scriptNames = extScript.getScripts(type).stream().map(ScriptWrapper::getName).collect(Collectors.toList());
                        scriptsMap.put(type.getName(), scriptNames);
                    }
                    return new Context(Locale.getDefault(), Map.of("scriptsMap", scriptsMap));
                }
                case "/render/view/scriptContent":
                case "/render/workbench/scriptEditor": {
                    var queryParams = session.getUrlParameters(uri);
                    var extScript = Control.getSingleton().getExtensionLoader().getExtension(ExtensionScript.class);
                    for (var param : queryParams) {
                        if (param.getName().equals("scriptName")) {
                            String scriptName = param.getValue();
                            ScriptWrapper script = extScript.getScript(scriptName);
                            if (script == null) {
                                LOGGER.error("Script not found: {}", scriptName);
                                return new Context();
                            }
                            return new Context(Locale.getDefault(), Map.of("script", script));
                        }
                    }
                }
                // Fall through
                default:
                    return new Context();
            }
        } catch (Exception e) {
            LOGGER.error("An error occurred while creating the context for endpoint: {}", path, e);
            return new Context();
        }
    }

    private static class WebUiTemplateResolver extends AbstractConfigurableTemplateResolver {

        @Override
        protected ITemplateResource computeTemplateResource(
                final IEngineConfiguration configuration,
                final String ownerTemplate,
                final String template,
                final String resourceName,
                final String characterEncoding,
                final Map<String, Object> templateResolutionAttributes) {
            String filePath = resourceName;
            if (filePath.endsWith("/")) {
                filePath = filePath.substring(0, filePath.length() - 1);
            }
            if (!filePath.endsWith(".html")) {
                filePath += ".html";
            }
            File file = new File(ExtensionWebUi.getBaseDirectory(), filePath);
            if (!file.exists()) {
                LOGGER.error("Requested template file does not exist: {}", file.getAbsolutePath());
                file = new File(ExtensionWebUi.getBaseDirectory(), WebUiServer.ERROR_404_PAGE);
            }
            return new FileTemplateResource(file.getAbsolutePath(), characterEncoding);
        }
    }
}
