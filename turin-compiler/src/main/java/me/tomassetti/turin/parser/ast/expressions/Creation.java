package me.tomassetti.turin.parser.ast.expressions;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.compiler.ParamUtils;
import me.tomassetti.turin.jvm.JvmConstructorDefinition;
import me.tomassetti.turin.parser.analysis.Property;
import me.tomassetti.turin.parser.analysis.UnsolvedConstructorException;
import me.tomassetti.turin.parser.analysis.UnsolvedTypeException;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.FormalParameter;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.TurinTypeDefinition;
import me.tomassetti.turin.parser.ast.TypeDefinition;
import me.tomassetti.turin.parser.ast.expressions.literals.StringLiteral;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

import java.util.*;
import java.util.stream.Collectors;


public class Creation extends Invokable {

    private String typeName;

    public String getTypeName() {
        return typeName;
    }

    @Override
    public String toString() {
        return "Creation{" +
                "typeName='" + typeName + '\'' +
                ", actualParams=" + actualParams +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Creation creation = (Creation) o;

        if (!actualParams.equals(creation.actualParams)) return false;
        if (!typeName.equals(creation.typeName)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = typeName.hashCode();
        result = 31 * result + actualParams.hashCode();
        return result;
    }

    @Override
    public boolean isOnOverloaded(SymbolResolver resolver) {
        return resolver.getTypeDefinitionIn(typeName, this, resolver).hasManyConstructors();
    }

    @Override
    protected List<FormalParameter> formalParameters(SymbolResolver resolver) {
        TypeDefinition typeDefinition = resolver.getTypeDefinitionIn(typeName, this, resolver);
        return typeDefinition.getConstructorParams(actualParams, resolver);
    }

    public Creation(String typeName, List<ActualParam> actualParams) {
        super(actualParams);
        this.typeName = typeName;
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.copyOf(actualParams);
    }

    @Override
    public TypeUsage calcType(SymbolResolver resolver) {
        // this node will not have a context so we resolve the type already
        Optional<TypeUsage> typeUsage = resolver.findTypeUsageIn(typeName, this, resolver);
        if (!typeUsage.isPresent()) {
            throw new UnsolvedTypeException(typeName, this);
        }
        return typeUsage.get();
    }

    public JvmConstructorDefinition jvmDefinition(SymbolResolver resolver) {
        return resolver.getTypeDefinitionIn(typeName, this, resolver).resolveConstructorCall(resolver, originalParams);
    }



}
