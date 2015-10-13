package me.tomassetti.turin.parser.ast.relations;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

public class RelationFieldDefinition extends Node {

    private String name;
    private TypeUsage type;

    public RelationFieldDefinition(String name, TypeUsage type) {
        this.name = name;
        this.type = type;
        this.type.setParent(this);
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of(type);
    }
}
