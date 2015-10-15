package me.tomassetti.turin.parser.ast.typeusage;

import com.google.common.collect.ImmutableList;
import me.tomassetti.jvm.JvmMethodDefinition;
import me.tomassetti.jvm.JvmNameUtils;
import me.tomassetti.jvm.JvmType;
import me.tomassetti.turin.compiler.errorhandling.ErrorCollector;
import me.tomassetti.turin.parser.analysis.exceptions.UnsolvedSymbolException;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.FormalParameter;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.NodeTypeDefinition;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;
import me.tomassetti.turin.parser.ast.expressions.FunctionCall;
import me.tomassetti.turin.parser.ast.expressions.Invokable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * It could represent also a reference to a Type Variable.
 */
public class ReferenceTypeUsage extends TypeUsage {

    public static final ReferenceTypeUsage OBJECT = new ReferenceTypeUsage("java.lang.Object");
    public static final ReferenceTypeUsage STRING = new ReferenceTypeUsage("java.lang.String");
    private List<TypeUsage> typeParams;
    private TypeParameterValues typeParameterValues = new TypeParameterValues();
    private String name;
    private boolean fullyQualifiedName;
    private NodeTypeDefinition cachedTypeDefinition;

    public ReferenceTypeUsage(NodeTypeDefinition typeDefinition, List<TypeUsage> typeParams) {
        this(typeDefinition.getQualifiedName(), false);
        this.typeParams = typeParams;
        this.cachedTypeDefinition = typeDefinition;
    }

    public ReferenceTypeUsage(String name) {
        this(name, false);
    }

    public ReferenceTypeUsage(String name, boolean fullyQualifiedName) {
        if (JvmNameUtils.isPrimitiveTypeName(name)) {
            throw new IllegalArgumentException(name);
        }
        if (!JvmNameUtils.isValidQualifiedName(name)) {
            throw new IllegalArgumentException(name);
        }
        this.name = name;
        this.typeParams = Collections.emptyList();
        this.fullyQualifiedName = fullyQualifiedName;
    }

    public ReferenceTypeUsage(NodeTypeDefinition td) {
        this(td.getQualifiedName(), true);
        this.cachedTypeDefinition = td;
    }

    public boolean isInterface(SymbolResolver resolver) {
        return getTypeDefinition(resolver).isInterface();
    }

    public boolean isClass(SymbolResolver resolver) {
        return getTypeDefinition(resolver).isClass();
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
                "typeParams=" + typeParams +
                ", typeParameterValues=" + typeParameterValues +
                ", name='" + name + '\'' +
                ", fullyQualifiedName=" + fullyQualifiedName +
                ", cachedTypeDefinition=" + cachedTypeDefinition +
                '}';
    }

    public NodeTypeDefinition getTypeDefinition(SymbolResolver resolver) {
        if (cachedTypeDefinition != null) {
            return cachedTypeDefinition;
        }
        NodeTypeDefinition typeDefinition = resolver.getRoot().getTypeDefinitionIn(this.name, this, resolver.getRoot());
        return typeDefinition;
    }

    @Override
    public JvmMethodDefinition findMethodFor(String methodName, List<JvmType> argsTypes, SymbolResolver resolver, boolean staticContext) {
        return getTypeDefinition(resolver).findMethodFor(methodName, argsTypes, resolver, staticContext);
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of();
    }

    @Override
    public JvmType jvmType(SymbolResolver resolver) {
        return getTypeDefinition(resolver).jvmType();
    }

    public String getQualifiedName(SymbolResolver resolver) {
        if (fullyQualifiedName) {
            return name;
        }
        return getTypeDefinition(resolver).getQualifiedName();
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

    @Override
    protected boolean specificValidate(SymbolResolver resolver, ErrorCollector errorCollector) {
        try {
            getTypeDefinition(resolver);
        } catch (UnsolvedSymbolException e) {
            errorCollector.recordSemanticError(getPosition(), e.getMessage());
            return false;
        }
        return super.specificValidate(resolver, errorCollector);
    }

    @Override
    public Optional<List<FormalParameter>> findFormalParametersFor(Invokable invokable, SymbolResolver resolver) {
        if (invokable instanceof FunctionCall) {
            FunctionCall functionCall = (FunctionCall)invokable;
            NodeTypeDefinition typeDefinition = getTypeDefinition(resolver);
            return Optional.of(typeDefinition.getMethodParams(functionCall.getName(), invokable.getActualParams(), resolver, functionCall.isStatic(resolver)));
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public TypeUsage returnTypeWhenInvokedWith(String methodName, List<ActualParam> actualParams, SymbolResolver resolver, boolean staticContext) {
        NodeTypeDefinition typeDefinition = getTypeDefinition(resolver);
        TypeUsage typeUsage = typeDefinition.returnTypeWhenInvokedWith(methodName, actualParams, resolver, staticContext);
        return typeUsage.replaceTypeVariables(typeParamsMap(resolver));
    }

    @Override
    public TypeUsage replaceTypeVariables(Map<String, TypeUsage> typeParams) {
        if (this.typeParams.size() == 0) {
            return this;
        }
        List<TypeUsage> replacedParams = this.typeParams.stream().map((tp)->tp.replaceTypeVariables(typeParams)).collect(Collectors.toList());
        if (!replacedParams.equals(this.typeParams)) {
            ReferenceTypeUsage copy = (ReferenceTypeUsage) this.copy();
            copy.typeParams = replacedParams;
            return copy;
        } else {
            return this;
        }
    }

    @Override
    public boolean isMethodOverloaded(SymbolResolver resolver, String methodName) {
        return getTypeDefinition(resolver).isMethodOverloaded(methodName, resolver);
    }

    public Map<String, TypeUsage> typeParamsMap(SymbolResolver resolver) {
        return getTypeDefinition(resolver).associatedTypeParametersToName(resolver, typeParams);
    }

    @Override
    public TypeUsage copy() {
        ReferenceTypeUsage copy = new ReferenceTypeUsage(name);
        copy.parent = this.parent;
        copy.cachedTypeDefinition = this.cachedTypeDefinition;
        copy.fullyQualifiedName = this.fullyQualifiedName;
        copy.typeParams = this.typeParams;
        copy.typeParameterValues = this.typeParameterValues;
        return copy;
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
        public String toString() {
            return "TypeParameterValues{" +
                    "usages=" + usages +
                    ", names=" + names +
                    '}';
        }
    }
}
