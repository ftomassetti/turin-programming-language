package me.tomassetti.turin.typesystem;

import java.util.List;

public abstract class OverloadedFunctionReferenceTypeUsage implements InvokableTypeUsage {

    protected List<FunctionReferenceTypeUsage> alternatives;

    OverloadedFunctionReferenceTypeUsage(List<FunctionReferenceTypeUsage> alternatives) {
        if (alternatives.size() < 2) {
            throw new IllegalArgumentException();
        }
        this.alternatives = alternatives;
    }

}
