package me.tomassetti.turin.parser.ast.relations;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.ast.Node;

import java.util.List;

public class RelationDefinition extends Node {

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
    public Iterable<Node> getChildren() {
        return ImmutableList.copyOf(fields);
    }
}
