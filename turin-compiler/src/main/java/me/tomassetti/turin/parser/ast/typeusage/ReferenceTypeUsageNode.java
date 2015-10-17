package me.tomassetti.turin.parser.ast.typeusage;

import com.google.common.collect.ImmutableList;
import me.tomassetti.jvm.JvmMethodDefinition;
import me.tomassetti.jvm.JvmNameUtils;
import me.tomassetti.jvm.JvmType;
import me.tomassetti.turin.compiler.errorhandling.ErrorCollector;
import me.tomassetti.turin.parser.analysis.exceptions.UnsolvedSymbolException;
import me.tomassetti.turin.parser.analysis.resolvers.ResolverRegistry;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.TypeDefinition;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;
import me.tomassetti.turin.parser.ast.expressions.FunctionCall;
import me.tomassetti.turin.parser.ast.expressions.Invokable;
import me.tomassetti.turin.symbols.FormalParameter;
import me.tomassetti.turin.typesystem.ReferenceTypeUsage;
import me.tomassetti.turin.typesystem.TypeUsage;

import java.util.*;
import java.util.stream.Collectors;

/**
 * It could represent also a reference to a Type Variable.
 */
public class ReferenceTypeUsageNode extends TypeUsageWrapperNode {

    private List<TypeUsage> typeParams;
    private String name;
    private TypeDefinition cachedTypeDefinition;

    public ReferenceTypeUsageNode(String name) {
        if (JvmNameUtils.isPrimitiveTypeName(name)) {
            throw new IllegalArgumentException(name);
        }
        if (!JvmNameUtils.isValidQualifiedName(name)) {
            throw new IllegalArgumentException(name);
        }
        this.name = name;
        this.typeParams = Collections.emptyList();
    }

    @Override
    public TypeUsage typeUsage() {
        if (typeUsage == null) {
            SymbolResolver resolver = ResolverRegistry.INSTANCE.requireResolver(this);
            typeUsage = new ReferenceTypeUsage(getTypeDefinition(resolver));
        }
        return super.typeUsage();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReferenceTypeUsageNode)) return false;

        ReferenceTypeUsageNode that = (ReferenceTypeUsageNode) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (typeParams != null ? !typeParams.equals(that.typeParams) : that.typeParams != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "ReferenceTypeUsageNode{" +
                "typeParams=" + typeParams +
                ", name='" + name + '\'' +
                ", cachedTypeDefinition=" + cachedTypeDefinition +
                '}';
    }

    public TypeDefinition getTypeDefinition(SymbolResolver resolver) {
        if (cachedTypeDefinition != null) {
            return cachedTypeDefinition;
        }
        TypeDefinition typeDefinition = resolver.getRoot().getTypeDefinitionIn(this.name, this);
        return typeDefinition;
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of();
    }

    public String getQualifiedName(SymbolResolver resolver) {
        return getTypeDefinition(resolver).getQualifiedName();
    }

    public List<ReferenceTypeUsage> getAllAncestors(SymbolResolver resolver) {
        // TODO perhaps some generic type substitution needs to be done
        return getTypeDefinition(resolver).getAllAncestors(resolver);
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
    public Optional<List<? extends FormalParameter>> findFormalParametersFor(Invokable invokable, SymbolResolver resolver) {
        if (invokable instanceof FunctionCall) {
            FunctionCall functionCall = (FunctionCall)invokable;
            TypeDefinition typeDefinition = getTypeDefinition(resolver);
            return Optional.of(typeDefinition.getMethodParams(functionCall.getName(), invokable.getActualParams(), resolver, functionCall.isStatic(resolver)));
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public boolean sameType(TypeUsage other, SymbolResolver resolver) {
        if (!other.isReferenceTypeUsage()) {
            return false;
        }
        return getQualifiedName(resolver).equals(other.asReferenceTypeUsage().getQualifiedName(resolver));
    }

    public Map<String, TypeUsage> typeParamsMap(SymbolResolver resolver) {
        return getTypeDefinition(resolver).associatedTypeParametersToName(resolver, typeParams);
    }

    @Override
    public TypeUsageNode copy() {
        ReferenceTypeUsageNode copy = new ReferenceTypeUsageNode(name);
        copy.parent = this.parent;
        copy.cachedTypeDefinition = this.cachedTypeDefinition;
        copy.typeParams = this.typeParams;
        return copy;
    }

}
