package me.tomassetti.turin.compiler;

import me.tomassetti.turin.parser.ast.Node;

public class SemanticErrorException extends RuntimeException {
    private Node node;
    private String description;

    public SemanticErrorException(Node node, String description) {
        this.node = node;
        this.description = description;
    }
}
