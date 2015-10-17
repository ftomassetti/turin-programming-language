package me.tomassetti.turin.definitions;

import me.tomassetti.jvm.JvmConstructorDefinition;
import me.tomassetti.jvm.JvmMethodDefinition;
import me.tomassetti.jvm.JvmType;
import me.tomassetti.turin.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Named;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.QualifiedName;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;
import me.tomassetti.turin.symbols.FormalParameter;
import me.tomassetti.turin.symbols.Symbol;
import me.tomassetti.turin.typesystem.ReferenceTypeUsage;
import me.tomassetti.turin.typesystem.TypeUsage;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A TypeDefinition should not need to receive a SymbolResolver externally.
 * If it needs it to calculate some answer it should store it since construction time.
 */
public interface TypeDefinition extends Symbol, Named {

    ///
    /// Naming
    ///

    String getQualifiedName();

    ///
    /// Hierarchy
    ///

    List<ReferenceTypeUsage> getAllAncestors(SymbolResolver resolver);

    TypeDefinition getSuperclass();

    ///
    /// Type
    ///

    boolean isInterface();

    boolean isClass();

    JvmType jvmType();

    ///
    /// Fields
    ///

    TypeUsage getFieldType(String fieldName, boolean staticContext, SymbolResolver resolver);

    Node getFieldOnInstance(String fieldName, Node instance, SymbolResolver resolver);

    boolean hasField(String name, boolean staticContext);

    boolean hasField(QualifiedName fieldName, boolean staticContext, SymbolResolver resolver);

    boolean canFieldBeAssigned(String field, SymbolResolver resolver);

    ///
    /// Constructors
    ///

    JvmConstructorDefinition resolveConstructorCall(SymbolResolver resolver, List<ActualParam> actualParams);

    boolean hasManyConstructors(SymbolResolver resolver);

    List<? extends FormalParameter> getConstructorParams(List<ActualParam> actualParams, SymbolResolver resolver);

    Optional<JvmConstructorDefinition> findConstructorDefinition(List<ActualParam> actualParams, SymbolResolver resolver);

    Optional<InternalConstructorDefinition> findConstructor(List<ActualParam> actualParams, SymbolResolver resolver);

    InternalConstructorDefinition getConstructor(List<ActualParam> actualParams, SymbolResolver resolver);

    List<InternalConstructorDefinition> getConstructors(SymbolResolver resolver);

    ///
    /// Methods
    ///

    JvmMethodDefinition findMethodFor(String name, List<JvmType> argsTypes, SymbolResolver resolver, boolean staticContext);

    boolean isMethodOverloaded(String methodName, SymbolResolver resolver);

    Optional<InternalMethodDefinition> findMethod(String methodName, List<ActualParam> actualParams, SymbolResolver resolver, boolean staticContext);

    TypeUsage returnTypeWhenInvokedWith(String methodName, List<ActualParam> actualParams, SymbolResolver resolver, boolean staticContext);

    List<? extends FormalParameter> getMethodParams(String methodName, List<ActualParam> actualParams, SymbolResolver resolver, boolean staticContext);

    boolean hasMethodFor(String methodName, List<ActualParam> actualParams, SymbolResolver resolver, boolean staticContext);

    InternalMethodDefinition getMethod(String methodName, List<ActualParam> actualParams, SymbolResolver resolver, boolean staticContext);

    ///
    /// Misc
    ///

    <T extends TypeUsage> Map<String, TypeUsage> associatedTypeParametersToName(SymbolResolver resolver, List<T> typeParams);
}
