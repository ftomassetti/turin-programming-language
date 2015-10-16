package me.tomassetti.turin.parser.analysis.resolvers;

import me.tomassetti.turin.parser.ast.invokables.FunctionDefinitionNode;
import me.tomassetti.turin.parser.ast.TypeDefinition;

import java.util.Optional;

public interface TypeResolver {

    public Optional<TypeDefinition> resolveAbsoluteTypeName(String typeName);
    public Optional<FunctionDefinitionNode> resolveAbsoluteFunctionName(String typeName);

    boolean existPackage(String packageName);
}
