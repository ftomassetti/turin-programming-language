package me.tomassetti.turin.parser.analysis;

import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.PropertyDefinition;
import me.tomassetti.turin.parser.ast.PropertyReference;
import me.tomassetti.turin.parser.ast.TypeDefinition;

public interface Resolver {

    public PropertyDefinition findDefinition(PropertyReference propertyReference);

    public TypeDefinition findTypeDefinitionIn(String typeName, Node context);

}
