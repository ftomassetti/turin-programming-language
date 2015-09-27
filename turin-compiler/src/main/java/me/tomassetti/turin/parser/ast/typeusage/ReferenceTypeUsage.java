package me.tomassetti.turin.parser.ast.typeusage;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.jvm.JvmMethodDefinition;
import me.tomassetti.turin.jvm.JvmNameUtils;
import me.tomassetti.turin.jvm.JvmType;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.FormalParameter;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.TypeDefinition;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;
import me.tomassetti.turin.parser.ast.expressions.FunctionCall;
import me.tomassetti.turin.parser.ast.expressions.Invokable;

import java.util.*;

/**
 * It could represent also a reference to a Type Variable.
 */
public class ReferenceTypeUsage extends TypeUsage {

    public static final ReferenceTypeUsage OBJECT = new ReferenceTypeUsage("java.lang.Object");
    public static final ReferenceTypeUsage STRING = new ReferenceTypeUsage("java.lang.String");
    private List<TypeUsage> typeParams;
    private TypeParameterValues typeParameterValues = new TypeParameterValues();
    private String name;

    public ReferenceTypeUsage(TypeDefinition typeDefinition, List<TypeUsage> typeParams) {
        this(typeDefinition.getQualifiedName());
        this.typeParams = typeParams;
    }

    public ReferenceTypeUsage(String name) {
        if (name.contains("/")) {
            throw new IllegalArgumentException(name);
        }
        if (name.startsWith(".")) {
            throw new IllegalArgumentException();
        }
        if (JvmNameUtils.isPrimitiveTypeName(name)) {
            throw new IllegalArgumentException();
        }
        this.name = name;
        this.typeParams = Collections.emptyList();
    }

    public ReferenceTypeUsage(TypeDefinition td) {
        this(td.getQualifiedName());
    }

    public boolean isInterface(SymbolResolver resolver) {
        return getTypeDefinition(resolver).isInterface();
    }

    public boolean isClass(SymbolResolver resolver) {
        throw new UnsupportedOperationException();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReferenceTypeUsage that = (ReferenceTypeUsage) o;

        if (!name.equals(that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "ReferenceTypeUsage{" +
                "name='" + name + '\'' +

                '}';
    }

    public TypeDefinition getTypeDefinition(SymbolResolver resolver) {
        TypeDefinition typeDefinition = resolver.getTypeDefinitionIn(this.name, this, resolver);
        return typeDefinition;
    }

    @Override
    public JvmMethodDefinition findMethodFor(String methodName, List<JvmType> argsTypes, SymbolResolver resolver, boolean staticContext) {
        TypeDefinition typeDefinition = getTypeDefinition(resolver);
        return typeDefinition.findMethodFor(methodName, argsTypes, resolver, staticContext);
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of();
    }

    @Override
    public JvmType jvmType(SymbolResolver resolver) {
        TypeDefinition typeDefinition = resolver.getTypeDefinitionIn(name, this, resolver);
        return typeDefinition.jvmType();
    }

    public String getQualifiedName(SymbolResolver resolver) {
        TypeDefinition typeDefinition = resolver.getTypeDefinitionIn(name, this, resolver);
        return typeDefinition.getQualifiedName();
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
    public boolean canBeAssignedTo(TypeUsage type, SymbolResolver resolver) {
        if (!type.isReferenceTypeUsage()) {
            return false;
        }
        ReferenceTypeUsage other = (ReferenceTypeUsage)type;
        if (this.getQualifiedName(resolver).equals(other.getQualifiedName(resolver))) {
            return true;
        }
        for (TypeUsage ancestor : this.getAllAncestors(resolver)) {
            if (ancestor.canBeAssignedTo(type, resolver)) {
                return true;
            }
        }
        return false;
    }

    public List<ReferenceTypeUsage> getAllAncestors(SymbolResolver resolver) {
        // TODO perhaps some generic type substitution needs to be done
        return getTypeDefinition(resolver).getAllAncestors(resolver);
    }

    @Override
    public Node getFieldOnInstance(String fieldName, Node instance, SymbolResolver resolver) {
        return getTypeDefinition(resolver).getFieldOnInstance(fieldName, instance, resolver);
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
    }

    @Override
    public Optional<List<FormalParameter>> findFormalParametersFor(Invokable invokable, SymbolResolver resolver) {
        if (invokable instanceof FunctionCall) {
            FunctionCall functionCall = (FunctionCall)invokable;
            TypeDefinition typeDefinition = getTypeDefinition(resolver);
            return Optional.of(typeDefinition.getMethodParams(functionCall.getName(), invokable.getActualParams(), resolver, functionCall.isStatic(resolver)));
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public TypeUsage returnTypeWhenInvokedWith(String methodName, List<ActualParam> actualParams, SymbolResolver resolver, boolean staticContext) {
        TypeDefinition typeDefinition = getTypeDefinition(resolver);
        return typeDefinition.returnTypeWhenInvokedWith(methodName, actualParams, resolver, staticContext);
    }

    @Override
    public boolean isMethodOverloaded(SymbolResolver resolver, String methodName) {
        return getTypeDefinition(resolver).isMethodOverloaded(methodName, resolver);
    }
}
