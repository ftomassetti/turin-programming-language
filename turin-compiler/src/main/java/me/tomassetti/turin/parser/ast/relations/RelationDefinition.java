package me.tomassetti.turin.parser.ast.relations;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.compiler.errorhandling.ErrorCollector;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;

import java.util.List;

public class RelationDefinition extends Node {

    public enum Type {
        ONE_TO_ONE,
        ONE_TO_MANY,
        MANY_TO_MANY
    }

    public static final String CLASS_PREFIX = "Relation_";

    private String name;
    private List<RelationFieldDefinition> fields;

    public String getName() {
        return name;
    }

    public List<RelationFieldDefinition> getFields() {
        return fields;
    }

    public RelationDefinition(String name, List<RelationFieldDefinition> fields) {
        this.name = name;
        this.fields = fields;
        this.fields.forEach((f)->f.setParent(RelationDefinition.this));
    }

    @Override
    protected boolean specificValidate(SymbolResolver resolver, ErrorCollector errorCollector) {
        if (fields.size() != 2) {
            errorCollector.recordSemanticError(getPosition(), "Each relation should have exactly 2 fields");
            return false;
        }
        return super.specificValidate(resolver, errorCollector);
    }

    public Type getRelationType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.copyOf(fields);
    }
}
