package me.tomassetti.turin.symbols;

import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsageNode;
import me.tomassetti.turin.typesystem.TypeUsage;

/**
 * Something generic: it could come from an AST or be a loaded value.
 */
public interface Symbol {

    TypeUsage calcType(SymbolResolver resolver);

    /**
     * Is this symbol an AST node?
     */
    default boolean isNode() {
        return false;
    }

    default Node asNode() {
        throw new UnsupportedOperationException();
    }
}
