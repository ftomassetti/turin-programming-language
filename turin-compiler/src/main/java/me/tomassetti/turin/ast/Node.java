package me.tomassetti.turin.ast;

public abstract class Node {

    protected Node parent;

    public Node getParent() {
        return parent;
    }

    public abstract Iterable<Node> getChildren();

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

}
