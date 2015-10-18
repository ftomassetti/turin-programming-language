package me.tomassetti.turin.parser.ast;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.statements.Statement;
import me.tomassetti.turin.symbols.FormalParameterSymbol;
import me.tomassetti.turin.symbols.Symbol;
import me.tomassetti.turin.typesystem.ArrayTypeUsage;
import me.tomassetti.turin.typesystem.ReferenceTypeUsage;

import java.util.Optional;

public class Program extends Node implements Named, Symbol {

    private String name;
    private Statement statement;
    private FormalParameterSymbol formalParameter;
    private String paramName;

    public FormalParameterSymbol getFormalParameter() {
        if (formalParameter == null) {
            formalParameter = new FormalParameterSymbol(new ArrayTypeUsage(ReferenceTypeUsage.STRING(symbolResolver())), paramName);
        }
        return formalParameter;
    }

    public String getParamName() {
        return paramName;
    }

    public Program(String name, Statement statement, String paramName) {
        this.name = name;
        this.statement = statement;
        this.statement.setParent(this);
        this.formalParameter = null;
        this.paramName = paramName;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Program program = (Program) o;

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
    public Optional<Symbol> findSymbol(String name, SymbolResolver resolver) {
        if (name.equals(paramName)) {
            return Optional.of(getFormalParameter());
        }
        return super.findSymbol(name, resolver);
    }

    public String getQualifiedName() {
        return contextName() + "." + name;
    }
}
