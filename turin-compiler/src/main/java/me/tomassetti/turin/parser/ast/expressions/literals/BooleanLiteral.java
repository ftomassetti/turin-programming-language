package me.tomassetti.turin.parser.ast.expressions.literals;

import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.expressions.Expression;
import me.tomassetti.turin.typesystem.PrimitiveTypeUsage;
import me.tomassetti.turin.typesystem.TypeUsage;

import java.util.Collections;

public class BooleanLiteral extends Expression {

    private boolean value;

    public BooleanLiteral(boolean value) {
        this.value = value;
    }

    @Override
    public TypeUsage calcType() {
        return PrimitiveTypeUsage.BOOLEAN;
    }

    @Override
    public Iterable<Node> getChildren() {
        return Collections.emptyList();
    }

    public boolean getValue() {
        return value;
    }

}
