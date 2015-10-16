package me.tomassetti.turin.symbols;

import me.tomassetti.turin.typesystem.TypeUsage;

import java.util.List;

public interface InvokableDefinition extends Symbol {
    TypeUsage getReturnType();

    List<? extends FormalParameter> getParameters();

    String getName();
}
