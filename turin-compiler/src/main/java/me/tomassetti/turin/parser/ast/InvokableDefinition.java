package me.tomassetti.turin.parser.ast;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.statements.Statement;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

import java.util.List;
import java.util.Optional;

/**
 * Either a function or a method.
 */
public abstract class InvokableDefinition extends Node {
    protected String name;
    protected TypeUsage returnType;
    protected List<FormalParameter> parameters;
    protected Statement body;

    public InvokableDefinition(List<FormalParameter> parameters, Statement body, String name, TypeUsage returnType) {
        this.parameters = parameters;
        this.parameters.forEach((p) -> p.parent = InvokableDefinition.this );
        this.body = body;
        this.body.parent = this;
        this.name = name;
        this.returnType = returnType;
        this.returnType.parent = this;
    }

    public TypeUsage getReturnType() {
        return returnType;
    }

    public List<FormalParameter> getParameters() {
        return parameters;
    }

    public Statement getBody() {
        return body;
    }

    @Override
    public Optional<Node> findSymbol(String name, SymbolResolver resolver) {
        for (FormalParameter param : parameters) {
            if (param.getName().equals(name)) {
                return Optional.of(param);
            }
        }
        return super.findSymbol(name, resolver);
    }

    public String getName() {
        return name;
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.<Node>builder().add(returnType).addAll(parameters).add(body).build();
    }
}
