package me.tomassetti.turin.parser.ast.typeusage;

import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.typesystem.BasicTypeUsage;

import java.util.Collections;

/**
 * NOTE: Being a Node we could need to have separate instances for each occurrence, so that each one can have a proper
 *       parent.
 */
public class BasicTypeUsageNode extends TypeUsageWrapperNode {

    public BasicTypeUsageNode(String name) {
        super(BasicTypeUsage.getByName(name));
    }

    @Override
    public TypeUsageNode copy() {
        return this;
    }

    @Override
    public Iterable<Node> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return "BasicTypeUsageNode{" + typeUsage + "}";
    }
}
