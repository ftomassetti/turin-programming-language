package me.tomassetti.turin.symbols;

import me.tomassetti.turin.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.QualifiedName;
import me.tomassetti.turin.parser.ast.expressions.Invokable;
import me.tomassetti.turin.typesystem.TypeUsage;

import java.util.List;
import java.util.Optional;

/**
 * Something generic: it could come from an AST or be a loaded value.
 */
public interface Symbol {

    TypeUsage calcType();

    /**
     * Is this symbol an AST node?
     */
    default boolean isNode() {
        return false;
    }

    default Node asNode() {
        throw new UnsupportedOperationException();
    }

    default Symbol getField(String fieldName, SymbolResolver resolver) {
        throw new UnsupportedOperationException();
    }

    default Symbol getField(QualifiedName fieldsPath, SymbolResolver resolver) {
        if (fieldsPath.isSimpleName()) {
            return getField(fieldsPath.getName(), resolver);
        } else {
            Symbol next = getField(fieldsPath.firstSegment(), resolver);
            return next.getField(fieldsPath.rest(), resolver);
        }
    }

    default Optional<List<? extends FormalParameter>> findFormalParametersFor(Invokable invokable, SymbolResolver resolver) {
        throw new UnsupportedOperationException();
    }
}
