package me.tomassetti.turin.parser.ast.literals;

import me.tomassetti.turin.parser.analysis.Resolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.expressions.Expression;
import me.tomassetti.turin.parser.ast.typeusage.PrimitiveTypeUsage;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

import java.util.Collections;

public class BooleanLiteral extends Expression {

    private boolean value;

    public BooleanLiteral(boolean value) {
        this.value = value;
    }

    @Override
    public TypeUsage calcType(Resolver resolver) {
        return PrimitiveTypeUsage.BOOLEAN;
    }

    @Override
    public Iterable<Node> getChildren() {
        return Collections.emptyList();
    }
}
