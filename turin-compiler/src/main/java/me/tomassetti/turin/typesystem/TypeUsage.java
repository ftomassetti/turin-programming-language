package me.tomassetti.turin.typesystem;

import me.tomassetti.jvm.JvmType;
import me.tomassetti.turin.symbols.Symbol;

import java.util.Map;
import java.util.Optional;

/**
 * The usage of a type. It can be every sort of type (void, primitive, reference, etc.)
 * used in the AST or in class files. This represents the abstract type while TypeUsageNode
 * represent a single specific usage of a type in the AST.
 */
public interface TypeUsage extends Symbol {

    ///
    /// Relation with other types
    ///

    default boolean isArray() {
        return false;
    }

    default boolean isPrimitive() {
        return false;
    }

    /**
     * Can this be seen as a ReferenceTypeUsage?
     * In other words: is this a reference to a class, an interface or an enum?
     */
    default boolean isReferenceTypeUsage() {
        return false;
    }

    default boolean isVoid() {
        return false;
    }

    default ReferenceTypeUsage asReferenceTypeUsage() {
        throw new UnsupportedOperationException(this.getClass().getCanonicalName());
    }

    default ArrayTypeUsage asArrayTypeUsage() {
        throw new UnsupportedOperationException();
    }

    default PrimitiveTypeUsage asPrimitiveTypeUsage() {
        throw new UnsupportedOperationException();
    }

    /**
     * Is this a reference type? Arrays or references to classes, interfaces and enums are references.
     * Primitive types or void are not.
     */
    default boolean isReference() {
        return false;
    }

    default boolean isInvokable() {
        return false;
    }

    default InvokableType asInvokable() {
        throw new UnsupportedOperationException(this.getClass().getCanonicalName() + ": " + this);
    }

    ///
    /// JVM
    ///

    /**
     * The corresponding JVM Type.
     */
    JvmType jvmType();

    ///
    /// Fields
    ///

    default boolean hasInstanceField(String fieldName, Symbol instance) {
        throw new UnsupportedOperationException(this.getClass().getCanonicalName());
    }

    default Symbol getInstanceField(String fieldName, Symbol instance) {
        throw new UnsupportedOperationException(this.getClass().getCanonicalName());
    }

    ///
    /// Methods
    ///

    default Optional<InvokableType> getMethod(String method, boolean staticContext) {
        throw new UnsupportedOperationException(this.getClass().getCanonicalName());
    }

    ///
    /// Misc
    ///

    boolean sameType(TypeUsage other);

    boolean canBeAssignedTo(TypeUsage type);

    <T extends TypeUsage> TypeUsage replaceTypeVariables(Map<String, T> typeParams);

    default TypeUsage calcType() {
        throw new UnsupportedOperationException();
    }

    default String describe() {
        throw new UnsupportedOperationException(this.getClass().getCanonicalName());
    }


}
