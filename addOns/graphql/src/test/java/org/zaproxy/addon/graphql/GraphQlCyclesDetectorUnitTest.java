package org.zaproxy.addon.graphql;

import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.UnExecutableSchemaGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zaproxy.zap.model.ValueGenerator;
import org.zaproxy.zap.testutils.TestUtils;

import static org.mockito.Mockito.mock;

class GraphQlCyclesDetectorUnitTest extends TestUtils {
    GraphQlGenerator generator;
    GraphQlParam param;
    private ValueGenerator valueGenerator;

    @BeforeEach
    void setup() throws Exception {
        setUpZap();
        param = new GraphQlParam(true, 5, true, 5, 5, true, null, null, null);
        valueGenerator = mock(ValueGenerator.class);
    }

    private GraphQlCyclesDetector createGraphQlCyclesDetector(String sdl) {
        GraphQLSchema schema = UnExecutableSchemaGenerator.makeUnExecutableSchema(new SchemaParser().parse(sdl));
        var generator = new GraphQlGenerator(valueGenerator, schema, null, param);
        return new GraphQlCyclesDetector(schema, generator);
    }

    @Test
    void circularRelationship() {
        var cyclesDetector = createGraphQlCyclesDetector(getHtml("circularRelationship.graphql"));
        cyclesDetector.detectCycles();
    }

    @Test
    void dvga() {
        var cyclesDetector = createGraphQlCyclesDetector(getHtml("dvga.graphql"));
        cyclesDetector.detectCycles();
    }
}
