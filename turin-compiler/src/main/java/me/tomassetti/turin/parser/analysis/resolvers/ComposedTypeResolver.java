package me.tomassetti.turin.parser.analysis.resolvers;

import me.tomassetti.turin.definitions.TypeDefinition;
import me.tomassetti.turin.parser.ast.invokables.FunctionDefinitionNode;

import java.util.List;
import java.util.Optional;

public class ComposedTypeResolver implements TypeResolver {

    private List<TypeResolver> elements;

    public ComposedTypeResolver(List<TypeResolver> elements) {
        this.elements = elements;
    }

    @Override
    public Optional<TypeDefinition> resolveAbsoluteTypeName(String typeName) {
        for (TypeResolver element : elements) {
            Optional<TypeDefinition> partial = element.resolveAbsoluteTypeName(typeName);
            if (partial.isPresent()) {
                return partial;
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<FunctionDefinitionNode> resolveAbsoluteFunctionName(String typeName) {
        for (TypeResolver element : elements) {
            Optional<FunctionDefinitionNode> partial = element.resolveAbsoluteFunctionName(typeName);
            if (partial.isPresent()) {
                return partial;
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean existPackage(String packageName) {
        for (TypeResolver element : elements) {
            if (element.existPackage(packageName)) {
                return true;
            }
        }
        return false;
    }
}
