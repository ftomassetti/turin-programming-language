package me.tomassetti.java.symbol_solver;

import java.util.Optional;

public interface JavaTypeResolver {
    public Optional<JavaTypeDefinition> resolveAbsoluteTypeName(String typeName);
    boolean existPackage(String packageName);
}
