package me.tomassetti.turin.ast;

import me.tomassetti.turin.analysis.Resolver;

public abstract class TypeUsage extends Node {

    public abstract String jvmType(Resolver resolver);

}
