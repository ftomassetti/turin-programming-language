package me.tomassetti.turin.parser.analysis.resolvers;

import me.tomassetti.turin.parser.ast.invokables.FunctionDefinition;
import me.tomassetti.turin.parser.ast.NodeTypeDefinition;

import java.util.List;
import java.util.Optional;

public class ComposedTypeResolver implements TypeResolver {

    private List<TypeResolver> elements;

    public ComposedTypeResolver(List<TypeResolver> elements) {
        this.elements = elements;
    }

    @Override
    public Optional<NodeTypeDefinition> resolveAbsoluteTypeName(String typeName) {
        for (TypeResolver element : elements) {
            Optional<NodeTypeDefinition> partial = element.resolveAbsoluteTypeName(typeName);
            if (partial.isPresent()) {
                return partial;
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<FunctionDefinition> resolveAbsoluteFunctionName(String typeName) {
        for (TypeResolver element : elements) {
            Optional<FunctionDefinition> partial = element.resolveAbsoluteFunctionName(typeName);
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
