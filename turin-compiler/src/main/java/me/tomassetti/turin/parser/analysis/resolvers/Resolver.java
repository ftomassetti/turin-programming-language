package me.tomassetti.turin.parser.analysis.resolvers;

import me.tomassetti.turin.jvm.JvmMethodDefinition;
import me.tomassetti.turin.parser.analysis.UnsolvedSymbolException;
import me.tomassetti.turin.parser.ast.*;
import me.tomassetti.turin.parser.ast.expressions.FunctionCall;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

import java.util.Optional;

public interface Resolver {

    /**
     * Given a PropertyReference it finds the corresponding declaration.
     */
    PropertyDefinition findDefinition(PropertyReference propertyReference);

    /**
     * @param typeName can be a simple name or a canonical name. Note that is not legal to pass a primitive type name
     *                 because it is not a valid identifier and there are no TypeDefinition associated
     * @param resolver top level resolver used during compilation
     */
    default TypeDefinition getTypeDefinitionIn(String typeName, Node context, Resolver resolver) {
        Optional<TypeDefinition> result = findTypeDefinitionIn(typeName, context, resolver);
        if (result.isPresent()) {
            return result.get();
        } else {
            throw new UnsolvedSymbolException(context, typeName);
        }
    }

    /**
     * @param typeName can be a simple name or a canonical name. Note that is not legal to pass a primitive type name
     *                 because it is not a valid identifier and there are no TypeDefinition associated
     * @param resolver top level resolver used during compilation
     */
    Optional<TypeDefinition> findTypeDefinitionIn(String typeName, Node context, Resolver resolver);

    /**
     * @param typeName can be a simple name or a canonical name. It is legal to pass a primitive type name.
     * @param resolver top level resolver used during compilation
     */
    TypeUsage findTypeUsageIn(String typeName, Node context, Resolver resolver);

    /**
     * Find the JVM method to invoke.
     */
    JvmMethodDefinition findJvmDefinition(FunctionCall functionCall);

    Optional<Node> findSymbol(String name, Node context);
}
