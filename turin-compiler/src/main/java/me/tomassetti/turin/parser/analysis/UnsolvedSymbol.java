package me.tomassetti.turin.parser.analysis;

import me.tomassetti.turin.parser.ast.Node;

/**
 * Some symbol of some sort has not being solved.
 */
public class UnsolvedSymbol extends RuntimeException {

    private Node node;

    public UnsolvedSymbol(Node node) {
        super("Unresolved " + node);
        this.node = node;
    }
}
