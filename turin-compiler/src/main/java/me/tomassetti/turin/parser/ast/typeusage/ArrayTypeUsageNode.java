package me.tomassetti.turin.parser.ast.typeusage;

import com.google.common.collect.ImmutableList;
import me.tomassetti.jvm.JvmType;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.virtual.ArrayLength;
import me.tomassetti.turin.typesystem.ArrayTypeUsage;
import me.tomassetti.turin.typesystem.TypeUsage;

public class ArrayTypeUsageNode extends TypeUsageWrapperNode {

    private TypeUsageNode componentTypeNode;
    private TypeUsage componentType;

    public ArrayTypeUsageNode(TypeUsageNode componentType) {
        super(new ArrayTypeUsage(componentType));
        this.componentTypeNode = componentType;
        this.componentType = componentTypeNode.typeUsage();
    }

    public TypeUsageNode getComponentTypeNode() {
        return componentTypeNode;
    }

    @Override
    public String toString() {
        return "ArrayTypeUsage{" +
                "componentTypeNode=" + componentTypeNode +
                '}';
    }

    @Override
    public TypeUsageNode copy() {
        ArrayTypeUsageNode copy = new ArrayTypeUsageNode(this.componentTypeNode);
        copy.parent = this.parent;
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ArrayTypeUsageNode that = (ArrayTypeUsageNode) o;

        if (!componentTypeNode.equals(that.componentTypeNode)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return componentTypeNode.hashCode();
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of(componentTypeNode);
    }
}
