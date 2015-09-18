package me.tomassetti.turin.parser.ast.expressions.literals;

import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.expressions.Expression;
import me.tomassetti.turin.parser.ast.typeusage.PrimitiveTypeUsage;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

import java.util.Collections;

public class FloatLiteral extends Expression {

    private float value;

    public FloatLiteral(float value) {
        this.value = value;
    }

    @Override
    public TypeUsage calcType(SymbolResolver resolver) {
        return PrimitiveTypeUsage.FLOAT;
    }

    @Override
    public Iterable<Node> getChildren() {
        return Collections.emptyList();
    }
}
