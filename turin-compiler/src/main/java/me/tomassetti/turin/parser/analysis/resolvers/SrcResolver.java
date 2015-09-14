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
public class SrcResolver implements Resolver {

    private List<TurinFile> turinFiles;
    private Map<String, TypeDefinition> definitions;

    public SrcResolver(List<TurinFile> turinFiles) {
        this.turinFiles = turinFiles;
        this.definitions = new HashMap<>();
        for (TurinFile turinFile : turinFiles) {
            for (TypeDefinition typeDefinition : turinFile.getTopLevelTypeDefinitions()) {
                definitions.put(typeDefinition.getQualifiedName(), typeDefinition);
            }
        }
    }

    @Override
    public PropertyDefinition findDefinition(PropertyReference propertyReference) {
        throw new UnsolvedSymbolException(propertyReference);
    }

    @Override
    public Optional<TypeDefinition> findTypeDefinitionIn(String typeName, Node context, Resolver resolver) {
        if (definitions.containsKey(typeName)) {
            return Optional.of(definitions.get(typeName));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public TypeUsage findTypeUsageIn(String typeName, Node context, Resolver resolver) {
        if (definitions.containsKey(typeName)) {
            return new ReferenceTypeUsage(definitions.get(typeName));
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
        if (definitions.containsKey(name)) {
            return Optional.of(definitions.get(name));
        } else {
            return Optional.empty();
        }
    }
}
