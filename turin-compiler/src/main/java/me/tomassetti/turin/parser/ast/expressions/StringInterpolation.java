package me.tomassetti.turin.parser.ast.expressions;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.analysis.Resolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by federico on 01/09/15.
 */
public class StringInterpolation extends Expression {

    private List<Expression> elements = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StringInterpolation that = (StringInterpolation) o;

        if (!elements.equals(that.elements)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return elements.hashCode();
    }

    @Override
    public String toString() {
        return "StringInterpolation{" +
                "elements=" + elements +

                '}';
    }

    @Override
    public TypeUsage calcType(Resolver resolver) {
        throw new UnsupportedOperationException();
    }

    /**
     * Note that also the StringLiterals are treated as the interpolated values (both are Expressions).
     * @param interpolatedValue
     */
    public void add(Expression interpolatedValue) {
        elements.add(interpolatedValue);
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.copyOf(elements);
    }
}
