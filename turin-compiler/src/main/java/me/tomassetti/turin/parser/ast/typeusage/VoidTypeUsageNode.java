package me.tomassetti.turin.parser.ast.typeusage;

import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.typesystem.TypeUsage;
import me.tomassetti.turin.typesystem.VoidTypeUsage;

import java.util.Collections;
import java.util.Map;

public class VoidTypeUsageNode extends TypeUsageWrapperNode {
    @Override
    public boolean isVoid() {
        return true;
    }

    @Override
    public <T extends TypeUsage> TypeUsage replaceTypeVariables(Map<String, T> typeParams) {
        return this;
    }

    @Override
    public TypeUsageNode copy() {
        return this;
    }

    @Override
    public TypeUsage typeUsage() {
        return new VoidTypeUsage();
    }

    @Override
    public Iterable<Node> getChildren() {
        return Collections.emptyList();
    }
}
