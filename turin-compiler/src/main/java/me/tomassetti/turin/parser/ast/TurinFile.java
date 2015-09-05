package me.tomassetti.turin.parser.ast;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.analysis.resolvers.Resolver;
import me.tomassetti.turin.parser.ast.imports.ImportDeclaration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TurinFile extends Node {

    private NamespaceDefinition namespaceDefinition;
    private List<Node> topNodes = new ArrayList<>();
    private List<ImportDeclaration> imports = new ArrayList<>();

    public void add(PropertyDefinition propertyDefinition) {
        topNodes.add(propertyDefinition);
        propertyDefinition.parent = this;
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

    public List<TypeDefinition> getTopTypeDefinitions() {
        return topNodes.stream().filter((n)-> (n instanceof TurinTypeDefinition)).map((n) -> (TypeDefinition)n).collect(Collectors.toList());
    }

    @Override
    public Optional<Node> findSymbol(String name, Resolver resolver) {
        for (ImportDeclaration importDeclaration : imports) {
            Optional<Node> imported = importDeclaration.findAmongImported(name, resolver);
            if (imported.isPresent()) {
                return imported;
            }
        }
        return Optional.empty();
    }
}
