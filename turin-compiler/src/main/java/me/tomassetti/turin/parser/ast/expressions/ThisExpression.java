package me.tomassetti.turin.parser.ast.expressions;

import me.tomassetti.turin.definitions.TypeDefinition;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.TurinTypeDefinition;
import me.tomassetti.turin.typesystem.ReferenceTypeUsage;

import java.util.Collections;

public class ThisExpression extends Expression {

    public ThisExpression() {

    }

    @Override
    public String toString() {
        return "ThisExpression{}";
    }

    @Override
    public Iterable<Node> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public ReferenceTypeUsage calcType() {
        TurinTypeDefinition turinTypeDefinition = getParentOfType(TurinTypeDefinition.class);
        return new ReferenceTypeUsage(turinTypeDefinition);
    }

    @Override
    public boolean canFieldBeAssigned(String field, SymbolResolver resolver) {
        TypeDefinition typeDefinition = calcType().getTypeDefinition();
        return typeDefinition.canFieldBeAssigned(field, resolver);
    }
}
