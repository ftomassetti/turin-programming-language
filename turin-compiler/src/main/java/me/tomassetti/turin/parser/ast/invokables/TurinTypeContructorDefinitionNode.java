package me.tomassetti.turin.parser.ast.invokables;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.ast.FormalParameterNode;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.statements.BlockStatement;
import me.tomassetti.turin.parser.ast.statements.Statement;
import me.tomassetti.turin.parser.ast.typeusage.VoidTypeUsageNode;

import java.util.List;

/**
 * Definition of a method in a Turin Type.
 */
public class TurinTypeContructorDefinitionNode extends InvokableDefinitionNode {

    public Statement getBody() {
        return body;
    }

    public TurinTypeContructorDefinitionNode(List<FormalParameterNode> parameters, BlockStatement body) {
        super(parameters, body, "<init>", new VoidTypeUsageNode());
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.<Node>builder()
                .addAll(parameters)
                .add(body)
                .build();
    }
}
