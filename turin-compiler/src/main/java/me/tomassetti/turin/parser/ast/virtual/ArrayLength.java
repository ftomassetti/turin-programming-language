package me.tomassetti.turin.parser.ast.virtual;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.typeusage.PrimitiveTypeUsageNode;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsageNode;
import me.tomassetti.turin.typesystem.PrimitiveTypeUsage;
import me.tomassetti.turin.typesystem.TypeUsage;

public class ArrayLength extends Node {

    private Node array;

    public ArrayLength(Node array) {
        // non setting the parent of array on purpose
        this.array = array;
    }

    @Override
    public Iterable<Node> getChildren() {
        // This is intended: it has a reference to array but it is not its parent
        return ImmutableList.of();
    }

    @Override
    public TypeUsage calcType() {
        return PrimitiveTypeUsage.INT;
    }
}
