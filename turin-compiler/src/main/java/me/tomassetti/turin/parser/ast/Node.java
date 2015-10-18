package me.tomassetti.turin.parser.ast;

import me.tomassetti.turin.compiler.errorhandling.ErrorCollector;
import me.tomassetti.turin.resolvers.ResolverRegistry;
import me.tomassetti.turin.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.expressions.Invokable;
import me.tomassetti.turin.parser.ast.statements.BlockStatement;
import me.tomassetti.turin.parser.ast.statements.Statement;
import me.tomassetti.turin.symbols.FormalParameter;
import me.tomassetti.turin.symbols.Symbol;
import me.tomassetti.turin.typesystem.TypeUsage;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * A Node the Abstract Syntax Tree.
 * Note that nodes are initially created by the parser but during compilation additional "virtual" nodes could be
 * created.
 */
public abstract class Node implements Symbol {

    protected Node parent;
    private Position position;
    private Boolean valid;

    @Override
    public boolean isNode() {
        return true;
    }

    @Override
    public Node asNode() {
        return this;
    }

    ///
    /// Resolver
    ///

    protected SymbolResolver symbolResolver() {
        return ResolverRegistry.INSTANCE.requireResolver(this);
    }

    ///
    /// Position
    ///

    public Position getPosition() {
        if (position == null) {
            throw new IllegalStateException(this.toString()+ " has no position assigned");
        }
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    ///
    /// Tree
    ///

    public Node getRoot() {
        if (isRoot()) {
            return this;
        } else {
            return getParent().getRoot();
        }
    }

    public boolean isRoot() {
        return parent == null;
    }

    public Node getParent() {
        return parent;
    }

    public abstract Iterable<Node> getChildren();

    public void setParent(Node parent){
        if (parent == this) {
            throw new IllegalArgumentException();
        }
        this.parent = parent;
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

    public <N extends Node> N getParentOfType(Class<N> parentClazz) {
        if (getParent() == null) {
            throw new IllegalStateException("It was expected to be contained in a " + parentClazz.getName());
        }
        if (parentClazz.isInstance(getParent())) {
            return parentClazz.cast(getParent());
        }
        return getParent().getParentOfType(parentClazz);
    }

    ///
    /// Naming
    ///

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

    ///
    /// Typing
    ///

    public TypeUsage calcType() {
        throw new UnsupportedOperationException(this.getClass().getCanonicalName());
    }

    ///
    /// Symbol resolution
    ///

    public Optional<Symbol> findSymbol(String name, SymbolResolver resolver) {
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
                Optional<Symbol> result = statement.findSymbol(name, resolver);
                if (result.isPresent()) {
                    return result;
                }
            }
            return parent.findSymbol(name, resolver);
        } else {
            return parent.findSymbol(name, resolver);
        }
    }

    public final Symbol getField(QualifiedName fieldsPath) {
        if (fieldsPath.isSimpleName()) {
            return getField(fieldsPath.getName());
        } else {
            Symbol next = getField(fieldsPath.firstSegment());
            return next.getField(fieldsPath.rest());
        }
    }

    public Optional<List<? extends FormalParameter>> findFormalParametersFor(Invokable invokable) {
        throw new UnsupportedOperationException(this.getClass().getCanonicalName());
    }

    /**
     * This is intended in a broad way: everything that can be accessed with a dot.
     */
    public Symbol getField(String fieldName) {
        return calcType().getInstanceField(fieldName, this);
    }

    ///
    /// Validation
    ///

    public final boolean validate(SymbolResolver resolver, ErrorCollector errorCollector) {
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
        valid = res;
        return res;
    }

    public boolean isValid() {
        if (valid == null) {
            throw new IllegalStateException("Not validated");
        }
        return valid;
    }

    /**
     * @return if the node is valid
     */
    protected boolean specificValidate(SymbolResolver resolver, ErrorCollector errorCollector) {
        // nothing to do
        return true;
    }

    ///
    /// Support
    ///

    public String describe() {
        return this.toString();
    }

}
