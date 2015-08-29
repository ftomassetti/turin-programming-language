package me.tomassetti.turin.ast;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TurinFile extends Node {

    private NamespaceDefinition namespaceDefinition;
    private List<Node> topNodes = new ArrayList<>();

    public void add(PropertyDefinition propertyDefinition) {
        topNodes.add(propertyDefinition);
        propertyDefinition.parent = this;
    }

    public NamespaceDefinition getNamespaceDefinition() {
        return namespaceDefinition;
    }

    public void add(TypeDefinition typeDefinition) {
        topNodes.add(typeDefinition);
        typeDefinition.parent = this;
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
        return ImmutableList.<Node>builder().add(namespaceDefinition).addAll(topNodes).build();
    }

    public Optional<TypeDefinition> getTopTypeDefinition(String name) {
        Optional<Node> res = topNodes.stream().filter((n)-> (n instanceof TypeDefinition) && ((TypeDefinition)n).getName().equals(name)).findFirst();
        if (res.isPresent()) {
            return Optional.of((TypeDefinition)res.get());
        } else {
            return Optional.empty();
        }
    }
}
