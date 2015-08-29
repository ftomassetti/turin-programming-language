package me.tomassetti.turin.analysis;

import me.tomassetti.turin.ast.Node;
import me.tomassetti.turin.ast.PropertyDefinition;
import me.tomassetti.turin.ast.PropertyReference;

/**
 * Created by federico on 29/08/15.
 */
public class InFileResolver implements Resolver {

    @Override
    public PropertyDefinition findDefinition(PropertyReference propertyReference) {
        return findDefinitionIn(propertyReference, propertyReference.getParent());
    }

    private PropertyDefinition findDefinitionIn(PropertyReference propertyReference, Node context) {
        for (Node child : context.getChildren()) {
            if (child instanceof PropertyDefinition) {
                PropertyDefinition propertyDefinition = (PropertyDefinition)child;
                if (propertyDefinition.getName().equals(propertyReference.getName())) {
                    return propertyDefinition;
                }
            }
        }
        if (context.getParent() == null) {
            throw new Unresolved(propertyReference);
        }
        return findDefinitionIn(propertyReference, context.getParent());
    }

}
