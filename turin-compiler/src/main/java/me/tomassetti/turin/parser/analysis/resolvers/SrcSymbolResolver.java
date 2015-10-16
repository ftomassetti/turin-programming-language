package me.tomassetti.turin.parser.analysis.resolvers;

import me.tomassetti.jvm.JvmMethodDefinition;
import me.tomassetti.turin.parser.analysis.exceptions.UnsolvedMethodException;
import me.tomassetti.turin.parser.ast.*;
import me.tomassetti.turin.parser.ast.expressions.FunctionCall;
import me.tomassetti.turin.parser.ast.invokables.FunctionDefinitionNode;
import me.tomassetti.turin.parser.ast.properties.PropertyDefinition;
import me.tomassetti.turin.parser.ast.properties.PropertyReference;
import me.tomassetti.turin.parser.ast.typeusage.ReferenceTypeUsageNode;
import me.tomassetti.turin.symbols.Symbol;
import me.tomassetti.turin.typesystem.TypeUsage;

import java.util.*;

/**
 * Solve symbols considering TurinFiles.
 */
public class SrcSymbolResolver implements SymbolResolver {

    private Set<String> packages = new HashSet<>();
    private Map<String, TypeDefinition> typeDefinitions;
    private Map<String, PropertyDefinition> propertyDefinitions;
    private Map<String, Program> programsDefinitions;
    private Map<String, FunctionDefinitionNode> functionDefinitions;

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
                packages.add(typeDefinition.contextName());
                typeDefinitions.put(typeDefinition.getQualifiedName(), typeDefinition);
            }
            for (PropertyDefinition propertyDefinition : turinFile.getTopLevelPropertyDefinitions()) {
                packages.add(propertyDefinition.contextName());
                propertyDefinitions.put(propertyDefinition.getQualifiedName(), propertyDefinition);
            }
            for (Program program : turinFile.getTopLevelPrograms()) {
                packages.add(program.contextName());
                programsDefinitions.put(program.getQualifiedName(), program);
            }
            for (FunctionDefinitionNode functionDefinition : turinFile.getTopLevelFunctionDefinitions()) {
                packages.add(functionDefinition.contextName());
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
            return Optional.of(new ReferenceTypeUsageNode(typeDefinitions.get(typeName)));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<JvmMethodDefinition> findJvmDefinition(FunctionCall functionCall) {
        throw new UnsolvedMethodException(functionCall);
    }

    @Override
    public Optional<Symbol> findSymbol(String name, Node context) {
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

    @Override
    public boolean existPackage(String packageName) {
        return packages.contains(packageName);
    }

}
