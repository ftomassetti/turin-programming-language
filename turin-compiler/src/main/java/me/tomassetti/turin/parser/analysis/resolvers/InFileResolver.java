package me.tomassetti.turin.parser.analysis.resolvers;

import me.tomassetti.turin.jvm.JvmMethodDefinition;
import me.tomassetti.turin.jvm.JvmNameUtils;
import me.tomassetti.turin.jvm.JvmType;
import me.tomassetti.turin.parser.analysis.UnsolvedSymbolException;
import me.tomassetti.turin.parser.analysis.UnsolvedTypeException;
import me.tomassetti.turin.parser.ast.*;
import me.tomassetti.turin.implicit.BasicTypeUsage;
import me.tomassetti.turin.parser.ast.expressions.Expression;
import me.tomassetti.turin.parser.ast.expressions.FunctionCall;
import me.tomassetti.turin.parser.ast.reflection.ReflectionTypeDefinitionFactory;
import me.tomassetti.turin.parser.ast.typeusage.PrimitiveTypeUsage;
import me.tomassetti.turin.parser.ast.typeusage.ReferenceTypeUsage;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

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
            throw new UnsolvedSymbolException(propertyReference);
        }
        return findDefinitionIn(propertyReference, context.getParent());
    }

    @Override
    public TypeDefinition findTypeDefinitionIn(String typeName, Node context) {
        // primitive names are not valid here
        if (!JvmNameUtils.isValidQualifiedName(typeName)) {
            throw new IllegalArgumentException(typeName);
        }
        Optional<TypeDefinition> result = findTypeDefinitionInHelper(typeName, context);
        if (result.isPresent()) {
            return result.get();
        } else {
            throw new UnsolvedTypeException(typeName, context);
        }
    }

    @Override
    public TypeUsage findTypeUsageIn(String typeName, Node context) {
        if (PrimitiveTypeUsage.isPrimitiveTypeName(typeName)){
            return PrimitiveTypeUsage.getByName(typeName);
        }
        // note that this check has to come after the check for primitive types
        if (!JvmNameUtils.isValidQualifiedName(typeName)) {
            throw new IllegalArgumentException(typeName);
        }

        // Note that our Turin basic types shadow other types
        Optional<BasicTypeUsage> basicType = BasicTypeUsage.findByName(typeName);
        if (basicType.isPresent()) {
            return basicType.get();
        }

        return new ReferenceTypeUsage(findTypeDefinitionIn(typeName, context));
    }

    @Override
    public JvmMethodDefinition findJvmDefinition(FunctionCall functionCall) {
        List<JvmType> argsTypes = functionCall.getActualParamValuesInOrder().stream()
                .map((ap) -> ap.calcType(this).jvmType(this))
                .collect(Collectors.toList());
        Expression function = functionCall.getFunction();
        boolean staticContext = function.isType(this);
        return function.findMethodFor(argsTypes, this, staticContext);
    }

    private Optional<TypeDefinition> findTypeDefinitionInHelper(String typeName, Node context) {
        if (!JvmNameUtils.isValidQualifiedName(typeName)) {
            throw new IllegalArgumentException(typeName);
        }
        for (Node child : context.getChildren()) {
            if (child instanceof TypeDefinition) {
                TypeDefinition typeDefinition = (TypeDefinition)child;
                if (typeDefinition.getName().equals(typeName)
                        || typeDefinition.getQualifiedName().equals(typeName)) {
                    return Optional.of(typeDefinition);
                }
            }
        }
        if (context.getParent() == null) {
            // implicitly look into java.lang package
            Optional<TypeDefinition> result = resolveAbsoluteTypeName("java.lang." + typeName);
            if (result.isPresent()) {
                return result;
            }

            return resolveAbsoluteTypeName(typeName);
        }
        return findTypeDefinitionInHelper(typeName, context.getParent());
    }

    private Optional<TypeDefinition> resolveAbsoluteTypeName(String typeName) {
        if (!JvmNameUtils.isValidQualifiedName(typeName)) {
            throw new IllegalArgumentException(typeName);
        }
        return ReflectionTypeDefinitionFactory.getInstance().findTypeDefinition(typeName);
    }

}
