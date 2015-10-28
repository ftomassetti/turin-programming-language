package me.tomassetti.turin.typesystem;

import me.tomassetti.turin.definitions.InternalInvokableDefinition;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;

import java.util.List;
import java.util.Optional;

/**
 * This represents something invokable.
 */
public interface Invokable {

    Optional<? extends InternalInvokableDefinition> internalInvokableDefinitionFor(List<ActualParam> actualParams);

    boolean isOverloaded();

}
