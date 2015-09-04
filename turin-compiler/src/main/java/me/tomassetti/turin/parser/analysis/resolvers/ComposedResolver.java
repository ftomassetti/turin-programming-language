package me.tomassetti.turin.parser.analysis.resolvers;

import me.tomassetti.turin.implicit.BasicTypeUsage;
import me.tomassetti.turin.jvm.JvmMethodDefinition;
import me.tomassetti.turin.jvm.JvmNameUtils;
import me.tomassetti.turin.jvm.JvmType;
import me.tomassetti.turin.parser.analysis.*;
import me.tomassetti.turin.parser.ast.*;
import me.tomassetti.turin.parser.ast.expressions.Expression;
import me.tomassetti.turin.parser.ast.expressions.FunctionCall;
import me.tomassetti.turin.parser.ast.reflection.ReflectionTypeDefinitionFactory;
import me.tomassetti.turin.parser.ast.typeusage.PrimitiveTypeUsage;
import me.tomassetti.turin.parser.ast.typeusage.ReferenceTypeUsage;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Combine several resolvers.
 */
public class ComposedResolver implements Resolver {

    private List<Resolver> elements = new ArrayList<>();

    public ComposedResolver(List<Resolver> elements) {
        this.elements = elements;
    }

    @Override
    public PropertyDefinition findDefinition(PropertyReference propertyReference) {
        for (Resolver element : elements) {
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
    public TypeDefinition findTypeDefinitionIn(String typeName, Node context) {
        for (Resolver element : elements) {
            try {
                TypeDefinition definition = element.findTypeDefinitionIn(typeName, context);
                return definition;
            } catch (UnsolvedException re) {
                // Ignore
            }
        }
        throw new UnsolvedTypeException(typeName, context);
    }

    @Override
    public TypeUsage findTypeUsageIn(String typeName, Node context) {
        for (Resolver element : elements) {
            try {
                TypeUsage typeUsage = element.findTypeUsageIn(typeName, context);
                return typeUsage;
            } catch (UnsolvedException re) {
                // Ignore
            }
        }
        throw new UnsolvedTypeException(typeName, context);
    }

    @Override
    public JvmMethodDefinition findJvmDefinition(FunctionCall functionCall) {
        for (Resolver element : elements) {
            try {
                return element.findJvmDefinition(functionCall);
            } catch (UnsolvedException re) {
                // Ignore
            }
        }
        throw new UnsolvedMethodException(functionCall);
    }

}
