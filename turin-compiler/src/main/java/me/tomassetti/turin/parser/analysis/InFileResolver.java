package me.tomassetti.turin.parser.analysis;

import me.tomassetti.turin.parser.ast.*;
import me.tomassetti.turin.implicit.BasicTypes;
import me.tomassetti.turin.parser.ast.expressions.Expression;
import me.tomassetti.turin.parser.ast.expressions.FunctionCall;
import me.tomassetti.turin.parser.ast.reflection.ReflectionTypeDefinitionFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
        if (typeName.contains("/")) {
            throw new IllegalArgumentException(typeName);
        }
        Optional<TypeDefinition> result = findTypeDefinitionInHelper(typeName, context);
        if (result.isPresent()) {
            return result.get();
        } else {
            throw new UnresolvedType(typeName, context);
        }
    }

    @Override
    public JvmMethodDefinition findJvmDefinition(FunctionCall functionCall) {
        List<JvmType> argsTypes = functionCall.getActualParamValuesInOrder().stream().map((ap)->ap.calcType(this).jvmType(this)).collect(Collectors.toList());
        Expression function = functionCall.getFunction();
        boolean staticContext = function.isType(this);
        return function.findMethodFor(argsTypes, this, staticContext);
    }

    private Optional<TypeDefinition> findTypeDefinitionInHelper(String typeName, Node context) {
        if (typeName.contains("/")) {
            throw new IllegalArgumentException(typeName);
        }
        if (typeName.startsWith(".")) {
            throw new IllegalArgumentException(typeName);
        }
        for (Node child : context.getChildren()) {
            if (child instanceof TypeDefinition) {
                TypeDefinition typeDefinition = (TypeDefinition)child;
                if (typeDefinition.getName().equals(typeName)) {
                    return Optional.of(typeDefinition);
                }
            }
        }
        if (context.getParent() == null) {
            Optional<TypeDefinition> basicType = BasicTypes.getBasicType(typeName);
            if (basicType.isPresent()) {
                return basicType;
            } else {
                // implicitly look into java.lang package
                Optional<TypeDefinition> result = resolveAbsoluteTypeName("java.lang." + typeName);
                if (result.isPresent()) {
                    return result;
                }

                return resolveAbsoluteTypeName(typeName);
            }
        }
        return findTypeDefinitionInHelper(typeName, context.getParent());
    }

    private Optional<TypeDefinition> resolveAbsoluteTypeName(String typeName) {
        if (typeName.contains("/")) {
            throw new IllegalArgumentException(typeName);
        }
        return ReflectionTypeDefinitionFactory.getInstance().findTypeDefinition(typeName);
    }

}
