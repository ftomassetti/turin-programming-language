package me.tomassetti.turin.parser.ast;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.analysis.resolvers.Resolver;
import me.tomassetti.turin.parser.ast.statements.Statement;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

import java.util.List;
import java.util.Optional;

public class MethodDefinition extends Node {

    private String name;
    private TypeUsage returnType;
    private List<FormalParameter> parameters;
    private Statement body;

    public TypeUsage getReturnType() {
        return returnType;
    }

    public List<FormalParameter> getParameters() {
        return parameters;
    }

    public Statement getBody() {
        return body;
    }

    public MethodDefinition(String name, TypeUsage returnType, List<FormalParameter> parameters, Statement body) {
        this.name = name;
        this.returnType = returnType;
        this.returnType.parent = this;
        this.parameters = parameters;
        this.parameters.forEach((p) -> p.parent = MethodDefinition.this );
        this.body = body;
        this.body.parent = this;
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.<Node>builder().add(returnType).addAll(parameters).add(body).build();
    }

    @Override
    public Optional<Node> findSymbol(String name, Resolver resolver) {
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
}
