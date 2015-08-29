package me.tomassetti.turin.parser.ast;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.ast.statements.Statement;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by federico on 29/08/15.
 */
public class Program extends Node {

    private String name;
    private List<Statement> statements = new ArrayList<>();

    public Program(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<Statement> getStatements() {
        return ImmutableList.copyOf(statements);
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.copyOf(statements);
    }

    public void add(Statement statement) {
        statements.add(statement);
        statement.parent = this;
    }

    @Override
    public String toString() {
        return "Program{" +
                "name='" + name + '\'' +
                ", statements=" + statements +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Program program = (Program) o;

        if (!name.equals(program.name)) return false;
        if (!statements.equals(program.statements)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + statements.hashCode();
        return result;
    }

    public String getQualifiedName() {
        return contextName() + "." + name;
    }
}
