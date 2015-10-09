package me.tomassetti.turin.parser.ast.annotations;

import com.google.common.collect.ImmutableList;
import me.tomassetti.jvm.JvmNameUtils;
import me.tomassetti.turin.parser.analysis.exceptions.UnsolvedTypeException;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.TypeDefinition;

import java.util.Optional;

public class AnnotationUsage extends Node {

    private String name;

    public AnnotationUsage(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "AnnotationUsage{" +
                "name='" + name + '\'' +
                '}';
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of();
    }

    public String getDescriptor(SymbolResolver resolver) {
        Optional<TypeDefinition> type = resolver.findTypeDefinitionIn(name, this, resolver);
        if (!type.isPresent()) {
            throw new UnsolvedTypeException(name, this);
        }
        String qName = type.get().getQualifiedName();
        return "L" + JvmNameUtils.canonicalToInternal(qName) + ";";
    }
}
