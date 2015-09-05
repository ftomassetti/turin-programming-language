package me.tomassetti.turin.parser.ast;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.ast.statements.BlockStatement;
import me.tomassetti.turin.parser.ast.statements.Statement;

import java.util.ArrayList;
import java.util.List;

public class Program extends Node {

    private String name;
    private Statement statement;

    public Program(String name, Statement statement) {
        this.name = name;
        this.statement = statement;
        this.statement.setParent(this);
    }

    public String getName() {
        return name;
    }

    public Statement getStatement() {
        return statement;
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of(statement);
    }

    @Override
    public String toString() {
        return "Program{" +
                "name='" + name + '\'' +
                ", statement=" + statement +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Program program = (Program) o;

        if (!name.equals(program.name)) return false;
        if (!statement.equals(program.statement)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + statement.hashCode();
        return result;
    }

    public String getQualifiedName() {
        return contextName() + "." + name;
    }
}
