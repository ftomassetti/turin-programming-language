package me.tomassetti.turin.parser.analysis;

import me.tomassetti.turin.parser.ast.*;
import me.tomassetti.turin.parser.ast.expressions.FunctionCall;

public interface Resolver {

    public PropertyDefinition findDefinition(PropertyReference propertyReference);

    public TypeDefinition findTypeDefinitionIn(String typeName, Node context);

    JvmMethodDefinition findJvmDefinition(FunctionCall functionCall);
}
