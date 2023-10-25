package org.zaproxy.addon.graphql;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.UnExecutableSchemaGenerator;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zaproxy.addon.commonlib.CommonAlertTag;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GraphQlCyclesDetector {

    private static final String CYCLES_ALERT_REF = ExtensionGraphQl.TOOL_ALERT_ID + "-3";
    private static final Map<String, String> CYCLES_ALERT_TAGS = CommonAlertTag.toMap(); // TODO
    private static final Logger LOGGER = LogManager.getLogger(GraphQlCyclesDetector.class);

    private final GraphQLSchema schema;
    private final GraphQlGenerator generator;

    public GraphQlCyclesDetector(GraphQLSchema schema, GraphQlGenerator generator) {
        this.schema = schema;
        this.generator = generator;
    }

    public void detectCycles() {
        detectCycles(schema.getQueryType(), new ArrayList<>(), new ArrayList<>());
    }

    public void detectCycles(GraphQLType type, List<GraphQLType> seenTypes,
                             List<GraphQLFieldDefinition> seenFields) {
        // Do a depth-first search to detect cycles
        // Ignore fragment spreads for now
        // Ignore leaf nodes
        if (!(type instanceof GraphQLObjectType)) {
            return;
        }
        seenTypes.add(type);
        var object = (GraphQLObjectType) type;
        List<GraphQLFieldDefinition> fields = object.getFieldDefinitions();
        for (GraphQLFieldDefinition field : fields) {
            GraphQLType fieldType = field.getType();
            if (GraphQLTypeUtil.isWrapped(fieldType)) {
                fieldType = GraphQLTypeUtil.unwrapAll(fieldType);
            }
            if (GraphQLTypeUtil.isLeaf(fieldType)) {
                continue;
            }
            seenFields.add(field);
            if (seenTypes.contains(fieldType)) {
                cycleFound(seenFields);
            } else {
                detectCycles(fieldType, seenTypes, seenFields);
            }
            seenFields.remove(seenTypes.size() - 1);
        }
        seenTypes.remove(seenTypes.size() - 1);
    }

    private void cycleFound(List<GraphQLFieldDefinition> fields) {
        var query = new StringBuilder("{ ");
        for (GraphQLFieldDefinition field : fields) {
            query.append(field.getName()).append(" { ");
        }
        query.setLength(query.length()-2);
        query.append(generator.getFirstLeafQuery(fields.get(fields.size()-1).getType(), null, null));
        query.append(StringUtils.repeat("} ", StringUtils.countMatches(query, "{")));
        System.out.println(query);
    }
}
