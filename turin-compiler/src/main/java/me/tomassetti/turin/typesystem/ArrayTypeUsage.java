package me.tomassetti.turin.typesystem;

import me.tomassetti.jvm.JvmMethodDefinition;
import me.tomassetti.jvm.JvmType;
import me.tomassetti.jvm.JvmTypeCategory;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;
import me.tomassetti.turin.parser.ast.typeusage.ReferenceTypeUsage;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsageNode;
import me.tomassetti.turin.parser.ast.virtual.ArrayLength;

import java.util.List;
import java.util.Map;

public class ArrayTypeUsage implements TypeUsage {

    private TypeUsage componentType;

    public ArrayTypeUsage(TypeUsage componentType) {
        this.componentType = componentType;
    }

    public TypeUsage getComponentType() {
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
    public JvmMethodDefinition findMethodFor(String name, List<JvmType> argsTypes, SymbolResolver resolver, boolean staticContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canBeAssignedTo(TypeUsageNode type, SymbolResolver resolver) {
        if (type.isArray()) {
            return componentType.equals(type.asArrayTypeUsage().getComponentType());
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
    public TypeUsageNode returnTypeWhenInvokedWith(List<ActualParam> actualParams, SymbolResolver resolver) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TypeUsageNode returnTypeWhenInvokedWith(String methodName, List<ActualParam> actualParams, SymbolResolver resolver, boolean staticContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isMethodOverloaded(SymbolResolver resolver, String methodName) {
        return false;
    }

    @Override
    public TypeUsageNode replaceTypeVariables(Map<String, TypeUsageNode> typeParams) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JvmTypeCategory toJvmTypeCategory(SymbolResolver resolver) {
        throw new UnsupportedOperationException();
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

}
