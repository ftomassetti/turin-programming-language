package me.tomassetti.turin.resolvers;

import me.tomassetti.jvm.JvmMethodDefinition;
import me.tomassetti.turin.definitions.TypeDefinition;
import me.tomassetti.turin.parser.ast.*;
import me.tomassetti.turin.parser.ast.expressions.FunctionCall;
import me.tomassetti.turin.parser.ast.properties.PropertyDefinition;
import me.tomassetti.turin.parser.ast.properties.PropertyReference;
import me.tomassetti.turin.symbols.Symbol;
import me.tomassetti.turin.typesystem.TypeUsage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Combine several resolvers.
 */
public class ComposedSymbolResolver implements SymbolResolver {

    private List<SymbolResolver> elements = new ArrayList<>();

    private SymbolResolver parent = null;

    @Override
    public SymbolResolver getParent() {
        return parent;
    }

    @Override
    public void setParent(SymbolResolver parent) {
        this.parent = parent;
    }

    public ComposedSymbolResolver(List<SymbolResolver> elements) {
        this.elements = elements;
        this.elements.forEach((e)->e.setParent(ComposedSymbolResolver.this));
    }

    @Override
    public Optional<PropertyDefinition> findDefinition(PropertyReference propertyReference) {
        for (SymbolResolver element : elements) {
            Optional<PropertyDefinition> definition = element.findDefinition(propertyReference);
            if (definition.isPresent()) {
                return definition;
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<TypeDefinition> findTypeDefinitionIn(String typeName, Node context, SymbolResolver resolver) {
        for (SymbolResolver element : elements) {
            Optional<TypeDefinition> definition = element.findTypeDefinitionIn(typeName, context, resolver);
            if (definition.isPresent()) {
                return definition;
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<TypeUsage> findTypeUsageIn(String typeName, Node context, SymbolResolver resolver) {
        for (SymbolResolver element : elements) {
            Optional<TypeUsage> typeUsage = element.findTypeUsageIn(typeName, context, resolver);
            if (typeUsage.isPresent()) {
                return typeUsage;
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<JvmMethodDefinition> findJvmDefinition(FunctionCall functionCall) {
        for (SymbolResolver element : elements) {
            Optional<JvmMethodDefinition> partial = element.findJvmDefinition(functionCall);
            if (partial.isPresent()) {
                return partial;
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Symbol> findSymbol(String name, Node context) {
        for (SymbolResolver element : elements) {
            Optional<Symbol> res = element.findSymbol(name, context);
            if (res.isPresent()) {
                return res;
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean existPackage(String packageName) {
        for (SymbolResolver element : elements) {
            if (element.existPackage(packageName)) {
                return true;
            }
        }
        return false;
    }

}
