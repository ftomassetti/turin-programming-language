package me.tomassetti.turin.typesystem;

import me.tomassetti.jvm.JvmMethodDefinition;
import me.tomassetti.jvm.JvmType;
import me.tomassetti.turin.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsageNode;
import me.tomassetti.turin.parser.ast.virtual.ArrayLength;
import me.tomassetti.turin.symbols.Symbol;

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

    ///
    /// Relation with other types
    ///

    @Override
    public boolean isArray() {
        return true;
    }

    @Override
    public boolean isReference() {
        return true;
    }

    @Override
    public ArrayTypeUsage asArrayTypeUsage() {
        return this;
    }

    ///
    /// JVM
    ///

    @Override
    public JvmType jvmType() {
        return new JvmType("[" + componentType.jvmType().getSignature());
    }

    ///
    /// Fields
    ///

    @Override
    public Symbol getFieldOnInstance(String fieldName, Symbol instance) {
        if (fieldName.equals("length")) {
            return new ArrayLength(instance);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public JvmMethodDefinition findMethodFor(String name, List<JvmType> argsTypes, boolean staticContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canBeAssignedTo(TypeUsage type) {
        if (type.isArray()) {
            return componentType.equals(type.asArrayTypeUsage().getComponentType());
        } else {
            return type.isReferenceTypeUsage() && type.asReferenceTypeUsage().getQualifiedName().equals(Object.class.getCanonicalName());
        }
    }

    @Override
    public String toString() {
        return "ArrayTypeUsage{" +
                "componentType=" + componentType +
                '}';
    }

    @Override
    public TypeUsageNode returnTypeWhenInvokedWith(List<ActualParam> actualParams) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TypeUsageNode returnTypeWhenInvokedWith(String methodName, List<ActualParam> actualParams, boolean staticContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isMethodOverloaded(String methodName) {
        return false;
    }

    @Override
    public <T extends TypeUsage> TypeUsage replaceTypeVariables(Map<String, T> typeParams) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean sameType(TypeUsage other) {
        if (!other.isArray()) {
            return false;
        }
        return this.getComponentType().sameType(other.asArrayTypeUsage().getComponentType());
    }

}
