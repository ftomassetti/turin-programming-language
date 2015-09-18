package me.tomassetti.turin.parser.ast.expressions;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.jvm.JvmConstructorDefinition;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

import java.util.*;


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
        return resolver.findTypeUsageIn(typeName, this, resolver);
    }

    public JvmConstructorDefinition jvmDefinition(SymbolResolver resolver) {
        return resolver.getTypeDefinitionIn(typeName, this, resolver).resolveConstructorCall(resolver, actualParams);
    }
}
