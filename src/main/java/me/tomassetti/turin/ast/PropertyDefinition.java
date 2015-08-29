package me.tomassetti.turin.ast;

import com.google.common.collect.ImmutableList;

public class PropertyDefinition extends Node {

    private String name;
    private TypeUsage type;

    public PropertyDefinition(String name, TypeUsage type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of();
    }
}
