package me.tomassetti.turin.parser.ast.expressions.literals;

import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.expressions.Expression;
import me.tomassetti.turin.parser.ast.typeusage.PrimitiveTypeUsage;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsageNode;

import java.util.Collections;

public class BooleanLiteral extends Expression {

    private boolean value;

    public BooleanLiteral(boolean value) {
        this.value = value;
    }

    @Override
    public TypeUsageNode calcType(SymbolResolver resolver) {
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
