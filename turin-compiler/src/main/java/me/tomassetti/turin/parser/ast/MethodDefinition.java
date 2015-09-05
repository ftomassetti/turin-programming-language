package me.tomassetti.turin.parser.ast;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.ast.statements.Statement;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

import java.util.List;

public class MethodDefinition extends Node {

    private String name;
    private TypeUsage returnType;
    private List<FormalParameter> parameters;
    private Statement body;

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
}
