package me.tomassetti.turin.parser.analysis.resolvers;

import me.tomassetti.turin.jvm.JvmMethodDefinition;
import me.tomassetti.turin.parser.analysis.UnsolvedException;
import me.tomassetti.turin.parser.analysis.UnsolvedMethodException;
import me.tomassetti.turin.parser.analysis.UnsolvedSymbolException;
import me.tomassetti.turin.parser.analysis.UnsolvedTypeException;
import me.tomassetti.turin.parser.ast.*;
import me.tomassetti.turin.parser.ast.expressions.FunctionCall;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Combine several resolvers.
 */
public class ComposedSymbolResolver implements SymbolResolver {

    private List<SymbolResolver> elements = new ArrayList<>();

    public ComposedSymbolResolver(List<SymbolResolver> elements) {
        this.elements = elements;
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
    public Optional<Node> findSymbol(String name, Node context) {
        for (SymbolResolver element : elements) {
            Optional<Node> res = element.findSymbol(name, context);
            if (res.isPresent()) {
                return res;
            }
        }
        return Optional.empty();
    }

}
