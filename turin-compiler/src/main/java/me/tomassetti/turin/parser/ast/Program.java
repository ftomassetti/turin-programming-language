package me.tomassetti.turin.parser.ast;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.analysis.resolvers.Resolver;
import me.tomassetti.turin.parser.ast.statements.BlockStatement;
import me.tomassetti.turin.parser.ast.statements.Statement;
import me.tomassetti.turin.parser.ast.typeusage.ArrayTypeUsage;
import me.tomassetti.turin.parser.ast.typeusage.ReferenceTypeUsage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Program extends Node {

    private String name;
    private Statement statement;
    private FormalParameter formalParameter;

    public Program(String name, Statement statement, String paramName) {
        this.name = name;
        this.statement = statement;
        this.statement.setParent(this);
        this.formalParameter = new FormalParameter(new ArrayTypeUsage(ReferenceTypeUsage.STRING), paramName);
        this.formalParameter.parent = this;
    }

    public String getName() {
        return name;
    }

    public Statement getStatement() {
        return statement;
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of(formalParameter, statement);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Program program = (Program) o;

        if (!formalParameter.equals(program.formalParameter)) return false;
        if (!name.equals(program.name)) return false;
        if (!statement.equals(program.statement)) return false;

        return true;
    }

    @Override
    public String toString() {
        return "Program{" +
                "name='" + name + '\'' +
                ", statement=" + statement +
                ", formalParameter=" + formalParameter +
                '}';
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + statement.hashCode();
        result = 31 * result + formalParameter.hashCode();
        return result;
    }

    @Override
    public Optional<Node> findSymbol(String name, Resolver resolver) {
        if (name.equals(formalParameter.getName())) {
            return Optional.of(formalParameter);
        }
        return super.findSymbol(name, resolver);
    }

    public String getQualifiedName() {
        return contextName() + "." + name;
    }
}
