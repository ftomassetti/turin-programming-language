package me.tomassetti.turin.parser.ast.typeusage;

import com.google.common.collect.ImmutableList;
import me.tomassetti.jvm.JvmType;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.virtual.ArrayLength;

public class ArrayTypeUsage extends TypeUsageNode {

    private TypeUsageNode componentType;

    public ArrayTypeUsage(TypeUsageNode componentType) {
        this.componentType = componentType;
    }

    public TypeUsageNode getComponentType() {
        return componentType;
    }

    @Override

    public JvmType jvmType(SymbolResolver resolver) {
        return new JvmType("[" + componentType.jvmType(resolver).getSignature());
    }

    @Override
    public ArrayTypeUsage asArrayTypeUsage() {
        return this;
    }

    @Override
    public boolean canBeAssignedTo(TypeUsageNode type, SymbolResolver resolver) {
        if (type.isArray()) {
            return this.getComponentType().equals(type.asArrayTypeUsage().getComponentType());
        } else {
            return type.equals(ReferenceTypeUsage.OBJECT);
        }
    }

    @Override
    public String toString() {
        return "ArrayTypeUsage{" +
                "componentType=" + componentType +
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
        ArrayTypeUsage copy = new ArrayTypeUsage(this.componentType);
        copy.parent = this.parent;
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ArrayTypeUsage that = (ArrayTypeUsage) o;

        if (!componentType.equals(that.componentType)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return componentType.hashCode();
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of(componentType);
    }
}
