package me.tomassetti.turin.ast;

import com.google.common.collect.ImmutableList;

public class NamespaceDefinition extends Node {

    private String name;

    public NamespaceDefinition(String manga) {
        this.name = name;
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of();
    }

}
