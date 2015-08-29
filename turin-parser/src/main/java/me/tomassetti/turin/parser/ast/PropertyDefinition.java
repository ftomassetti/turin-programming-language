package me.tomassetti.turin.parser.ast;

import com.google.common.collect.ImmutableList;

public class PropertyDefinition extends Node {

    private String name;
    private TypeUsage type;

    public PropertyDefinition(String name, TypeUsage type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public TypeUsage getType() {
        return type;
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of();
    }
}
