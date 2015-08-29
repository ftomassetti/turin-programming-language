package me.tomassetti.turin.parser.ast;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.analysis.Resolver;

/**
 * Created by federico on 28/08/15.
 */
public class PropertyReference extends Node {
    public String getName() {
        return name;
    }

    private String name;

    public PropertyReference(String name) {
        this.name = name;
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of();
    }

    public TypeUsage getType(Resolver resolver) {
        return resolver.findDefinition(this).getType();
    }
}
