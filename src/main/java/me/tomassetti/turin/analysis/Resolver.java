package me.tomassetti.turin.analysis;

import me.tomassetti.turin.ast.PropertyDefinition;
import me.tomassetti.turin.ast.PropertyReference;

public interface Resolver {

    public PropertyDefinition findDefinition(PropertyReference propertyReference);

}
