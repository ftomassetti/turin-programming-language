package me.tomassetti.turin.parser.analysis.resolvers;

import me.tomassetti.turin.parser.ast.TypeDefinition;

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
}
