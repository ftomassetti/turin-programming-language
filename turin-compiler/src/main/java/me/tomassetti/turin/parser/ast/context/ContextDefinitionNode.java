package me.tomassetti.turin.parser.ast.context;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.definitions.ContextDefinition;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsageNode;

public class ContextDefinitionNode extends Node implements ContextDefinition {

    public static final String CLASS_PREFIX = "Context_";
    private String name;
    private TypeUsageNode type;

    public ContextDefinitionNode(String name, TypeUsageNode type) {
        this.name = name;
        this.type = type;
        this.type.setParent(this);
    }

    @Override
    public String toString() {
        return "ContextDefinitionNode{" +
                "name='" + name + '\'' +
                ", type=" + type +
                '}';
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

    public String getClassQualifiedName() {
        return contextName() + "." + CLASS_PREFIX + name;
    }

    public String getQualifiedName() {
        return contextName() + "." + name;
    }
}
