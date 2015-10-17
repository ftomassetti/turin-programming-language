package me.tomassetti.turin.parser.ast.expressions.literals;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.expressions.Expression;
import me.tomassetti.turin.typesystem.PrimitiveTypeUsage;
import me.tomassetti.turin.typesystem.TypeUsage;

public class LongLiteral extends Expression {

    private long value;

    public LongLiteral(long value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "LongLiteral{" +
                "value=" + value +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LongLiteral that = (LongLiteral) o;

        if (value != that.value) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(value);
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of();
    }

    @Override
    public TypeUsage calcType() {
        return PrimitiveTypeUsage.LONG;
    }

    public long getValue() {
        return value;
    }
}
