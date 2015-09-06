package me.tomassetti.turin.parser.ast;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.analysis.UnsolvedConstructorException;
import me.tomassetti.turin.jvm.JvmConstructorDefinition;
import me.tomassetti.turin.jvm.JvmMethodDefinition;
import me.tomassetti.turin.jvm.JvmType;
import me.tomassetti.turin.parser.analysis.*;
import me.tomassetti.turin.parser.analysis.resolvers.Resolver;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;
import me.tomassetti.turin.parser.ast.typeusage.ReferenceTypeUsage;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Type defined in Turin.
 */
public class TurinTypeDefinition extends TypeDefinition {
    private List<Node> members = new ArrayList<>();

    public String getQualifiedName() {
        String contextName = contextName();
        if (contextName.isEmpty()) {
            return name;
        } else {
            return contextName + "." + name;
        }
    }

    @Override
    public JvmMethodDefinition findMethodFor(String name, List<JvmType> argsTypes, Resolver resolver, boolean staticContext) {
        // TODO this should be implemented
        throw new UnsupportedOperationException();
    }

    public void add(PropertyDefinition propertyDefinition){
        members.add(propertyDefinition);
        propertyDefinition.parent = this;
    }

    public TurinTypeDefinition(String name) {
        super(name);
    }

    public ImmutableList<Node> getMembers() {
        return ImmutableList.copyOf(members);
    }

    private int numberOfProperties(Resolver resolver){
        // TODO consider inherited properties
        return getDirectProperties(resolver).size();
    }

    @Override
    public JvmConstructorDefinition resolveConstructorCall(Resolver resolver, List<ActualParam> actualParams) {
        if (actualParams.size() != numberOfProperties(resolver)) {
            throw new UnsolvedConstructorException(getQualifiedName(), actualParams);
        }

        // no unnamed parameters after the named ones
        boolean namedFound = false;
        for (ActualParam actualParam : actualParams) {
            if (namedFound && !actualParam.isNamed()){
                throw new IllegalArgumentException();
            }
            if (!namedFound && actualParam.isNamed()){
                namedFound = true;
            }
        }

        List<TypeUsage> paramTypesInOrder = orderConstructorParamTypes(actualParams, resolver);

        List<Property> properties = getDirectProperties(resolver);
        for (int i=0;i<properties.size();i++){
            if (!paramTypesInOrder.get(i).canBeAssignedTo(properties.get(i).getTypeUsage(), resolver)){
                throw new UnsolvedConstructorException(getQualifiedName(), actualParams);
            }
        }

        // For type defined in Turin we generate one single constructor so
        // it is easy to find it
        List<String> paramSignatures = paramTypesInOrder.stream()
                .map((p) -> p.jvmType(resolver).getSignature())
                .collect(Collectors.toList());
        return new JvmConstructorDefinition(jvmType().getInternalName(), "(" + String.join("", paramSignatures) + ")V");
    }

    @Override
    public TypeUsage getField(String fieldName, boolean staticContext) {
        // TODO to be implemented
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ReferenceTypeUsage> getAllAncestors(Resolver resolver) {
        // TODO to be implemented
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isInterface() {
        // TODO when it will be possible to declare interface fix this
        return false;
    }

    private List<TypeUsage> orderConstructorParamTypes(List<ActualParam> actualParams, Resolver resolver) {
        TypeUsage[] types = new TypeUsage[actualParams.size()];
        int i = 0;
        for (ActualParam actualParam : actualParams) {
            if (actualParam.isNamed()) {
                int pos = findPosOfProperty(actualParam.getName(), resolver);
                if (types[pos] != null) {
                    throw new IllegalArgumentException();
                }
                types[pos] = actualParam.getValue().calcType(resolver);
            } else {
                types[i] = actualParam.getValue().calcType(resolver);
            }
            i++;
        }
        for (TypeUsage tu : types) {
            if (tu == null) {
                throw new IllegalArgumentException();
            }
        }
        return Arrays.asList(types);
    }

    private int findPosOfProperty(String name, Resolver resolver) {
        List<Property> properties = getDirectProperties(resolver);
        for (int i=0; i<properties.size(); i++){
            if (properties.get(i).getName().equals(name)) {
                return i;
            }
        }
        throw new IllegalArgumentException(name);
    }

    public void add(PropertyReference propertyReference) {
        members.add(propertyReference);
        propertyReference.parent = this;
    }

    @Override
    public String toString() {
        return "TypeDefinition{" +
                "name='" + name + '\'' +
                ", members=" + members +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TurinTypeDefinition that = (TurinTypeDefinition) o;

        if (!members.equals(that.members)) return false;
        if (!name.equals(that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + members.hashCode();
        return result;
    }

    @Override
    public Optional<Node> findSymbol(String name, Resolver resolver) {
        // TODO support references to methods
        for (Property property : this.getAllProperties(resolver)) {
            if (property.getName().equals(name)) {
                return Optional.of(property);
            }
        }

        return super.findSymbol(name, resolver);
    }

    /**
     * Does it override the toString method defined in Object?
     */
    public boolean defineMethodToString(Resolver resolver) {
        return isDefiningMethod("toString", Collections.emptyList(), resolver);
    }

    /**
     * Does it override the hashCode method defined in Object?
     */
    public boolean defineMethodHashCode(Resolver resolver) {
        return isDefiningMethod("hashCode", Collections.emptyList(), resolver);
    }

    /**
     * Does it override the equals method defined in Object?
     */
    public boolean defineMethodEquals(Resolver resolver) {
        return isDefiningMethod("equals", ImmutableList.of(ReferenceTypeUsage.OBJECT), resolver);
    }

    private boolean isDefiningMethod(String name, List<TypeUsage> paramTypes, Resolver resolver) {
        return getDirectMethods().stream().filter((m)->m.getName().equals(name))
                .filter((m) -> m.getParameters().stream().map((p) -> p.calcType(resolver).jvmType(resolver)).collect(Collectors.toList())
                        .equals(paramTypes.stream().map((p) -> p.jvmType(resolver)).collect(Collectors.toList())))
                .count() > 0;
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.copyOf(members);
    }

    public List<Property> getDirectProperties(Resolver resolver) {
        List<Property> properties = new ArrayList<>();
        for (Node member : members) {
            if (member instanceof PropertyDefinition) {
                properties.add(Property.fromDefinition((PropertyDefinition)member));
            } else if (member instanceof PropertyReference) {
                properties.add(Property.fromReference((PropertyReference) member, resolver));
            }
        }
        return properties;
    }

    public List<MethodDefinition> getDirectMethods() {
        List<MethodDefinition> methods = new ArrayList<>();
        for (Node member : members) {
            if (member instanceof MethodDefinition) {
                methods.add((MethodDefinition)member);
            }
        }
        return methods;
    }

    /**
     * Get direct and inherited properties.
     */
    public List<Property> getAllProperties(Resolver resolver) {
        // TODO consider also inherited properties
        return getDirectProperties(resolver);
    }

    public void add(MethodDefinition methodDefinition) {
        members.add(methodDefinition);
        methodDefinition.parent = this;
    }
}
