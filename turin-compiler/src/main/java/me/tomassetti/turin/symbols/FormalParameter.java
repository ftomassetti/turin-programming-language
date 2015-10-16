package me.tomassetti.turin.symbols;

import me.tomassetti.turin.typesystem.TypeUsage;

public interface FormalParameter {

    boolean hasDefaultValue();

    TypeUsage getType();

    String getName();
}
