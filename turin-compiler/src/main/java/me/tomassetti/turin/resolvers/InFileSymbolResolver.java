package me.tomassetti.turin.resolvers;

import me.tomassetti.jvm.JvmMethodDefinition;
import me.tomassetti.jvm.JvmNameUtils;
import me.tomassetti.turin.compiler.errorhandling.SemanticErrorException;
import me.tomassetti.turin.definitions.ContextDefinition;
import me.tomassetti.turin.definitions.TypeDefinition;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.context.ContextDefinitionNode;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;
import me.tomassetti.turin.parser.ast.expressions.Expression;
import me.tomassetti.turin.parser.ast.expressions.FunctionCall;
import me.tomassetti.turin.parser.ast.expressions.StaticFieldAccess;
import me.tomassetti.turin.parser.ast.imports.ImportDeclaration;
import me.tomassetti.turin.parser.ast.invokables.FunctionDefinitionNode;
import me.tomassetti.turin.parser.ast.properties.PropertyDefinition;
import me.tomassetti.turin.parser.ast.properties.PropertyReference;
import me.tomassetti.turin.symbols.Symbol;
import me.tomassetti.turin.typesystem.UnsignedPrimitiveTypeUsage;
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

    @Override
    public String toString() {
        return "InFileSymbolResolver{" +
                "typeResolver=" + typeResolver +
                ", parent=" + parent +
                '}';
    }

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
        this.typeResolver.setSymbolResolver(this);
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
        Optional<UnsignedPrimitiveTypeUsage> basicType = UnsignedPrimitiveTypeUsage.findByName(typeName);
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
        List<ActualParam> argsTypes = functionCall.getActualParamValuesInOrder().stream()
                .map((e) -> {
                    ActualParam ap = new ActualParam (e);
                    ap.setParent(functionCall);
                    return ap;
                })
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

    @Override
    public Optional<ContextDefinition> findContextSymbol(String contextName, Node context) {
        return findContextSymbolHelper(contextName, context, null);
    }

    public Optional<ContextDefinition> findContextSymbolHelper(String contextName, Node context, Node previousContext) {
        if (!JvmNameUtils.isValidQualifiedName(contextName)) {
            throw new IllegalArgumentException(contextName);
        }
        if (context == null) {
            return Optional.empty();
        }
        for (Node child : context.getChildren()) {
            if (child instanceof ContextDefinitionNode) {
                ContextDefinitionNode contextDefinition = (ContextDefinitionNode)child;
                if (contextDefinition.getName().equals(contextName)
                        || contextDefinition.getQualifiedName().equals(contextName)) {
                    return Optional.of(contextDefinition);
                }
            } else if (child instanceof ImportDeclaration) {
                // this is necessary to avoid infinite recursion
                if (child != previousContext) {
                    ImportDeclaration importDeclaration = (ImportDeclaration) child;
                    Optional<Symbol> resolvedNode = importDeclaration.findAmongImported(contextName, this.getRoot());
                    if (resolvedNode.isPresent()) {
                        if (resolvedNode.get() instanceof ContextDefinition) {
                            return Optional.of((ContextDefinition) resolvedNode.get());
                        } else {
                            throw new SemanticErrorException(context, "" + contextName + " is not a context");
                        }
                    }
                }
            }
        }
        if (!context.contextName().isEmpty()) {
            String qName = context.contextName() + "." + contextName;
            Optional<ContextDefinition>  partial = getRoot().findContextSymbol(qName, null);
            if (partial.isPresent()) {
                return partial;
            }
        }
        return findContextSymbolHelper(contextName, context.getParent(), context);
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
