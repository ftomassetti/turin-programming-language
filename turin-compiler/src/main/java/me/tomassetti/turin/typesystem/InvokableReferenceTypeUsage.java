package me.tomassetti.turin.typesystem;

import me.tomassetti.jvm.JvmType;
import me.tomassetti.turin.definitions.InternalInvokableDefinition;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A reference to something that has a first goal to be invoked.
 * This could represent a function for example.
 * It represents something not overloaded.
 */
public class InvokableReferenceTypeUsage implements TypeUsage, InvokableType {

    private InternalInvokableDefinition internalInvokableDefinition;

    @Override
    public <T extends TypeUsage> TypeUsage replaceTypeVariables(Map<String, T> typeParams) {
        throw new UnsupportedOperationException();
    }

    public InvokableReferenceTypeUsage(InternalInvokableDefinition internalInvokableDefinition) {
        this.internalInvokableDefinition = internalInvokableDefinition;
    }

    @Override
    public boolean sameType(TypeUsage other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isInvokable() {
        return true;
    }

    @Override
    public InvokableType asInvokable() {
        return this;
    }

    @Override
    public JvmType jvmType() {
        return internalInvokableDefinition.getReturnType().jvmType();
    }

    @Override
    public boolean canBeAssignedTo(TypeUsage type) {
        return false;
    }

    @Override
    public boolean isOverloaded() {
        return false;
    }

    @Override
    public Optional<InternalInvokableDefinition> internalInvokableDefinitionFor(List<ActualParam> actualParams) {
        return Optional.of(internalInvokableDefinition);
    }
}
