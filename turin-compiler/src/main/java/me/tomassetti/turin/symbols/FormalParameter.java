package me.tomassetti.turin.symbols;

import me.tomassetti.turin.typesystem.TypeUsage;

public interface FormalParameter extends Symbol {

    boolean hasDefaultValue();

    TypeUsage getType();

    String getName();
}
