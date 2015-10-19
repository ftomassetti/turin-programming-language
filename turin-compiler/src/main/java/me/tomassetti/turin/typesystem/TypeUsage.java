package me.tomassetti.turin.typesystem;

import me.tomassetti.jvm.JvmMethodDefinition;
import me.tomassetti.jvm.JvmType;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;
import me.tomassetti.turin.parser.ast.expressions.Invokable;
import me.tomassetti.turin.symbols.FormalParameter;
import me.tomassetti.turin.symbols.Symbol;

import java.util.List;
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

    default InvokableTypeUsage asInvokable() {
        throw new UnsupportedOperationException(this.getClass().getCanonicalName());
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

    TypeUsage returnTypeWhenInvokedWith(String methodName, List<ActualParam> actualParams, boolean staticContext);

    boolean isMethodOverloaded(String methodName);

    default Optional<List<? extends FormalParameter>> findFormalParametersFor(Invokable invokable) {
        throw new UnsupportedOperationException(this.getClass().getCanonicalName());
    }

    JvmMethodDefinition findMethodFor(String name, List<JvmType> argsTypes, boolean staticContext);

    ///
    /// Misc
    ///

    boolean canBeAssignedTo(TypeUsage type);

    <T extends TypeUsage> TypeUsage replaceTypeVariables(Map<String, T> typeParams);

    default TypeUsage calcType() {
        throw new UnsupportedOperationException();
    }

    default String describe() {
        throw new UnsupportedOperationException(this.getClass().getCanonicalName());
    }

    boolean sameType(TypeUsage other);

}
