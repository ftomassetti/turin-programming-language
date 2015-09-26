package me.tomassetti.turin.parser.ast.expressions;

import me.tomassetti.turin.jvm.JvmMethodDefinition;
import me.tomassetti.turin.jvm.JvmType;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.FormalParameter;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.Optional;

public abstract class Expression extends Node {
    public abstract TypeUsage calcType(SymbolResolver resolver);

    /**
     * When the expression corresponds to something invokable this method find which Jvm method corresponds to the call
     * with the given parameters.
     */
    public JvmMethodDefinition findMethodFor(List<JvmType> argsTypes, SymbolResolver resolver, boolean staticContext) {
        throw new UnsupportedOperationException("On " + this.getClass().getCanonicalName());
    }

    /**
     * This expression represents a type?
     */
    public boolean isType(SymbolResolver resolver) {
        return false;
    }

}
