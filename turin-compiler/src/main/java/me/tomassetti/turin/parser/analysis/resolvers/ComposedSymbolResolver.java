package me.tomassetti.turin.parser.analysis.resolvers;

import me.tomassetti.turin.jvm.JvmMethodDefinition;
import me.tomassetti.turin.parser.analysis.UnsolvedException;
import me.tomassetti.turin.parser.analysis.UnsolvedMethodException;
import me.tomassetti.turin.parser.analysis.UnsolvedSymbolException;
import me.tomassetti.turin.parser.analysis.UnsolvedTypeException;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.PropertyDefinition;
import me.tomassetti.turin.parser.ast.PropertyReference;
import me.tomassetti.turin.parser.ast.TypeDefinition;
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
    public PropertyDefinition findDefinition(PropertyReference propertyReference) {
        for (SymbolResolver element : elements) {
            try {
                PropertyDefinition definition = element.findDefinition(propertyReference);
                return definition;
            } catch (UnsolvedException re) {
                // Ignore
            }
        }
        throw new UnsolvedSymbolException(propertyReference);
    }

    @Override
    public Optional<TypeDefinition> findTypeDefinitionIn(String typeName, Node context, SymbolResolver resolver) {
        for (SymbolResolver element : elements) {
            try {
                TypeDefinition definition = element.getTypeDefinitionIn(typeName, context, resolver);
                return Optional.of(definition);
            } catch (UnsolvedException re) {
                // Ignore
            }
        }
        return Optional.empty();
    }

    @Override
    public TypeUsage findTypeUsageIn(String typeName, Node context, SymbolResolver resolver) {
        for (SymbolResolver element : elements) {
            try {
                TypeUsage typeUsage = element.findTypeUsageIn(typeName, context, resolver);
                return typeUsage;
            } catch (UnsolvedException re) {
                // Ignore
            }
        }
        throw new UnsolvedTypeException(typeName, context);
    }

    @Override
    public JvmMethodDefinition findJvmDefinition(FunctionCall functionCall) {
        for (SymbolResolver element : elements) {
            try {
                return element.findJvmDefinition(functionCall);
            } catch (UnsolvedException re) {
                // Ignore
            }
        }
        throw new UnsolvedMethodException(functionCall);
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
