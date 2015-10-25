package me.tomassetti.turin.parser.ast.context;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsageNode;

public class ContextDeclaration extends Node {

    public static final String CLASS_PREFIX = "Context_";
    private String name;
    private TypeUsageNode type;

    public ContextDeclaration(String name, TypeUsageNode type) {
        this.name = name;
        this.type = type;
        this.type.setParent(this);
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of(type);
    }

    public TypeUsageNode getType() {
        return type;
    }

    public String getName() {
        return name;

    }
}
