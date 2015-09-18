package me.tomassetti.turin.compiler;

import me.tomassetti.turin.parser.ast.Node;

public class SemanticErrorException extends RuntimeException {
    private Node node;
    private String description;

    public Node getNode() {
        return node;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SemanticErrorException that = (SemanticErrorException) o;

        if (!description.equals(that.description)) return false;
        if (!node.equals(that.node)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = node.hashCode();
        result = 31 * result + description.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "SemanticErrorException{" +
                "node=" + node +
                ", description='" + description + '\'' +
                '}';
    }

    public String getDescription() {
        return description;
    }

    public SemanticErrorException(Node node, String description) {
        this.node = node;
        this.description = description;
    }
}
