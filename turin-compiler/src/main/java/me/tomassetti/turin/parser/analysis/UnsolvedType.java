package me.tomassetti.turin.parser.analysis;

import me.tomassetti.turin.parser.ast.Node;

public class UnsolvedType extends RuntimeException {
    public UnsolvedType(String typeName, Node context) {
        super("type " + typeName + " not solved in " + context);
    }
}
