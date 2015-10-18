package me.tomassetti.turin.typesystem;

import me.tomassetti.jvm.JvmMethodDefinition;
import me.tomassetti.jvm.JvmType;
import me.tomassetti.turin.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
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
    /// Subclassing
    ///

    default boolean isArray() {
        return false;
    }

    default boolean isPrimitive() {
        return false;
    }

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

    ///
    /// JVM
    ///

    JvmType jvmType();
    
    JvmMethodDefinition findMethodFor(String name, List<JvmType> argsTypes, SymbolResolver resolver, boolean staticContext);

    boolean canBeAssignedTo(TypeUsage type, SymbolResolver resolver);

    ///
    /// Fields
    ///

    Symbol getFieldOnInstance(String fieldName, Symbol instance, SymbolResolver resolver);

    ///
    /// Methods
    ///


    TypeUsage returnTypeWhenInvokedWith(List<ActualParam> actualParams, SymbolResolver resolver);

    TypeUsage returnTypeWhenInvokedWith(String methodName, List<ActualParam> actualParams, SymbolResolver resolver, boolean staticContext);

    boolean isMethodOverloaded(SymbolResolver resolver, String methodName);

    default boolean isOverloaded() {
        return false;
    }

    default Optional<List<? extends FormalParameter>> findFormalParametersFor(Invokable invokable, SymbolResolver resolver) {
        throw new UnsupportedOperationException(this.getClass().getCanonicalName());
    }

    ///
    /// Misc
    ///

    default boolean isReference() {
        return false;
    }

    <T extends TypeUsage> TypeUsage replaceTypeVariables(Map<String, T> typeParams);

    default TypeUsage calcType() {
        throw new UnsupportedOperationException();
    }

    default String describe() {
        throw new UnsupportedOperationException(this.getClass().getCanonicalName());
    }

    boolean sameType(TypeUsage other);

}
