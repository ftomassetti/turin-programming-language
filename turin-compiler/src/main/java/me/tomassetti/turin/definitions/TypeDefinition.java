package me.tomassetti.turin.definitions;

import me.tomassetti.jvm.JvmConstructorDefinition;
import me.tomassetti.jvm.JvmMethodDefinition;
import me.tomassetti.jvm.JvmNameUtils;
import me.tomassetti.jvm.JvmType;
import me.tomassetti.turin.parser.analysis.exceptions.UnsolvedConstructorException;
import me.tomassetti.turin.parser.analysis.exceptions.UnsolvedMethodException;
import me.tomassetti.turin.parser.ast.Named;
import me.tomassetti.turin.parser.ast.QualifiedName;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;
import me.tomassetti.turin.symbols.FormalParameter;
import me.tomassetti.turin.symbols.Symbol;
import me.tomassetti.turin.typesystem.InvokableType;
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

    default boolean hasField(QualifiedName fieldName, boolean staticContext) {
        if (!fieldName.isSimpleName()) {
            String firstName = fieldName.firstSegment();
            if (!hasField(firstName, staticContext)) {
                return false;
            }
            Symbol field = getField(firstName);
            TypeUsage typeUsage = field.calcType();
            if (typeUsage.isReferenceTypeUsage()) {
                TypeDefinition typeOfFirstField = typeUsage.asReferenceTypeUsage().getTypeDefinition();
                return typeOfFirstField.hasField(fieldName.rest(), true) || typeOfFirstField.hasField(fieldName.rest(), false);
            } else {
                return false;
            }
        }
        return hasField(fieldName.getName(), staticContext);
    }

    boolean canFieldBeAssigned(String field);

    ///
    /// Constructors
    ///

    JvmConstructorDefinition resolveConstructorCall(List<ActualParam> actualParams);

    default boolean hasManyConstructors() {
        return getConstructors().size() > 1;
    }

    default List<? extends FormalParameter> getConstructorParams(List<ActualParam> actualParams) {
        return getConstructor(actualParams).getFormalParameters();
    }

    default Optional<JvmConstructorDefinition> findConstructorDefinition(List<ActualParam> actualParams) {
        Optional<InternalConstructorDefinition> res = findConstructor(actualParams);
        if (res.isPresent()) {
            return Optional.of(res.get().getJvmConstructorDefinition());
        } else {
            return Optional.empty();
        }
    }

    default InternalConstructorDefinition getConstructor(List<ActualParam> actualParams) {
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

    default TypeUsage returnTypeWhenInvokedWith(String methodName, List<ActualParam> actualParams, boolean staticContext) {
        return getMethod(methodName, actualParams, staticContext).getReturnType();
    }

    default List<? extends FormalParameter> getMethodParams(String methodName, List<ActualParam> actualParams, boolean staticContext) {
        return getMethod(methodName, actualParams, staticContext).getFormalParameters();
    }

    default boolean hasMethodFor(String methodName, List<ActualParam> actualParams, boolean staticContext) {
        return findMethod(methodName, actualParams, staticContext).isPresent();
    }

    default InternalMethodDefinition getMethod(String methodName, List<ActualParam> actualParams, boolean staticContext) {
        Optional<InternalMethodDefinition> method = findMethod(methodName, actualParams, staticContext);
        if (method.isPresent()) {
            return method.get();
        } else {
            throw new UnsolvedMethodException(getQualifiedName(), methodName, actualParams);
        }
    }

    JvmMethodDefinition findMethodFor(String name, List<JvmType> argsTypes, boolean staticContext);

    Optional<InternalMethodDefinition> findMethod(String methodName, List<ActualParam> actualParams, boolean staticContext);

    ///
    /// Misc
    ///

    <T extends TypeUsage> Map<String, TypeUsage> associatedTypeParametersToName(List<T> typeParams);

    default Optional<InvokableType> getMethod(String method, boolean staticContext, Map<String, TypeUsage> stringTypeUsageMap) {
        throw new UnsupportedOperationException(this.getClass().getCanonicalName());
    }
}
