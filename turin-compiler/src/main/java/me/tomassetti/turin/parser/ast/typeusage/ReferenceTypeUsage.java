package me.tomassetti.turin.parser.ast.typeusage;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.jvm.JvmMethodDefinition;
import me.tomassetti.turin.jvm.JvmType;
import me.tomassetti.turin.parser.analysis.resolvers.Resolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.TypeDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * It could represent also a reference to a Type Variable.
 */
public class ReferenceTypeUsage extends TypeUsage {

    public static final ReferenceTypeUsage OBJECT = new ReferenceTypeUsage("java.lang.Object");
    public static final ReferenceTypeUsage STRING = new ReferenceTypeUsage("java.lang.String");

    public boolean isInterface(Resolver resolver) {
        return getTypeDefinition(resolver).isInterface();
    }

    public boolean isClass(Resolver resolver) {
        throw new UnsupportedOperationException();
    }

    public boolean isEnum(Resolver resolver) {
        throw new UnsupportedOperationException();
    }

    public boolean isTypeVariable(Resolver resolver) {
        throw new UnsupportedOperationException();
    }

    public class TypeParameterValues {
        private List<TypeUsage> usages = new ArrayList<>();
        private List<String> names = new ArrayList<>();

        public void add(String name, TypeUsage typeUsage) {
            names.add(name);
            usages.add(typeUsage);
        }

        public List<TypeUsage> getInOrder() {
            return usages;
        }

        public List<String> getNamesInOrder() {
            return names;
        }

        public TypeUsage getByName(String name) {
            for (int i=0; i<names.size(); i++) {
                if (names.get(i).equals(name)) {
                    return usages.get(i);
                }
            }
            throw new IllegalArgumentException(name);
        }
    }

    private TypeParameterValues typeParameterValues = new TypeParameterValues();

    public TypeParameterValues getTypeParameterValues() {
        return typeParameterValues;
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
        TypeDefinition typeDefinition = resolver.getTypeDefinitionIn(this.name, this, resolver);
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
        TypeDefinition typeDefinition = resolver.getTypeDefinitionIn(name, this, resolver);
        return typeDefinition.jvmType();
    }

    public String getQualifiedName(Resolver resolver) {
        TypeDefinition typeDefinition = resolver.getTypeDefinitionIn(name, this, resolver);
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
        if (this.getQualifiedName(resolver).equals(other.getQualifiedName(resolver))) {
            return true;
        }
        for (TypeUsage ancestor : this.getAllAncestors(resolver)) {
            if (ancestor.canBeAssignedTo(type, resolver)) {
                return true;
            }
        }
        return false;
    }

    public List<ReferenceTypeUsage> getAllAncestors(Resolver resolver) {
        // TODO perhaps some generic type substitution needs to be done
        return getTypeDefinition(resolver).getAllAncestors(resolver);
    }

    @Override
    public Node getFieldOnInstance(String fieldName, Node instance, Resolver resolver) {
        return getTypeDefinition(resolver).getFieldOnInstance(fieldName, instance, resolver);
    }
}
