package me.tomassetti.turin.parser.ast.invokables;

import me.tomassetti.turin.parser.ast.FormalParameterNode;
import me.tomassetti.turin.parser.ast.statements.Statement;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsageNode;

import java.util.List;

/**
 * Definition of a method in a Turin Type.
 */
public class TurinTypeMethodDefinitionNode extends InvokableDefinitionNode {

    public TurinTypeMethodDefinitionNode(String name, TypeUsageNode returnType, List<FormalParameterNode> parameters, Statement body) {
        super(parameters, body, name, returnType);
        this.returnType.setParent(this);
        this.parameters.forEach((p) -> p.setParent(TurinTypeMethodDefinitionNode.this) );
        this.body.setParent(this);
    }

}
