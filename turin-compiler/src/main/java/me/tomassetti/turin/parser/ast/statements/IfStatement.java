package me.tomassetti.turin.parser.ast.statements;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.expressions.Expression;

import java.util.List;

public class IfStatement extends Statement {

    private Expression condition;
    private BlockStatement ifBody;
    private List<ElifClause> elifStatements;
    private BlockStatement elseBody;

    public Expression getCondition() {
        return condition;
    }

    public BlockStatement getIfBody() {
        return ifBody;
    }

    public List<ElifClause> getElifStatements() {
        return elifStatements;
    }

    public BlockStatement getElseBody() {
        if (elseBody == null) {
            throw new UnsupportedOperationException();
        }
        return elseBody;
    }

    public IfStatement(Expression condition, BlockStatement ifBody, List<ElifClause> elifClauses, BlockStatement elseBody) {
        this.condition = condition;
        this.condition.setParent(this);
        this.ifBody = ifBody;
        this.ifBody.setParent(this);
        this.elifStatements = elifClauses;
        this.elifStatements.forEach((s)->s.setParent(IfStatement.this));
        this.elseBody = elseBody;
        this.elseBody.setParent(this);
    }

    public IfStatement(Expression condition, BlockStatement ifBody, List<ElifClause> elifClauses) {
        this.condition = condition;
        this.condition.setParent(this);
        this.ifBody = ifBody;
        this.ifBody.setParent(this);
        this.elifStatements = elifClauses;
        this.elifStatements.forEach((s)->s.setParent(IfStatement.this));
    }

    public boolean hasElse() {
        return elseBody != null;
    }

    @Override
    public Iterable<Node> getChildren() {
        ImmutableList.Builder<Node> lb = ImmutableList.<Node>builder();
        lb.add(condition);
        lb.add(ifBody);
        lb.addAll(elifStatements);
        if (elseBody != null) {
            lb.add(elseBody);
        }
        return lb.build();
    }
}
