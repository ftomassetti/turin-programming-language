package me.tomassetti.turin.parser.analysis;

import me.tomassetti.turin.jvm.JvmMethodDefinition;
import me.tomassetti.turin.parser.ast.*;
import me.tomassetti.turin.parser.ast.expressions.FunctionCall;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

public interface Resolver {

    PropertyDefinition findDefinition(PropertyReference propertyReference);

    TypeDefinition findTypeDefinitionIn(String typeName, Node context);

    TypeUsage findTypeUsageIn(String typeName, Node context);

    JvmMethodDefinition findJvmDefinition(FunctionCall functionCall);
}
