package me.tomassetti.turin.parser.ast.relations;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

public class RelationFieldDefinition extends Node {

    private String name;
    private TypeUsage type;

    public enum Cardinality {
        SINGLE,
        MANY
    }

    public String getName() {
        return name;
    }

    public TypeUsage getType() {
        return type;
    }

    public Cardinality getCardinality() {
        return cardinality;
    }

    private Cardinality cardinality;

    public RelationFieldDefinition(Cardinality cardinality, String name, TypeUsage type) {
        this.cardinality = cardinality;
        this.name = name;
        this.type = type;
        this.type.setParent(this);
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of(type);
    }
}
