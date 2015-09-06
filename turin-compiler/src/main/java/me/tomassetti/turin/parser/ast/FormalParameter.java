package me.tomassetti.turin.parser.ast;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.analysis.resolvers.Resolver;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

public class FormalParameter extends Node {

    private TypeUsage type;
    private String name;

    public FormalParameter(TypeUsage type, String name) {
        this.type = type;
        this.type.parent = this;
        this.name = name;
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of(type);
    }

    @Override
    public TypeUsage calcType(Resolver resolver) {
        return type;
    }

    public TypeUsage getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
