package me.tomassetti.turin.parser.ast;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.compiler.errorhandling.ErrorCollector;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.imports.ImportDeclaration;
import me.tomassetti.turin.parser.ast.invokables.FunctionDefinition;
import me.tomassetti.turin.parser.ast.properties.PropertyDefinition;
import me.tomassetti.turin.parser.ast.relations.RelationDefinition;

import java.util.*;
import java.util.stream.Collectors;

public class TurinFile extends Node {

    private NamespaceDefinition namespaceDefinition;
    private List<Node> topNodes = new ArrayList<>();
    private List<ImportDeclaration> imports = new ArrayList<>();

    public void add(PropertyDefinition propertyDefinition) {
        topNodes.add(propertyDefinition);
        propertyDefinition.parent = this;
    }

    @Override
    protected boolean specificValidate(SymbolResolver resolver, ErrorCollector errorCollector) {
        boolean valid = true;
        Map<String, List<Position>> positions = new HashMap<>();
        for (Node topNode : topNodes) {
            if (topNode instanceof Named) {
                Named named = (Named)topNode;
                if (positions.containsKey(named.getName())) {
                    positions.get(named.getName()).add(topNode.getPosition());
                    List<String> positionsAsStrings = positions.get(named.getName()).stream().map((p)->p.toString()).collect(Collectors.toList());
                    errorCollector.recordSemanticError(topNode.getPosition(), "Duplicate name \"" + named.getName() + "\" appearing at " + String.join(", ", positionsAsStrings));
                    valid = false;
                } else {
                    List<Position> ps = new ArrayList<>();
                    ps.add(topNode.getPosition());
                    positions.put(named.getName(), ps);
                }
            }
        }
        return valid && super.specificValidate(resolver, errorCollector);
    }

    public void add(ImportDeclaration importDeclaration) {
        imports.add(importDeclaration);
        importDeclaration.parent = this;
    }

    public NamespaceDefinition getNamespaceDefinition() {
        return namespaceDefinition;
    }

    public void add(TurinTypeDefinition typeDefinition) {
        topNodes.add(typeDefinition);
        typeDefinition.parent = this;
    }

    @Override
    public String toString() {
        return "TurinFile{" +
                "namespaceDefinition=" + namespaceDefinition +
                ", topNodes=" + topNodes +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TurinFile turinFile = (TurinFile) o;

        if (!namespaceDefinition.equals(turinFile.namespaceDefinition)) return false;
        if (!topNodes.equals(turinFile.topNodes)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = namespaceDefinition.hashCode();
        result = 31 * result + topNodes.hashCode();
        return result;
    }

    public ImmutableList<Node> getNodes() {
        return ImmutableList.copyOf(topNodes);
    }

    public void setNameSpace(NamespaceDefinition namespaceDefinition) {
        if (this.namespaceDefinition != null) {
            this.namespaceDefinition.parent = null;
        }
        this.namespaceDefinition = namespaceDefinition;
        this.namespaceDefinition.parent = this;
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.<Node>builder().add(namespaceDefinition).addAll(topNodes).addAll(imports).build();
    }

    public Optional<TurinTypeDefinition> getTopTypeDefinition(String name) {
        Optional<Node> res = topNodes.stream().filter((n)-> (n instanceof TurinTypeDefinition) && ((TurinTypeDefinition)n).getName().equals(name)).findFirst();
        if (res.isPresent()) {
            return Optional.of((TurinTypeDefinition)res.get());
        } else {
            return Optional.empty();
        }
    }

    public void add(Program program) {
        topNodes.add(program);
        program.parent = this;
    }

    public List<TurinTypeDefinition> getTopLevelTypeDefinitions() {
        return topNodes.stream().filter((n)-> (n instanceof TurinTypeDefinition)).map((n) -> (TurinTypeDefinition)n).collect(Collectors.toList());
    }

    public List<FunctionDefinition> getTopLevelFunctionDefinitions() {
        return topNodes.stream().filter((n)-> (n instanceof FunctionDefinition)).map((n) -> (FunctionDefinition)n).collect(Collectors.toList());
    }

    public List<PropertyDefinition> getTopLevelPropertyDefinitions() {
        return topNodes.stream().filter((n)-> (n instanceof PropertyDefinition)).map((n) -> (PropertyDefinition)n).collect(Collectors.toList());
    }

    public List<Program> getTopLevelPrograms() {
        return topNodes.stream().filter((n)-> (n instanceof Program)).map((n) -> (Program)n).collect(Collectors.toList());
    }

    @Override
    public Optional<Node> findSymbol(String name, SymbolResolver resolver) {
        for (ImportDeclaration importDeclaration : imports) {
            Optional<Node> imported = importDeclaration.findAmongImported(name, resolver);
            if (imported.isPresent()) {
                return imported;
            }
        }
        for (FunctionDefinition functionDefinition : getTopLevelFunctionDefinitions()) {
            if (functionDefinition.getName().equals(name)) {
                return Optional.of(functionDefinition);
            }
        }
        for (PropertyDefinition propertyDefinition : getTopLevelPropertyDefinitions()) {
            if (propertyDefinition.getName().equals(name)) {
                return Optional.of(propertyDefinition);
            }
        }
        for (Program program : getTopLevelPrograms()) {
            if (program.getName().equals(name)) {
                return Optional.of(program);
            }
        }
        for (NodeTypeDefinition typeDefinition : getTopLevelTypeDefinitions()) {
            if (typeDefinition.getName().equals(name)) {
                return Optional.of(typeDefinition);
            }
        }
        String qName = namespaceDefinition.getName() + "." + name;
        return resolver.getRoot().findSymbol(qName, null);
    }

    public void add(FunctionDefinition functionDefinition) {
        topNodes.add(functionDefinition);
        functionDefinition.parent = this;
    }

    public void add(RelationDefinition relationDefinition) {
        topNodes.add(relationDefinition);
        relationDefinition.parent = this;
    }
}
