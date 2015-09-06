package me.tomassetti.turin.parser.ast.statements;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.ast.Node;

import java.util.List;

public class BlockStatement extends Statement {

    private List<Statement> statements;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BlockStatement that = (BlockStatement) o;

        if (!statements.equals(that.statements)) return false;

        return true;
    }

    public List<Statement> getStatements() {
        return statements;
    }

    @Override
    public String toString() {
        return "BlockStatement{" +
                "statements=" + statements +
                '}';
    }

    @Override
    public int hashCode() {
        return statements.hashCode();
    }

    public BlockStatement(List<Statement> statements) {
        this.statements = statements;
        this.statements.forEach((s) -> s.setParent(BlockStatement.this));
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.copyOf(statements);
    }

    public List<Statement> findPreeceding(Statement statement) {
        for (int i=0;i<statements.size();i++){
            // we look for exactly that statement
            if (statements.get(i) == statement) {
                return statements.subList(0, i);
            }
        }
        throw new IllegalArgumentException(statement.describe());
    }
}
