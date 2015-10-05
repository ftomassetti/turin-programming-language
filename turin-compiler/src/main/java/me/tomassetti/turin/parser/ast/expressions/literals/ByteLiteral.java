package me.tomassetti.turin.parser.ast.expressions.literals;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.expressions.Expression;
import me.tomassetti.turin.parser.ast.typeusage.PrimitiveTypeUsage;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

public class ByteLiteral extends Expression {

    private byte value;

    public ByteLiteral(byte value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "ByteLiteral{" +
                "value=" + value +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ByteLiteral that = (ByteLiteral) o;

        if (value != that.value) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return value;
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of();
    }

    @Override
    public TypeUsage calcType(SymbolResolver resolver) {
        return PrimitiveTypeUsage.BYTE;
    }

    public byte getValue() {
        return value;
    }
}
