package me.tomassetti.turin.parser.ast;

import me.tomassetti.turin.parser.analysis.JvmMethodDefinition;
import me.tomassetti.turin.parser.analysis.JvmType;
import me.tomassetti.turin.parser.analysis.Resolver;

import java.util.List;

public abstract class TypeUsage extends Node {

    public abstract JvmType jvmType(Resolver resolver);

    public boolean isReferenceTypeUsage() {
        return false;
    }

    public ReferenceTypeUsage asReferenceTypeUsage() {
        throw new UnsupportedOperationException();
    }

    public JvmMethodDefinition findMethodFor(String name, List<JvmType> argsTypes, Resolver resolve, boolean staticContext) {
        throw new UnsupportedOperationException(this.getClass().getCanonicalName());
    }
}
