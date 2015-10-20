package me.tomassetti.turin.typesystem;

import me.tomassetti.turin.definitions.InternalInvokableDefinition;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;

import java.util.List;
import java.util.Optional;

/**
 * This is a type which represents something invokable.
 */
public interface InvokableType {

    TypeUsage returnTypeWhenInvokedWith(List<ActualParam> actualParams);
    boolean isOverloaded();
    Optional<? extends InternalInvokableDefinition> internalInvokableDefinitionFor(List<ActualParam> actualParams);
}
