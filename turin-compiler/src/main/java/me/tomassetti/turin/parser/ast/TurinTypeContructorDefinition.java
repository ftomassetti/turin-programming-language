package me.tomassetti.turin.parser.ast;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;
import me.tomassetti.turin.parser.ast.statements.Statement;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

import java.util.List;

/**
 * Definition of a method in a Turin Type.
 */
public class TurinTypeContructorDefinition extends Node {

    private List<FormalParameter> parameters;
    private List<ActualParam> superParameters;
    private Statement body;

    public TurinTypeContructorDefinition(List<FormalParameter> parameters, List<ActualParam> superParameters, Statement body) {
        this.parameters = parameters;
        this.superParameters = superParameters;
        this.body = body;
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.<Node>builder()
                .addAll(parameters)
                .addAll(superParameters)
                .add(body)
                .build();
    }
}
