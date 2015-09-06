package me.tomassetti.turin.parser.ast.statements;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.expressions.Expression;

public class ElifClause extends Node {

    private Expression condition;
    private BlockStatement body;

    public ElifClause(Expression condition, BlockStatement body) {
        this.condition = condition;
        this.condition.setParent(this);
        this.body = body;
        this.body.setParent(this);
    }

    public Expression getCondition() {
        return condition;
    }

    public BlockStatement getBody() {
        return body;
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of(condition, body);
    }
}
