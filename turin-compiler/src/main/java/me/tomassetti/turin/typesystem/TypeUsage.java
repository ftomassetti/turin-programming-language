package me.tomassetti.turin.typesystem;

import me.tomassetti.jvm.JvmMethodDefinition;
import me.tomassetti.jvm.JvmType;
import me.tomassetti.jvm.JvmTypeCategory;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.FormalParameter;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;
import me.tomassetti.turin.parser.ast.expressions.Invokable;
import me.tomassetti.turin.parser.ast.typeusage.PrimitiveTypeUsageNode;
import me.tomassetti.turin.parser.ast.typeusage.ReferenceTypeUsage;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsageNode;
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
    JvmType jvmType(SymbolResolver resolver);

    default boolean isReferenceTypeUsage() {
        return false;
    }

    default ReferenceTypeUsage asReferenceTypeUsage() {
        throw new UnsupportedOperationException();
    }

    default ArrayTypeUsage asArrayTypeUsage() {
        throw new UnsupportedOperationException();
    }

    JvmMethodDefinition findMethodFor(String name, List<JvmType> argsTypes, SymbolResolver resolver, boolean staticContext);

    boolean canBeAssignedTo(TypeUsage type, SymbolResolver resolver);

    default boolean isArray() {
        return false;
    }

    default boolean isPrimitive() {
        return false;
    }

    default boolean isReference() {
        return false;
    }

    default PrimitiveTypeUsage asPrimitiveTypeUsage() {
        throw new UnsupportedOperationException();
    }

    Node getFieldOnInstance(String fieldName, Node instance, SymbolResolver resolver);

    TypeUsageNode returnTypeWhenInvokedWith(List<ActualParam> actualParams, SymbolResolver resolver);

    TypeUsageNode returnTypeWhenInvokedWith(String methodName, List<ActualParam> actualParams, SymbolResolver resolver, boolean staticContext);

    boolean isMethodOverloaded(SymbolResolver resolver, String methodName);

    default boolean isOverloaded() {
        return false;
    }

    default boolean isVoid() {
        return false;
    }

    TypeUsageNode replaceTypeVariables(Map<String, TypeUsageNode> typeParams);

    default TypeUsage calcType(SymbolResolver resolver) {
        throw new UnsupportedOperationException();
    }

    default Optional<List<FormalParameter>> findFormalParametersFor(Invokable invokable, SymbolResolver resolver) {
        throw new UnsupportedOperationException(this.getClass().getCanonicalName());
    }

    default String describe() {
        throw new UnsupportedOperationException(this.getClass().getCanonicalName());
    }

    default JvmTypeCategory toJvmTypeCategory(SymbolResolver resolver) {
        return jvmType(resolver).typeCategory();
    }
}
