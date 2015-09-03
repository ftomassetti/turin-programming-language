package me.tomassetti.turin.parser.ast.typeusage;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.analysis.JvmMethodDefinition;
import me.tomassetti.turin.parser.analysis.JvmType;
import me.tomassetti.turin.parser.analysis.Resolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.TypeDefinition;

import java.util.List;
import java.util.stream.Collectors;

/**
 * It could represent also a reference to a Type Variable.
 */
public class ReferenceTypeUsage extends TypeUsage {

    public class TypeParameterValues {
        public List<TypeUsage> getInOrder() {
            throw new UnsupportedOperationException();
        }

        public List<String> getNamesInOrder() {
            throw new UnsupportedOperationException();
        }

        public TypeUsage getByName(String name) {
            throw new UnsupportedOperationException();
        }
    }

    public static final ReferenceTypeUsage STRING = new ReferenceTypeUsage("java.lang.String");

    public TypeParameterValues getTypeParameterValues() {
        throw new UnsupportedOperationException();
    }

    private String name;

    public ReferenceTypeUsage(String name) {
        if (name.contains("/")) {
            throw new IllegalArgumentException(name);
        }
        if (name.startsWith(".")) {
            throw new IllegalArgumentException();
        }
        this.name = name;
    }

    public ReferenceTypeUsage(TypeDefinition td) {
        this(td.getQualifiedName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReferenceTypeUsage that = (ReferenceTypeUsage) o;

        if (!name.equals(that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "ReferenceTypeUsage{" +
                "name='" + name + '\'' +

                '}';
    }

    public TypeDefinition getTypeDefinition(Resolver resolver) {
        TypeDefinition typeDefinition = resolver.findTypeDefinitionIn(this.name, this);
        return typeDefinition;
    }

    @Override
    public JvmMethodDefinition findMethodFor(String methodName, List<JvmType> argsTypes, Resolver resolver, boolean staticContext) {
        TypeDefinition typeDefinition = getTypeDefinition(resolver);
        return typeDefinition.findMethodFor(methodName, argsTypes, resolver, staticContext);
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of();
    }

    @Override
    public JvmType jvmType(Resolver resolver) {
        TypeDefinition typeDefinition = resolver.findTypeDefinitionIn(name, this);
        return typeDefinition.jvmType();
    }

    public String getQualifiedName(Resolver resolver) {
        TypeDefinition typeDefinition = resolver.findTypeDefinitionIn(name, this);
        return typeDefinition.getQualifiedName();
    }

    @Override
    public boolean isReferenceTypeUsage() {
        return true;
    }

    @Override
    public ReferenceTypeUsage asReferenceTypeUsage() {
        return this;
    }

    @Override
    public boolean canBeAssignedTo(TypeUsage type, Resolver resolver) {
        if (!type.isReferenceTypeUsage()) {
            return false;
        }
        ReferenceTypeUsage other = (ReferenceTypeUsage)type;
        if (this.equals(other)) {
            return true;
        }
        for (TypeUsage ancestor : this.getAllAncestors(resolver)) {
            if (ancestor.canBeAssignedTo(type, resolver)) {
                return true;
            }
        }
        return false;
    }

    private List<ReferenceTypeUsage> getAllAncestors(Resolver resolver) {
        return getTypeDefinition(resolver).getAllAncestors(resolver);
    }
}
