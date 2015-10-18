package me.tomassetti.turin.resolvers;

import me.tomassetti.turin.definitions.TypeDefinition;
import me.tomassetti.turin.parser.ast.invokables.FunctionDefinitionNode;

import java.util.List;
import java.util.Optional;

public class ComposedTypeResolver implements TypeResolver {

    private List<TypeResolver> elements;

    public ComposedTypeResolver(List<TypeResolver> elements) {
        this.elements = elements;
        this.elements.forEach((e)->e.setRoot(ComposedTypeResolver.this));
    }

    protected SymbolResolver symbolResolver;

    public SymbolResolver symbolResolver() {
        SymbolResolver symbolResolver;
        if (this.root() == this || this.root() == null) {
            symbolResolver = this.symbolResolver;
        } else {
            symbolResolver = this.root().symbolResolver();
        }
        if (symbolResolver == null) {
            TypeResolver typeResolver = this.root();
            if (typeResolver == null) {
                typeResolver = this;
            }
            return new InFileSymbolResolver(typeResolver);
        }
        return symbolResolver.getRoot();
    }

    @Override
    public void setSymbolResolver(SymbolResolver symbolResolver) {
        this.symbolResolver = symbolResolver;
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

    private TypeResolver root;

    public TypeResolver root() {
        return root;
    }

    public void setRoot(TypeResolver root) {
        this.root = root;
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
