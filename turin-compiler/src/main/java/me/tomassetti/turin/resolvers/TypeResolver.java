package me.tomassetti.turin.resolvers;

import me.tomassetti.turin.definitions.TypeDefinition;
import me.tomassetti.turin.parser.ast.invokables.FunctionDefinitionNode;

import java.util.Optional;

public interface TypeResolver {

    public TypeResolver root();
    public void setRoot(TypeResolver root);

    public Optional<TypeDefinition> resolveAbsoluteTypeName(String typeName);
    public Optional<FunctionDefinitionNode> resolveAbsoluteFunctionName(String typeName);

    boolean existPackage(String packageName);

    SymbolResolver symbolResolver();
    void setSymbolResolver(SymbolResolver symbolResolver);
}
