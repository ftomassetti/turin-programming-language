package me.tomassetti.turin.parser.analysis.resolvers;

import me.tomassetti.jvm.JvmMethodDefinition;
import me.tomassetti.jvm.JvmNameUtils;
import me.tomassetti.jvm.JvmType;
import me.tomassetti.turin.compiler.errorhandling.SemanticErrorException;
import me.tomassetti.turin.parser.ast.typeusage.BasicTypeUsageNode;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.TypeDefinition;
import me.tomassetti.turin.parser.ast.expressions.Expression;
import me.tomassetti.turin.parser.ast.expressions.FunctionCall;
import me.tomassetti.turin.parser.ast.expressions.StaticFieldAccess;
import me.tomassetti.turin.parser.ast.imports.ImportDeclaration;
import me.tomassetti.turin.parser.ast.invokables.FunctionDefinitionNode;
import me.tomassetti.turin.parser.ast.properties.PropertyDefinition;
import me.tomassetti.turin.parser.ast.properties.PropertyReference;
import me.tomassetti.turin.symbols.Symbol;
import me.tomassetti.turin.typesystem.PrimitiveTypeUsage;
import me.tomassetti.turin.typesystem.ReferenceTypeUsage;
import me.tomassetti.turin.typesystem.TypeUsage;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Resolve symbols by looking in the file where the context node is contained.
 */
public class InFileSymbolResolver implements SymbolResolver {

    private TypeResolver typeResolver;

    private SymbolResolver parent = null;

    @Override
    public SymbolResolver getParent() {
        return parent;
    }

    @Override
    public void setParent(SymbolResolver parent) {
        this.parent = parent;
    }

    public InFileSymbolResolver(TypeResolver typeResolver) {
        this.typeResolver = typeResolver;
    }

    @Override
    public Optional<PropertyDefinition> findDefinition(PropertyReference propertyReference) {
        return findDefinitionIn(propertyReference, propertyReference.getParent());
    }

    private Optional<PropertyDefinition> findDefinitionIn(PropertyReference propertyReference, Node context) {
        for (Node child : context.getChildren()) {
            if (child instanceof PropertyDefinition) {
                PropertyDefinition propertyDefinition = (PropertyDefinition)child;
                if (propertyDefinition.getName().equals(propertyReference.getName())) {
                    return Optional.of(propertyDefinition);
                }
            }
        }
        if (context.getParent() == null) {
            return Optional.empty();
        }
        return findDefinitionIn(propertyReference, context.getParent());
    }

    @Override
    public Optional<TypeDefinition> findTypeDefinitionIn(String typeName, Node context, SymbolResolver resolver) {
        // primitive names are not valid here
        if (!JvmNameUtils.isValidQualifiedName(typeName)) {
            throw new IllegalArgumentException(typeName);
        }
        return findTypeDefinitionInHelper(typeName, context, null, resolver);
    }

    @Override
    public Optional<TypeUsage> findTypeUsageIn(String typeName, Node context, SymbolResolver resolver) {
        if (PrimitiveTypeUsage.isPrimitiveTypeName(typeName)){
            return Optional.of(PrimitiveTypeUsage.getByName(typeName));
        }
        // note that this check has to come after the check for primitive types
        if (!JvmNameUtils.isValidQualifiedName(typeName)) {
            throw new IllegalArgumentException(typeName);
        }

        // Note that our Turin basic types could shadow other types
        Optional<BasicTypeUsageNode> basicType = BasicTypeUsageNode.findByName(typeName);
        if (basicType.isPresent()) {
            return Optional.of(basicType.get());
        }

        Optional<TypeDefinition> typeDefinition = findTypeDefinitionIn(typeName, context, resolver.getRoot());
        if (typeDefinition.isPresent()) {
            ReferenceTypeUsage ref = new ReferenceTypeUsage(typeDefinition.get());
            return Optional.of(ref);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<JvmMethodDefinition> findJvmDefinition(FunctionCall functionCall) {
        List<JvmType> argsTypes = functionCall.getActualParamValuesInOrder().stream()
                .map((ap) -> ap.calcType(this).jvmType(this))
                .collect(Collectors.toList());
        Expression function = functionCall.getFunction();
        boolean staticContext = function.isType(this) || (function instanceof StaticFieldAccess);
        return Optional.of(function.findMethodFor(argsTypes, this, staticContext));
    }

    @Override
    public Optional<Symbol> findSymbol(String name, Node context) {
        if (context == null) {
            Optional<TypeDefinition> typeDefinition = typeResolver.resolveAbsoluteTypeName(name);
            if (typeDefinition.isPresent()) {
                return Optional.of(typeDefinition.get());
            }
            Optional<FunctionDefinitionNode> functionDefinition = typeResolver.resolveAbsoluteFunctionName(name);
            if (functionDefinition.isPresent()) {
                return Optional.of(functionDefinition.get());
            }
            return Optional.empty();
        } else {
            return context.findSymbol(name, this);
        }
    }

    @Override
    public boolean existPackage(String packageName) {
        return typeResolver.existPackage(packageName);
    }

    private Optional<TypeDefinition> findTypeDefinitionInHelper(String typeName, Node context,
                                                                Node previousContext, SymbolResolver resolver) {
        if (!JvmNameUtils.isValidQualifiedName(typeName)) {
            throw new IllegalArgumentException(typeName);
        }
        if (context == null) {
            // implicitly look into java.lang package
            Optional<TypeDefinition> result = typeResolver.resolveAbsoluteTypeName("java.lang." + typeName);
            if (result.isPresent()) {
                return result;
            }

            return typeResolver.resolveAbsoluteTypeName(typeName);
        }
        for (Node child : context.getChildren()) {
            if (child instanceof TypeDefinition) {
                TypeDefinition typeDefinition = (TypeDefinition)child;
                if (typeDefinition.getName().equals(typeName)
                        || typeDefinition.getQualifiedName().equals(typeName)) {
                    return Optional.of(typeDefinition);
                }
            } else if (child instanceof ImportDeclaration) {
                // this is necessary to avoid infinite recursion
                if (child != previousContext) {
                    ImportDeclaration importDeclaration = (ImportDeclaration) child;
                    Optional<Symbol> resolvedNode = importDeclaration.findAmongImported(typeName, resolver);
                    if (resolvedNode.isPresent()) {
                        if (resolvedNode.get() instanceof TypeDefinition) {
                            return Optional.of((TypeDefinition) resolvedNode.get());
                        } else {
                            throw new SemanticErrorException(context, "" + typeName + " is not a type");
                        }
                    }
                }
            }
        }
        if (!context.contextName().isEmpty()) {
            String qName = context.contextName() + "." + typeName;
            Optional<TypeDefinition>  partial = getRoot().findTypeDefinitionIn(qName, null, getRoot());
            if (partial.isPresent()) {
                return partial;
            }
        }
        return findTypeDefinitionInHelper(typeName, context.getParent(), context, resolver);
    }

}
