package me.tomassetti.turin.parser.analysis;

import me.tomassetti.turin.parser.ast.Node;

/**
 * Some symbol of some sort has not being solved.
 */
public class UnsolvedSymbolException extends UnsolvedException {

    private Node node;

    public UnsolvedSymbolException(Node node) {
        super("Unsolved " + node);
        this.node = node;
    }
}
