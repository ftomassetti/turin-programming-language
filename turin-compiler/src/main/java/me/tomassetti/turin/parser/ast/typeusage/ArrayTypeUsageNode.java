package me.tomassetti.turin.parser.ast.typeusage;

import com.google.common.collect.ImmutableList;
import me.tomassetti.jvm.JvmType;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.virtual.ArrayLength;
import me.tomassetti.turin.typesystem.TypeUsage;

public class ArrayTypeUsageNode extends TypeUsageNode {

    private TypeUsageNode componentTypeNode;
    private TypeUsage componentType;
    private TypeUsage typeUsage;

    public final TypeUsage typeUsage() {
        return typeUsage;
    }

    public ArrayTypeUsageNode(TypeUsageNode componentType) {
        this.componentTypeNode = componentType;
        this.componentType = componentTypeNode.typeUsage();
        this.typeUsage = new me.tomassetti.turin.typesystem.ArrayTypeUsage(componentType);
    }

    public TypeUsageNode getComponentTypeNode() {
        return componentTypeNode;
    }

    @Override
    public JvmType jvmType(SymbolResolver resolver) {
        return new JvmType("[" + componentTypeNode.jvmType(resolver).getSignature());
    }

    @Override
    public me.tomassetti.turin.typesystem.ArrayTypeUsage asArrayTypeUsage() {
        return this.typeUsage().asArrayTypeUsage();
    }

    @Override
    public boolean canBeAssignedTo(TypeUsageNode type, SymbolResolver resolver) {
        if (type.isArray()) {
            return this.getComponentTypeNode().equals(type.asArrayTypeUsage().getComponentType());
        } else {
            return type.equals(ReferenceTypeUsage.OBJECT);
        }
    }

    @Override
    public String toString() {
        return "ArrayTypeUsage{" +
                "componentTypeNode=" + componentTypeNode +
                '}';
    }

    @Override
    public Node getFieldOnInstance(String fieldName, Node instance, SymbolResolver resolver) {
        if (fieldName.equals("length")) {
            return new ArrayLength(instance);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isMethodOverloaded(SymbolResolver resolver, String methodName) {
        return false;
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
