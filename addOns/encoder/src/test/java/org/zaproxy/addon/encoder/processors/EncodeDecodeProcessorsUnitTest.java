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
package org.zaproxy.addon.encoder.processors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.zaproxy.addon.encoder.ExtensionEncoder;
import org.zaproxy.zap.testutils.TestUtils;

/** Unit test for {@link EncodeDecodeProcessors}. */
public class EncodeDecodeProcessorsUnitTest extends TestUtils {

    @BeforeAll
    static void setup() {
        mockMessages(new ExtensionEncoder());
    }

    @Test
    void shouldLoadPredefinedProcessors() {
        // Given / When
        List<EncodeDecodeProcessorItem> processors =
                EncodeDecodeProcessors.getPredefinedProcessors();
        // Then
        assertThat(processors, hasSize(31));
    }
}