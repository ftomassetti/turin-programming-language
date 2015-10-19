package me.tomassetti.turin.typesystem;

import me.tomassetti.turin.parser.ast.expressions.ActualParam;

import java.util.List;

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

}
