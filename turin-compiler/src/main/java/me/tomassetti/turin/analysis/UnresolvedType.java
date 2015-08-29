package me.tomassetti.turin.analysis;

import me.tomassetti.turin.ast.Node;

/**
 * Created by federico on 29/08/15.
 */
public class UnresolvedType extends RuntimeException {
    public UnresolvedType(String typeName, Node context) {
        super("type " + typeName + " not solved in " + context);
    }
}
