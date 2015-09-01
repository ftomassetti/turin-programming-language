package me.tomassetti.turin.parser.ast;

import me.tomassetti.turin.parser.analysis.JvmType;
import me.tomassetti.turin.parser.analysis.Resolver;

public abstract class TypeUsage extends Node {

    public abstract JvmType jvmType(Resolver resolver);

    public boolean isReferenceTypeUsage() {
        return false;
    }

    public ReferenceTypeUsage asReferenceTypeUsage() {
        throw new UnsupportedOperationException();
    }

}
