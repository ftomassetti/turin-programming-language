package me.tomassetti.turin.symbols;

import me.tomassetti.turin.typesystem.TypeUsage;

import java.util.Map;

public interface FormalParameter extends Symbol {

    boolean hasDefaultValue();

    TypeUsage getType();

    String getName();

    FormalParameter apply(Map<String, TypeUsage> typeParams);
}
