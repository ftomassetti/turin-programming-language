package me.tomassetti.turin.parser.ast;

import com.google.common.collect.ImmutableList;

public class ComposedReference extends Node {

    private String typeName;
    private QualifiedName pathInType;

    public ComposedReference(String typeName, QualifiedName pathInType) {
        this.typeName = typeName;
        this.pathInType = pathInType;
        this.pathInType.setParent(this);
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of(pathInType);
    }
}
