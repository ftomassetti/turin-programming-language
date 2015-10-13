package me.tomassetti.turin.parser.analysis.resolvers;

import me.tomassetti.turin.parser.ast.invokables.FunctionDefinition;
import me.tomassetti.turin.parser.ast.TypeDefinition;

import java.util.Optional;

public interface TypeResolver {

    public Optional<TypeDefinition> resolveAbsoluteTypeName(String typeName);
    public Optional<FunctionDefinition> resolveAbsoluteFunctionName(String typeName);

    boolean existPackage(String packageName);
}
