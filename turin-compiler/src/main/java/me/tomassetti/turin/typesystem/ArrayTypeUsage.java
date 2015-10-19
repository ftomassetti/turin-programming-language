package me.tomassetti.turin.typesystem;

import me.tomassetti.jvm.JvmType;
import me.tomassetti.turin.parser.ast.virtual.ArrayLength;
import me.tomassetti.turin.symbols.Symbol;

import java.util.Map;

public class ArrayTypeUsage implements TypeUsage {

    private static String LENGTH_FIELD_NAME = "length";

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
    public boolean hasInstanceField(String fieldName, Symbol instance) {
        return fieldName.equals(LENGTH_FIELD_NAME);
    }

    @Override
    public Symbol getInstanceField(String fieldName, Symbol instance) {
        if (fieldName.equals(LENGTH_FIELD_NAME)) {
            return new ArrayLength(instance);
        } else {
            throw new IllegalArgumentException("An array has no field named " + fieldName);
        }
    }

    ///
    /// Methods
    ///

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
