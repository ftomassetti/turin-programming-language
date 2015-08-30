package me.tomassetti.turin.parser.analysis;

import me.tomassetti.turin.parser.ast.Node;

public class UnresolvedType extends RuntimeException {
    public UnresolvedType(String typeName, Node context) {
        super("type " + typeName + " not solved in " + context);
    }
}
