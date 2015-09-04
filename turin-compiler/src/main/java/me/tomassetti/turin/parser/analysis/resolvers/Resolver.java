package me.tomassetti.turin.parser.analysis.resolvers;

import me.tomassetti.turin.jvm.JvmMethodDefinition;
import me.tomassetti.turin.parser.ast.*;
import me.tomassetti.turin.parser.ast.expressions.FunctionCall;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

public interface Resolver {

    /**
     * Given a PropertyReference it finds the corresponding declaration.
     */
    PropertyDefinition findDefinition(PropertyReference propertyReference);

    /**
     * @param typeName can be a simple name or a canonical name. Note that is not legal to pass a primitive type name
     *                 because it is not a valid identifier and there are no TypeDefinition associated
     */
    TypeDefinition findTypeDefinitionIn(String typeName, Node context);

    /**
     * @param typeName can be a simple name or a canonical name. It is legal to pass a primitive type name.
     */
    TypeUsage findTypeUsageIn(String typeName, Node context);

    /**
     * Find the JVM method to invoke.
     */
    JvmMethodDefinition findJvmDefinition(FunctionCall functionCall);
}
