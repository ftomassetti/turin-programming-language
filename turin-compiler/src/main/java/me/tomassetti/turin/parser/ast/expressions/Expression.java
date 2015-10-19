package me.tomassetti.turin.parser.ast.expressions;

import me.tomassetti.jvm.JvmMethodDefinition;
import me.tomassetti.jvm.JvmType;
import me.tomassetti.turin.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.typesystem.TypeUsage;

import java.util.List;

public abstract class Expression extends Node {
    public abstract TypeUsage calcType();

    /**
     * When the expression corresponds to something invokable this method find which Jvm method corresponds to the call
     * with the given parameters.
     */
    public JvmMethodDefinition findMethodFor(List<ActualParam> argsTypes, SymbolResolver resolver, boolean staticContext) {
        throw new UnsupportedOperationException("On " + this.getClass().getCanonicalName());
    }

    /**
     * This expression represents a type?
     */
    public boolean isType(SymbolResolver resolver) {
        return false;
    }

    public boolean canBeAssigned(SymbolResolver resolver) {
        return false;
    }

    public boolean canFieldBeAssigned(String field, SymbolResolver resolver) {
        throw new UnsupportedOperationException(this.getClass().getCanonicalName());
    }
}
