package me.tomassetti.turin.parser.ast.statements;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.ast.Node;

import java.util.List;

public class TryCatchStatement extends Statement {

    private BlockStatement body;
    private List<CatchClause> catchClauses;

    public BlockStatement getBody() {
        return body;
    }

    public List<CatchClause> getCatchClauses() {
        return catchClauses;
    }

    public TryCatchStatement(BlockStatement body, List<CatchClause> catchClauses) {
        this.body = body;
        this.body.setParent(this);
        this.catchClauses = catchClauses;
        this.catchClauses.forEach((cc)->cc.setParent(this));
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.<Node>builder().add(body).addAll(catchClauses).build();
    }
}
