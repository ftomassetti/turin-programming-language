package me.tomassetti.turin.parser.ast;

import me.tomassetti.turin.parser.analysis.Resolver;

public abstract class TypeUsage extends Node {

    public abstract String jvmType(Resolver resolver);

    public boolean isReferenceTypeUsage() {
        return false;
    }

    public ReferenceTypeUsage asReferenceTypeUsage() {
        throw new UnsupportedOperationException();
    }

}
