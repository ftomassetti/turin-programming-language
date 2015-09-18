package me.tomassetti.turin.parser.analysis.resolvers;

import me.tomassetti.turin.jvm.JvmMethodDefinition;
import me.tomassetti.turin.parser.analysis.UnsolvedMethodException;
import me.tomassetti.turin.parser.analysis.UnsolvedSymbolException;
import me.tomassetti.turin.parser.analysis.UnsolvedTypeException;
import me.tomassetti.turin.parser.ast.*;
import me.tomassetti.turin.parser.ast.expressions.FunctionCall;
import me.tomassetti.turin.parser.ast.typeusage.ReferenceTypeUsage;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Solve considering TurinFiles.
 */
public class SrcSymbolResolver implements SymbolResolver {

    private List<TurinFile> turinFiles;
    private Map<String, TypeDefinition> typeDefinitions;
    private Map<String, PropertyDefinition> propertyDefinitions;

    public SrcSymbolResolver(List<TurinFile> turinFiles) {
        this.turinFiles = turinFiles;
        this.typeDefinitions = new HashMap<>();
        this.propertyDefinitions = new HashMap<>();
        for (TurinFile turinFile : turinFiles) {
            for (TypeDefinition typeDefinition : turinFile.getTopLevelTypeDefinitions()) {
                typeDefinitions.put(typeDefinition.getQualifiedName(), typeDefinition);
            }
            for (PropertyDefinition propertyDefinition : turinFile.getTopLevelPropertyDefinitions()) {
                propertyDefinitions.put(propertyDefinition.getQualifiedName(), propertyDefinition);
            }
        }
    }

    @Override
    public PropertyDefinition findDefinition(PropertyReference propertyReference) {
        String name = propertyReference.contextName() + "." + propertyReference.getName();
        if (propertyDefinitions.containsKey(name)) {
            return propertyDefinitions.get(name);
        } else {
            throw new UnsolvedSymbolException(propertyReference);
        }

    }

    @Override
    public Optional<TypeDefinition> findTypeDefinitionIn(String typeName, Node context, SymbolResolver resolver) {
        if (typeDefinitions.containsKey(typeName)) {
            return Optional.of(typeDefinitions.get(typeName));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public TypeUsage findTypeUsageIn(String typeName, Node context, SymbolResolver resolver) {
        if (typeDefinitions.containsKey(typeName)) {
            return new ReferenceTypeUsage(typeDefinitions.get(typeName));
        } else {
            throw new UnsolvedTypeException(typeName, context);
        }
    }

    @Override
    public JvmMethodDefinition findJvmDefinition(FunctionCall functionCall) {
        throw new UnsolvedMethodException(functionCall);
    }

    @Override
    public Optional<Node> findSymbol(String name, Node context) {
        // TODO consider also static fields and methods
        if (typeDefinitions.containsKey(name)) {
            return Optional.of(typeDefinitions.get(name));
        } else {
            return Optional.empty();
        }
    }
}
