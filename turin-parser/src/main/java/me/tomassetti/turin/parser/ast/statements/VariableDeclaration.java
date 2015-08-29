package me.tomassetti.turin.parser.ast.statements;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.TypeUsage;
import me.tomassetti.turin.parser.ast.expressions.Expression;

import java.util.Collections;

/**
 * Created by federico on 29/08/15.
 */
public class VariableDeclaration extends Statement {
    private TypeUsage type;
    private String name;
    private Expression value;

    public VariableDeclaration(String name, Expression value) {
        this.name = name;
        this.value = value;
        this.value.setParent(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VariableDeclaration that = (VariableDeclaration) o;

        if (!name.equals(that.name)) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (!value.equals(that.value)) return false;

        return true;
    }

    @Override
    public String toString() {
        return "VariableDeclaration{" +
                "type=" + type +
                ", name='" + name + '\'' +
                ", value=" + value +
                '}';
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + name.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }

    public VariableDeclaration(String name, Expression value, TypeUsage type) {
        this.name = name;
        this.type = type;
        this.type.setParent(this);
        this.value = value;
        this.value.setParent(this);
    }

    @Override
    public Iterable<Node> getChildren() {
        if (type != null) {
            return ImmutableList.of(type, value);
        } else {
            return ImmutableList.of(value);
        }
    }
}
