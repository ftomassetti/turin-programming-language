package me.tomassetti.turin.symbols;

import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.QualifiedName;
import me.tomassetti.turin.parser.ast.expressions.InvokableExpr;
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

    default Symbol getField(String fieldName) {
        throw new UnsupportedOperationException(this.getClass().getCanonicalName());
    }

    default Symbol getField(QualifiedName fieldsPath) {
        if (fieldsPath.isSimpleName()) {
            return getField(fieldsPath.getName());
        } else {
            Symbol next = getField(fieldsPath.firstSegment());
            return next.getField(fieldsPath.rest());
        }
    }

    default Optional<List<? extends FormalParameter>> findFormalParametersFor(InvokableExpr invokable) {
        throw new UnsupportedOperationException();
    }
}
