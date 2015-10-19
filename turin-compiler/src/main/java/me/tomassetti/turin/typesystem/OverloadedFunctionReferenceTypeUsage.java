package me.tomassetti.turin.typesystem;

import java.util.List;

public abstract class OverloadedFunctionReferenceTypeUsage implements TypeUsage, InvokableType {

    protected List<FunctionReferenceTypeUsage> alternatives;

    OverloadedFunctionReferenceTypeUsage(List<FunctionReferenceTypeUsage> alternatives) {
        if (alternatives.size() < 2) {
            throw new IllegalArgumentException();
        }
        this.alternatives = alternatives;
    }

    @Override
    public InvokableType asInvokable() {
        return this;
    }

    @Override
    public boolean isInvokable() {
        return true;
    }
}
