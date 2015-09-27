package me.tomassetti.turin.parser.ast.expressions;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.ast.Node;

public class ActualParam extends Node {
    private String name;
    private Expression value;
    private boolean asterisk;

    public String getName() {
        return name;
    }

    public Expression getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ActualParam)) return false;

        ActualParam that = (ActualParam) o;

        if (asterisk != that.asterisk) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (!value.equals(that.value)) return false;

        return true;
    }

    public boolean isAsterisk() {
        return asterisk;
    }

    @Override
    public String toString() {
        return "ActualParam{" +
                "name='" + name + '\'' +
                ", value=" + value +
                ", asterisk=" + asterisk +
                '}';
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + value.hashCode();
        result = 31 * result + (asterisk ? 1 : 0);
        return result;
    }

    public ActualParam(Expression value) {
        this.value = value;
        this.value.setParent(this);
    }

    public ActualParam(Expression value, boolean asterisk) {
        this.value = value;
        this.value.setParent(this);
        this.asterisk = asterisk;
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

    public boolean isNamed() {
        return name != null;
    }

    public ActualParam toUnnamed() {
        return new ActualParam(value);
    }
}
