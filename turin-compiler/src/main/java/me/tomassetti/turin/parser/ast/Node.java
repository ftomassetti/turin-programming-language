package me.tomassetti.turin.parser.ast;

import me.tomassetti.turin.parser.analysis.resolvers.Resolver;
import me.tomassetti.turin.parser.ast.statements.BlockStatement;

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

    public Optional<Node> findSymbol(String name, Resolver resolver) {
        if (parent == null) {
            return Optional.empty();
        } else {
            return parent.findSymbol(name, resolver);
        }
    }

    public final Node getField(QualifiedName fieldsPath, Resolver resolver) {
        if (fieldsPath.isSimpleName()) {
            return getField(fieldsPath.getName(), resolver);
        } else {
            Node next = getField(fieldsPath.firstSegment(), resolver);
            return next.getField(fieldsPath.rest(), resolver);
        }
    }

    public String describe() {
        return this.toString();
    }

    /**
     * This is intended in a broad way: everything that can be accessed with a dot.
     */
    public Node getField(String fieldName, Resolver resolver) {
        throw new UnsupportedOperationException("It is not possible to get field of " + describe());
    }
}
