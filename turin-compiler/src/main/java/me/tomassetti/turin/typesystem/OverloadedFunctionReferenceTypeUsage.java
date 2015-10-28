package me.tomassetti.turin.typesystem;

import java.util.List;

public abstract class OverloadedFunctionReferenceTypeUsage implements TypeUsage, Invokable {

    protected List<InvokableReferenceTypeUsage> alternatives;

    OverloadedFunctionReferenceTypeUsage(List<InvokableReferenceTypeUsage> alternatives) {
        if (alternatives.size() < 2) {
            throw new IllegalArgumentException();
        }
        this.alternatives = alternatives;
    }

    @Override
    public Invokable asInvokable() {
        return this;
    }

    @Override
    public boolean isInvokable() {
        return true;
    }
}
