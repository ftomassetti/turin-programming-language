package me.tomassetti.turin.ast;

import me.tomassetti.turin.analysis.Resolver;

public abstract class TypeUsage extends Node {

    public abstract String jvmType(Resolver resolver);

    public boolean isReferenceTypeUsage() {
        return false;
    }

    public ReferenceTypeUsage asReferenceTypeUsage() {
        throw new UnsupportedOperationException();
    }

}
