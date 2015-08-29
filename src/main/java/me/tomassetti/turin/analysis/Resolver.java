package me.tomassetti.turin.analysis;

import me.tomassetti.turin.ast.Node;
import me.tomassetti.turin.ast.PropertyDefinition;
import me.tomassetti.turin.ast.PropertyReference;
import me.tomassetti.turin.ast.TypeDefinition;

public interface Resolver {

    public PropertyDefinition findDefinition(PropertyReference propertyReference);

    public TypeDefinition findTypeDefinitionIn(String typeName, Node context);

}
