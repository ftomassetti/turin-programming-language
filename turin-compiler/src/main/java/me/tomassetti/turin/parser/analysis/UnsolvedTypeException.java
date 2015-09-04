package me.tomassetti.turin.parser.analysis;

import me.tomassetti.turin.parser.ast.Node;

public class UnsolvedTypeException extends UnsolvedException {

    public UnsolvedTypeException(String typeName, Node context) {
        super("type " + typeName + " not solved in " + context);
    }

}
