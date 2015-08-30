package me.tomassetti.turin.parser.ast.expressions;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.ast.Node;

/**
 * Created by federico on 29/08/15.
 */
public class ActualParam extends Node {
    private String name;
    private Expression value;

    public String getName() {
        return name;
    }

    public Expression getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ActualParam that = (ActualParam) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (!value.equals(that.value)) return false;

        return true;
    }

    @Override
    public String toString() {
        return "ActualParam{" +
                "name='" + name + '\'' +
                ", value=" + value +
                '}';
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + value.hashCode();
        return result;
    }

    public ActualParam(Expression value) {
        this.value = value;
        this.value.setParent(this);
    }

    public ActualParam(String name, Expression value) {
        this.name = name;
        this.value = value;
        this.value.setParent(this);
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of(value);
    }
}
