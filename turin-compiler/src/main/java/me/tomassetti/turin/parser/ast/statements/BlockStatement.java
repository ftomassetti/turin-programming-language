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
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.copyOf(statements);
    }
}
