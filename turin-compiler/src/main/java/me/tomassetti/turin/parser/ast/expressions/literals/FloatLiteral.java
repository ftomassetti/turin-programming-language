package me.tomassetti.turin.parser.ast.expressions.literals;

import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.expressions.Expression;
import me.tomassetti.turin.parser.ast.typeusage.PrimitiveTypeUsageNode;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsageNode;
import me.tomassetti.turin.typesystem.PrimitiveTypeUsage;
import me.tomassetti.turin.typesystem.TypeUsage;

import java.util.Collections;

public class FloatLiteral extends Expression {

    private float value;

    public float getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "FloatLiteral{" +
                "value=" + value +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FloatLiteral)) return false;

        FloatLiteral that = (FloatLiteral) o;

        if (Float.compare(that.value, value) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (value != +0.0f ? Float.floatToIntBits(value) : 0);
    }

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
