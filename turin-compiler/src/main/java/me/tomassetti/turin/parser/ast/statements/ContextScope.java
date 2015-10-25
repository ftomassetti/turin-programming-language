package me.tomassetti.turin.parser.ast.statements;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.ast.Node;

import java.util.List;

public class ContextScope extends Statement {

    private List<ContextAssignment> assignments;
    private List<Statement> statements;

    public ContextScope(List<ContextAssignment> assignments, List<Statement> statements) {
        this.assignments = assignments;
        this.assignments.forEach((a)->a.setParent(ContextScope.this));
        this.statements = statements;
        this.statements.forEach((s)->s.setParent(ContextScope.this));
    }

    public List<Statement> getStatements() {
        return statements;
    }

    public List<ContextAssignment> getAssignments() {
        return assignments;
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.<Node>builder()
                .addAll(assignments)
                .addAll(statements)
                .build();
    }
}
