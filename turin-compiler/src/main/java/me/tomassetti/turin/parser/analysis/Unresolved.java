package me.tomassetti.turin.parser.analysis;

import me.tomassetti.turin.parser.ast.Node;

/**
 * Created by federico on 29/08/15.
 */
public class Unresolved extends RuntimeException {

    private Node node;

    public Unresolved(Node node) {
        super("Unresolved " + node);
        this.node = node;
    }
}
