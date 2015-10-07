package me.tomassetti.turin.parser.ast;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;
import me.tomassetti.turin.parser.ast.statements.BlockStatement;
import me.tomassetti.turin.parser.ast.statements.Statement;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;
import me.tomassetti.turin.parser.ast.typeusage.VoidTypeUsage;

import java.util.List;

/**
 * Definition of a method in a Turin Type.
 */
public class TurinTypeContructorDefinition extends InvokableDefinition {

    public Statement getBody() {
        return body;
    }

    public TurinTypeContructorDefinition(List<FormalParameter> parameters, BlockStatement body) {
        super(parameters, body, "<init>", new VoidTypeUsage());
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.<Node>builder()
                .addAll(parameters)
                .add(body)
                .build();
    }
}
