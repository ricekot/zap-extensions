package org.zaproxy.zap.extension.openapi;

import org.zaproxy.zap.extension.graaljs.GraalJsActiveScriptScanRuleTestUtils;

import java.nio.file.Path;
import java.util.ResourceBundle;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class SwaggerSecretDetectorScriptTest extends GraalJsActiveScriptScanRuleTestUtils {
    @Override
    public Path getScriptPath() throws Exception {
        return Path.of(
                getClass()
                        .getResource("/scripts/scripts/active/SwaggerSecretDetector.js")
                        .toURI());
    }
}
