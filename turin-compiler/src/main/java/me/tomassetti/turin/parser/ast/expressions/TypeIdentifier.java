package me.tomassetti.turin.parser.ast.expressions;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.analysis.UnsolvedTypeException;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.QualifiedName;
import me.tomassetti.turin.parser.ast.TypeDefinition;

import java.util.Collections;
import java.util.Optional;

public class TypeIdentifier extends Node {
    private QualifiedName packageName;
    private String typeName;

    public TypeIdentifier(QualifiedName packageName, String typeName) {
        this.packageName = packageName;
        this.packageName.setParent(this);
        this.typeName = typeName;
    }

    @Override
    public String toString() {
        return "TypeIdentifier{" +
                "packageName=" + packageName +
                ", typeName='" + typeName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TypeIdentifier that = (TypeIdentifier) o;

        if (packageName != null ? !packageName.equals(that.packageName) : that.packageName != null) return false;
        if (!typeName.equals(that.typeName)) return false;

        return true;
    }

    public TypeDefinition resolve(SymbolResolver resolver) {
        Optional<TypeDefinition> res = resolver.findTypeDefinitionIn(qualifiedName(), this, resolver);
        if (res.isPresent()) {
            return res.get();
        } else {
            throw new UnsolvedTypeException(qualifiedName(), this);
        }
    }

    @Override
    public int hashCode() {
        int result = packageName != null ? packageName.hashCode() : 0;
        result = 31 * result + typeName.hashCode();
        return result;
    }

    public TypeIdentifier(String typeName) {
        this.packageName = null;
        this.typeName = typeName;
    }

    @Override
    public Iterable<Node> getChildren() {
        if (packageName == null) {
            return Collections.emptyList();
        } else {
            return ImmutableList.of(packageName);
        }
    }

    public String qualifiedName() {
        if (packageName == null) {
            return typeName;
        } else {
            return packageName.qualifiedName() + "." + typeName;
        }
    }
}
