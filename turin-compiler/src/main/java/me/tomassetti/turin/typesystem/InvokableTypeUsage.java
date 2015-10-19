package me.tomassetti.turin.typesystem;

import me.tomassetti.turin.parser.ast.expressions.ActualParam;
import me.tomassetti.turin.symbols.FormalParameter;

import java.util.List;
import java.util.Optional;

/**
 * This is a type which represents something invokable.
 */
public interface InvokableTypeUsage extends TypeUsage {

    default boolean isInvokable() {
        return true;
    }

    default InvokableTypeUsage asInvokable() {
        return this;
    }

    TypeUsage returnTypeWhenInvokedWith(List<ActualParam> actualParams);

    boolean isOverloaded();

    default Optional<List<? extends FormalParameter>> findFormalParametersFor(List<ActualParam> actualParams) {
        throw new UnsupportedOperationException(this.getClass().getCanonicalName());
    }

}
