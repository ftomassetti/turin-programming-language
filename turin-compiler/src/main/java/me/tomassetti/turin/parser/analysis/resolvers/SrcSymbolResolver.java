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
 * Solve symbols considering TurinFiles.
 */
public class SrcSymbolResolver implements SymbolResolver {

    private Map<String, TypeDefinition> typeDefinitions;
    private Map<String, PropertyDefinition> propertyDefinitions;
    private Map<String, Program> programsDefinitions;
    private Map<String, FunctionDefinition> functionDefinitions;

    private SymbolResolver parent = null;

    @Override
    public SymbolResolver getParent() {
        return parent;
    }

    @Override
    public void setParent(SymbolResolver parent) {
        this.parent = parent;
    }

    public SrcSymbolResolver(List<TurinFile> turinFiles) {
        this.typeDefinitions = new HashMap<>();
        this.propertyDefinitions = new HashMap<>();
        this.programsDefinitions = new HashMap<>();
        this.functionDefinitions = new HashMap<>();
        for (TurinFile turinFile : turinFiles) {
            for (TypeDefinition typeDefinition : turinFile.getTopLevelTypeDefinitions()) {
                typeDefinitions.put(typeDefinition.getQualifiedName(), typeDefinition);
            }
            for (PropertyDefinition propertyDefinition : turinFile.getTopLevelPropertyDefinitions()) {
                propertyDefinitions.put(propertyDefinition.getQualifiedName(), propertyDefinition);
            }
            for (Program program : turinFile.getTopLevelPrograms()) {
                programsDefinitions.put(program.getQualifiedName(), program);
            }
            for (FunctionDefinition functionDefinition : turinFile.getTopLevelFunctionDefinitions()) {
                functionDefinitions.put(functionDefinition.getQualifiedName(), functionDefinition);
            }
        }
    }

    @Override
    public Optional<PropertyDefinition> findDefinition(PropertyReference propertyReference) {
        String name = propertyReference.contextName() + "." + propertyReference.getName();
        if (propertyDefinitions.containsKey(name)) {
            return Optional.of(propertyDefinitions.get(name));
        } else {
            return Optional.empty();
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
    public Optional<TypeUsage> findTypeUsageIn(String typeName, Node context, SymbolResolver resolver) {
        if (typeDefinitions.containsKey(typeName)) {
            return Optional.of(new ReferenceTypeUsage(typeDefinitions.get(typeName)));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<JvmMethodDefinition> findJvmDefinition(FunctionCall functionCall) {
        throw new UnsolvedMethodException(functionCall);
    }

    @Override
    public Optional<Node> findSymbol(String name, Node context) {
        // TODO consider also static fields and methods
        if (typeDefinitions.containsKey(name)) {
            return Optional.of(typeDefinitions.get(name));
        } else if (propertyDefinitions.containsKey(name)) {
            return Optional.of(propertyDefinitions.get(name));
        } else if (functionDefinitions.containsKey(name)) {
            return Optional.of(functionDefinitions.get(name));
        } else if (programsDefinitions.containsKey(name)) {
            return Optional.of(programsDefinitions.get(name));
        } else {
            return Optional.empty();
        }
    }
}
