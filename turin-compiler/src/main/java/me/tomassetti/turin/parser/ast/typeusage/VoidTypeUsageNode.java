package me.tomassetti.turin.parser.ast.typeusage;

import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.typesystem.TypeUsage;
import me.tomassetti.turin.typesystem.VoidTypeUsage;

import java.util.Collections;
import java.util.Map;

public class VoidTypeUsageNode extends TypeUsageWrapperNode {

    public VoidTypeUsageNode() {
        super(new VoidTypeUsage());
    }

    @Override
    public TypeUsageNode copy() {
        return this;
    }

    @Override
    public Iterable<Node> getChildren() {
        return Collections.emptyList();
    }
}
