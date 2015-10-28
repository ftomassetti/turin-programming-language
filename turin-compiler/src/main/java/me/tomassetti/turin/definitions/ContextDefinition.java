package me.tomassetti.turin.definitions;

import me.tomassetti.turin.typesystem.TypeUsage;

public interface ContextDefinition {
    String getName();
    TypeUsage getType();

    String getClassQualifiedName();

    String getQualifiedName();
}
