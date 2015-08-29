package me.tomassetti.turin.ast;

import com.google.common.collect.ImmutableList;

public class ReferenceTypeUsage extends TypeUsage {

    private String name;

    public ReferenceTypeUsage(String name) {
        this.name = name;
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of();
    }
}
