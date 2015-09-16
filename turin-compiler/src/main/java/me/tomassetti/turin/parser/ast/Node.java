package me.tomassetti.turin.parser.ast;

import me.tomassetti.turin.compiler.errorhandling.ErrorCollector;
import me.tomassetti.turin.parser.analysis.resolvers.Resolver;
import me.tomassetti.turin.parser.ast.statements.BlockStatement;
import me.tomassetti.turin.parser.ast.statements.Statement;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
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

    public TypeUsage calcType(Resolver resolver) {
        throw new UnsupportedOperationException(this.getClass().getCanonicalName());
    }

    public Optional<Node> findSymbol(String name, Resolver resolver) {
        if (parent == null) {
            return Optional.empty();
        } else if (parent instanceof BlockStatement) {
            if (!(this instanceof Statement)) {
                throw new RuntimeException();
            }
            // This is a peculiar case because we have visibility only on the elements preceding this statement
            BlockStatement blockStatement = (BlockStatement)parent;
            List<Statement> preceedingStatements = blockStatement.findPreeceding((Statement)this);
            for (Statement statement : preceedingStatements) {
                Optional<Node> result = statement.findSymbol(name, resolver);
                if (result.isPresent()) {
                    return result;
                }
            }
            return parent.findSymbol(name, resolver);
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
        return calcType(resolver).getFieldOnInstance(fieldName, this, resolver);
    }

    public <T extends Node> List<T> findAll(Class<T> desiredClass) {
        List<T> results = new LinkedList<>();
        if (desiredClass.isInstance(this)) {
            results.add(desiredClass.cast(this));
        }
        for (Node child : getChildren()) {
            results.addAll(child.findAll(desiredClass));
        }
        return results;
    }

    public boolean validate(Resolver resolver, ErrorCollector errorCollector) {
        boolean res = specificValidate(resolver, errorCollector);
        // if the node is wrong we do not check its children
        if (res) {
            for (Node child : getChildren()) {
                boolean partial = child.validate(resolver, errorCollector);
                if (!partial) {
                    res = false;
                }
            }
        }
        return res;
    }

    /**
     * @return if the node is valid
     */
    protected boolean specificValidate(Resolver resolver, ErrorCollector errorCollector) {
        // nothing to do
        return true;
    }
}
