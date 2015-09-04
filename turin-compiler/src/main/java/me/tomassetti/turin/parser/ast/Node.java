package me.tomassetti.turin.parser.ast;

import java.util.Optional;

/**
 * A Node the Abstract Syntax Tree.
 * Note that nodes are initially created by the parser but during compilation additional "virtual" nodes could be
 * created.
 */
public abstract class Node {

    protected Node parent;
    private Position position;

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Node getParent() {
        return parent;
    }

    public abstract Iterable<Node> getChildren();

    public void setParent(Node parent){
        this.parent = parent;
    }

    public String contextName() {
        if (parent == null) {
            return "";
        }
        if (parent instanceof TurinFile) {
            TurinFile turinFile = (TurinFile)parent;
            return turinFile.getNamespaceDefinition().getName();
        }
        return parent.contextName();
    }

    public Optional<Node> findSymbol(String name) {
        if (parent == null) {
            return Optional.empty();
        } else {
            return parent.findSymbol(name);
        }
    }
}
