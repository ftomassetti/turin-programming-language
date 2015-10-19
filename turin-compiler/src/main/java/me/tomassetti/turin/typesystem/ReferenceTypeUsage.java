package me.tomassetti.turin.typesystem;

import me.tomassetti.jvm.JvmMethodDefinition;
import me.tomassetti.jvm.JvmType;
import me.tomassetti.turin.definitions.InternalConstructorDefinition;
import me.tomassetti.turin.definitions.TypeDefinition;
import me.tomassetti.turin.resolvers.SymbolResolver;
import me.tomassetti.turin.resolvers.jdk.ReflectionTypeDefinitionFactory;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;
import me.tomassetti.turin.parser.ast.expressions.FunctionCall;
import me.tomassetti.turin.parser.ast.expressions.Invokable;
import me.tomassetti.turin.symbols.FormalParameter;
import me.tomassetti.turin.symbols.Symbol;

import java.util.*;
import java.util.stream.Collectors;

/**
 * It could represent also a reference to a Type Variable.
 */
public class ReferenceTypeUsage implements TypeUsage {

    public static final ReferenceTypeUsage OBJECT(SymbolResolver resolver) {
        return new ReferenceTypeUsage(
                ReflectionTypeDefinitionFactory.getInstance().getTypeDefinition(Object.class, resolver));
    }
    public static final ReferenceTypeUsage STRING(SymbolResolver resolver) {
        return new ReferenceTypeUsage(
                ReflectionTypeDefinitionFactory.getInstance().getTypeDefinition(String.class, resolver));
    }
    private List<TypeUsage> typeParams;
    private TypeParameterValues typeParameterValues = new TypeParameterValues();
    private TypeDefinition cachedTypeDefinition;

    public ReferenceTypeUsage(TypeDefinition typeDefinition, List<TypeUsage> typeParams) {
        this.typeParams = new ArrayList<>(typeParams);
        this.cachedTypeDefinition = typeDefinition;
    }

    public ReferenceTypeUsage(TypeDefinition td) {
        this(td, Collections.emptyList());
    }

    @Override
    public boolean isReference() {
        return true;
    }

    public boolean isInterface(SymbolResolver resolver) {
        return getTypeDefinition().isInterface();
    }

    public boolean isClass(SymbolResolver resolver) {
        return getTypeDefinition().isClass();
    }

    public boolean isEnum(SymbolResolver resolver) {
        throw new UnsupportedOperationException();
    }

    public boolean isTypeVariable(SymbolResolver resolver) {
        throw new UnsupportedOperationException();
    }

    public TypeParameterValues getTypeParameterValues() {
        return typeParameterValues;
    }

    public TypeDefinition getTypeDefinition() {
        return cachedTypeDefinition;
    }

    @Override
    public JvmType jvmType() {
        return getTypeDefinition().jvmType();
    }

    public String getQualifiedName() {
        return getTypeDefinition().getQualifiedName();
    }

    @Override
    public boolean isReferenceTypeUsage() {
        return true;
    }

    @Override
    public ReferenceTypeUsage asReferenceTypeUsage() {
        return this;
    }

    @Override
    public boolean canBeAssignedTo(TypeUsage type) {
        if (!type.isReferenceTypeUsage()) {
            return false;
        }
        ReferenceTypeUsage other = type.asReferenceTypeUsage();
        if (this.getQualifiedName().equals(other.getQualifiedName())) {
            return true;
        }
        for (TypeUsage ancestor : this.getAllAncestors()) {
            if (ancestor.canBeAssignedTo(type)) {
                return true;
            }
        }
        return false;
    }

    public List<ReferenceTypeUsage> getAllAncestors() {
        // TODO perhaps some generic type substitution needs to be done
        return getTypeDefinition().getAllAncestors();
    }

    @Override
    public Symbol getInstanceField(String fieldName, Symbol instance) {
        return getTypeDefinition().getFieldOnInstance(fieldName, instance);
    }

    @Override
    public Optional<List<? extends FormalParameter>> findFormalParametersFor(Invokable invokable) {
        if (invokable instanceof FunctionCall) {
            FunctionCall functionCall = (FunctionCall)invokable;
            TypeDefinition typeDefinition = getTypeDefinition();
            return Optional.of(typeDefinition.getMethodParams(functionCall.getName(), invokable.getActualParams(), functionCall.isStatic()));
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public boolean isInvokable() {
        return true;
    }

    @Override
    public InvokableType asInvokable() {
        return new AsConstructor();
    }

    class AsConstructor implements InvokableType {

        @Override
        public TypeUsage returnTypeWhenInvokedWith(List<ActualParam> actualParams) {
            return ReferenceTypeUsage.this;
        }

        @Override
        public Optional<List<? extends FormalParameter>> findFormalParametersFor(List<ActualParam> actualParams) {
            Optional<InternalConstructorDefinition> constructor = getTypeDefinition().findConstructor(actualParams);
            if (constructor.isPresent()) {
                return Optional.of(constructor.get().getFormalParameters());
            } else {
                return Optional.empty();
            }
        }

        @Override
        public JvmMethodDefinition findMethodFor(List<ActualParam> actualParams) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isOverloaded() {
            return getTypeDefinition().hasManyConstructors();
        }
    }

    @Override
    public <T extends TypeUsage> TypeUsage replaceTypeVariables(Map<String, T> typeParams) {
        if (this.typeParams.size() == 0) {
            return this;
        }
        List<TypeUsage> replacedParams = this.typeParams.stream().map((tp)->tp.replaceTypeVariables(typeParams)).collect(Collectors.toList());
        if (!replacedParams.equals(this.typeParams)) {
            ReferenceTypeUsage copy = new ReferenceTypeUsage(this.cachedTypeDefinition);
            copy.typeParams = replacedParams;
            return copy;
        } else {
            return this;
        }
    }

    @Override
    public boolean sameType(TypeUsage other) {
        if (!other.isReferenceTypeUsage()) {
            return false;
        }
        return getQualifiedName().equals(other.asReferenceTypeUsage().getQualifiedName());
    }

    public Map<String, TypeUsage> typeParamsMap() {
        return getTypeDefinition().associatedTypeParametersToName(typeParams);
    }

    public class TypeParameterValues {
        private List<TypeUsage> usages = new ArrayList<>();
        private List<String> names = new ArrayList<>();

        public void add(String name, TypeUsage typeUsage) {
            names.add(name);
            usages.add(typeUsage);
        }

        public List<TypeUsage> getInOrder() {
            return usages;
        }

        public List<String> getNamesInOrder() {
            return names;
        }

        public TypeUsage getByName(String name) {
            for (int i=0; i<names.size(); i++) {
                if (names.get(i).equals(name)) {
                    return usages.get(i);
                }
            }
            throw new IllegalArgumentException(name);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TypeParameterValues)) return false;

            TypeParameterValues that = (TypeParameterValues) o;

            if (!names.equals(that.names)) return false;
            if (!usages.equals(that.usages)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = usages.hashCode();
            result = 31 * result + names.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "TypeParameterValues{" +
                    "usages=" + usages +
                    ", names=" + names +
                    '}';
        }
    }

    @Override
    public Optional<InvokableType> getMethod(String method, boolean staticContext) {
        return getTypeDefinition().getMethod(method, staticContext, typeParamsMap());
    }
}
