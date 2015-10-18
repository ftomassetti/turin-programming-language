package me.tomassetti.turin.definitions;

import me.tomassetti.jvm.JvmConstructorDefinition;
import me.tomassetti.jvm.JvmMethodDefinition;
import me.tomassetti.jvm.JvmNameUtils;
import me.tomassetti.jvm.JvmType;
import me.tomassetti.turin.parser.analysis.exceptions.UnsolvedConstructorException;
import me.tomassetti.turin.parser.analysis.exceptions.UnsolvedMethodException;
import me.tomassetti.turin.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Named;
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

    List<ReferenceTypeUsage> getAllAncestors();

    TypeDefinition getSuperclass();

    ///
    /// Type
    ///

    boolean isInterface();

    boolean isClass();

    default JvmType jvmType() {
        return new JvmType(JvmNameUtils.canonicalToDescriptor(getQualifiedName()));
    }

    ///
    /// Fields
    ///

    TypeUsage getFieldType(String fieldName, boolean staticContext);

    Symbol getFieldOnInstance(String fieldName, Symbol instance);

    boolean hasField(String name, boolean staticContext);

    default boolean hasField(QualifiedName fieldName, boolean staticContext, SymbolResolver resolver) {
        if (!fieldName.isSimpleName()) {
            String firstName = fieldName.firstSegment();
            if (!hasField(firstName, staticContext)) {
                return false;
            }
            Symbol field = getField(firstName);
            TypeUsage typeUsage = field.calcType();
            if (typeUsage.isReferenceTypeUsage()) {
                TypeDefinition typeOfFirstField = typeUsage.asReferenceTypeUsage().getTypeDefinition();
                return typeOfFirstField.hasField(fieldName.rest(), true, resolver) || typeOfFirstField.hasField(fieldName.rest(), false, resolver);
            } else {
                return false;
            }
        }
        return hasField(fieldName.getName(), staticContext);
    }

    boolean canFieldBeAssigned(String field, SymbolResolver resolver);

    ///
    /// Constructors
    ///

    JvmConstructorDefinition resolveConstructorCall(SymbolResolver resolver, List<ActualParam> actualParams);

    default boolean hasManyConstructors() {
        return getConstructors().size() > 1;
    }

    default List<? extends FormalParameter> getConstructorParams(List<ActualParam> actualParams, SymbolResolver resolver) {
        return getConstructor(actualParams, resolver).getFormalParameters();
    }

    default Optional<JvmConstructorDefinition> findConstructorDefinition(List<ActualParam> actualParams, SymbolResolver resolver) {
        Optional<InternalConstructorDefinition> res = findConstructor(actualParams);
        if (res.isPresent()) {
            return Optional.of(res.get().getJvmConstructorDefinition());
        } else {
            return Optional.empty();
        }
    }

    default InternalConstructorDefinition getConstructor(List<ActualParam> actualParams, SymbolResolver resolver) {
        Optional<InternalConstructorDefinition> constructor = findConstructor(actualParams);
        if (constructor.isPresent()) {
            return constructor.get();
        } else {
            throw new UnsolvedConstructorException(getQualifiedName(), actualParams);
        }
    }

    Optional<InternalConstructorDefinition> findConstructor(List<ActualParam> actualParams);

    List<InternalConstructorDefinition> getConstructors();

    ///
    /// Methods
    ///

    default TypeUsage returnTypeWhenInvokedWith(String methodName, List<ActualParam> actualParams, SymbolResolver resolver, boolean staticContext) {
        return getMethod(methodName, actualParams, resolver, staticContext).getReturnType();
    }

    default List<? extends FormalParameter> getMethodParams(String methodName, List<ActualParam> actualParams, SymbolResolver resolver, boolean staticContext) {
        return getMethod(methodName, actualParams, resolver, staticContext).getFormalParameters();
    }

    default boolean hasMethodFor(String methodName, List<ActualParam> actualParams, SymbolResolver resolver, boolean staticContext) {
        return findMethod(methodName, actualParams, resolver, staticContext).isPresent();
    }

    default InternalMethodDefinition getMethod(String methodName, List<ActualParam> actualParams, SymbolResolver resolver, boolean staticContext) {
        Optional<InternalMethodDefinition> method = findMethod(methodName, actualParams, resolver, staticContext);
        if (method.isPresent()) {
            return method.get();
        } else {
            throw new UnsolvedMethodException(getQualifiedName(), methodName, actualParams);
        }
    }

    JvmMethodDefinition findMethodFor(String name, List<JvmType> argsTypes, SymbolResolver resolver, boolean staticContext);

    boolean isMethodOverloaded(String methodName, SymbolResolver resolver);

    Optional<InternalMethodDefinition> findMethod(String methodName, List<ActualParam> actualParams, SymbolResolver resolver, boolean staticContext);

    ///
    /// Misc
    ///

    <T extends TypeUsage> Map<String, TypeUsage> associatedTypeParametersToName(SymbolResolver resolver, List<T> typeParams);
}
