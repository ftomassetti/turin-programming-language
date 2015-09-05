package me.tomassetti.turin.parser.ast.statements;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.expressions.Expression;

public class ReturnStatement extends Statement {
    private Expression value;

    public ReturnStatement(Expression value) {
        this.value = value;
        this.value.setParent(this);
    }

    public Expression getValue() {
        return value;
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of(value);
    }

    public boolean hasValue() {
        return value != null;
    }
}
