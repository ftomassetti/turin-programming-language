package me.tomassetti.turin.parser.analysis;

import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.PropertyDefinition;
import me.tomassetti.turin.implicit.BasicTypes;
import me.tomassetti.turin.parser.ast.PropertyReference;
import me.tomassetti.turin.parser.ast.TypeDefinition;

import java.util.Optional;

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

    @Override
    public TypeDefinition findTypeDefinitionIn(String typeName, Node context) {
        return findTypeDefinitionInHelper(typeName, context, context);
    }

    private TypeDefinition findTypeDefinitionInHelper(String typeName, Node context, Node startContext) {
        for (Node child : context.getChildren()) {
            if (child instanceof TypeDefinition) {
                TypeDefinition typeDefinition = (TypeDefinition)child;
                if (typeDefinition.getName().equals(typeName)) {
                    return typeDefinition;
                }
            }
        }
        if (context.getParent() == null) {
            Optional<TypeDefinition> basicType = BasicTypes.getBasicType(typeName);
            if (basicType.isPresent()) {
                return basicType.get();
            } else {
                throw new UnresolvedType(typeName, startContext);
            }
        }
        return findTypeDefinitionInHelper(typeName, context.getParent(), startContext);
    }

}
