package me.tomassetti.turin.parser.ast;

import com.google.common.collect.ImmutableList;
import jdk.nashorn.internal.ir.annotations.Immutable;
import me.tomassetti.turin.compiler.ParamUtils;
import me.tomassetti.turin.jvm.JvmNameUtils;
import me.tomassetti.turin.parser.analysis.UnsolvedConstructorException;
import me.tomassetti.turin.jvm.JvmConstructorDefinition;
import me.tomassetti.turin.jvm.JvmMethodDefinition;
import me.tomassetti.turin.jvm.JvmType;
import me.tomassetti.turin.parser.analysis.*;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;
import me.tomassetti.turin.parser.ast.typeusage.ReferenceTypeUsage;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

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
    public JvmMethodDefinition findMethodFor(String methodName, List<JvmType> actualParams, SymbolResolver resolver, boolean staticContext) {
        for (Property property : getDirectProperties(resolver)) {
            if (methodName.equals(property.getterName()) && actualParams.size() == 0) {
                String descriptor = "()" + property.getTypeUsage().jvmType(resolver).getDescriptor();
                return new JvmMethodDefinition(getInternalName(), methodName, descriptor, false, false);
            }
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public TypeUsage returnTypeWhenInvokedWith(String methodName, List<ActualParam> actualParams, SymbolResolver resolver, boolean staticContext) {
        for (Property property : getDirectProperties(resolver)) {
            if (methodName.equals(property.getterName()) && actualParams.size() == 0) {
                return property.getTypeUsage();
            }
        }
        throw new UnsupportedOperationException();
    }

    private String getInternalName() {
        return JvmNameUtils.canonicalToInternal(getQualifiedName());
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

    private int numberOfProperties(SymbolResolver resolver){
        // TODO consider inherited properties
        return getDirectProperties(resolver).size();
    }

    /**
     * Properties which can be referred to in the constructor
     */
    public List<Property> assignableProperties(SymbolResolver resolver) {
        return getDirectProperties(resolver).stream().filter((p)->!p.hasInitialValue()).collect(Collectors.toList());
    }

    public List<Property> propertiesAppearingInConstructor(SymbolResolver resolver) {
        return getDirectProperties(resolver).stream().filter((p)->!p.hasInitialValue() && !p.hasDefaultValue()).collect(Collectors.toList());
    }

    public List<Property> nonDefaultPropeties(SymbolResolver resolver) {
        return getDirectProperties(resolver).stream().filter((p)->!p.hasDefaultValue()).collect(Collectors.toList());
    }

    public List<Property> defaultPropeties(SymbolResolver resolver) {
        return getDirectProperties(resolver).stream().filter((p)->p.hasDefaultValue()).collect(Collectors.toList());
    }

    public List<Property> propertiesWhichCanBeAssignedWithoutName(SymbolResolver resolver) {
        return getDirectProperties(resolver).stream().filter((p)->!p.hasInitialValue()).collect(Collectors.toList());
    }

    public boolean hasDefaultProperties(SymbolResolver resolver) {
        return getDirectProperties(resolver).stream().filter((p)->p.hasDefaultValue()).findFirst().isPresent();
    }

    public int numberOfPropertiesUsedByConstructor(SymbolResolver resolver){
        return propertiesAppearingInConstructor(resolver).size() + (hasDefaultProperties(resolver) ? 1 : 0);
    }

    @Override
    public JvmConstructorDefinition resolveConstructorCall(SymbolResolver resolver, List<ActualParam> actualParams) {
        // all named parameters should be after the named ones
        if (!ParamUtils.allNamedParamsAreAtTheEnd(actualParams)) {
            throw new IllegalArgumentException("Named params should all be grouped after the positional ones");
        }

        // TODO verify BEFORE that the call is correct

        /*Set<String> paramsAssigned = new HashSet<>();

        List<Property> propertiesWhichCanBeAssignedWithoutName = propertiesWhichCanBeAssignedWithoutName(resolver);
        List<ActualParam> unnamedParams = ParamUtils.unnamedParams(actualParams);
        List<ActualParam> namedParams = ParamUtils.namedParams(actualParams);

        // use the unnamed params
        if (unnamedParams.size() > propertiesWhichCanBeAssignedWithoutName(resolver).size()) {
            throw new UnsolvedConstructorException(getQualifiedName(), actualParams, "Too many unnamed params: " + actualParams);
        }
        int i = 0;
        for (ActualParam param : unnamedParams) {
            if (!param.getValue().calcType(resolver).canBeAssignedTo(propertiesWhichCanBeAssignedWithoutName.get(i).getTypeUsage(), resolver)){
                throw new UnsolvedConstructorException(getQualifiedName(), actualParams);
            }
            paramsAssigned.add(propertiesWhichCanBeAssignedWithoutName.get(i).getName());
            i++;
        }
        // use the named params
        Map<String, Property> validNames = new HashMap<>();
        assignableProperties(resolver).forEach((p) ->validNames.put(p.getName(), p));
        for (ActualParam param : namedParams) {
            if (paramsAssigned.contains(param.getName())) {
                throw new IllegalArgumentException("Property " + param.getName() + " assigned several times");
            }
            if (!validNames.containsKey(param.getName())) {
                throw new UnsolvedConstructorException(getQualifiedName(), actualParams, "Unknown property " + param.getName());
            }
            if (!param.getValue().calcType(resolver).canBeAssignedTo(validNames.get(param.getName()).getTypeUsage(), resolver)){
                throw new UnsolvedConstructorException(getQualifiedName(), actualParams);
            }
            paramsAssigned.add(param.getName());
        }

        // verify that all properties with no default or initial value have been assigned
        for (Property property : propertiesAppearingInConstructor(resolver)) {
            if (!paramsAssigned.contains(property.getName())) {
                throw new IllegalArgumentException("Property not assigned: " + property.getName());
            }
        }*/

        // For type defined in Turin we generate one single constructor so
        // it is easy to find it
        List<String> paramSignatures = propertiesAppearingInConstructor(resolver).stream()
                .map((p) -> p.getTypeUsage().jvmType(resolver).getSignature())
                .collect(Collectors.toList());
        if (hasDefaultProperties(resolver)) {
            paramSignatures.add("Ljava/util/Map;");
        }
        return new JvmConstructorDefinition(jvmType().getInternalName(), "(" + String.join("", paramSignatures) + ")V");
    }

    @Override
    public TypeUsage getField(String fieldName, boolean staticContext) {
        // TODO to be implemented
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ReferenceTypeUsage> getAllAncestors(SymbolResolver resolver) {
        // TODO consider superclasses
        return ImmutableList.of(ReferenceTypeUsage.OBJECT);
    }

    @Override
    public boolean isInterface() {
        // TODO when it will be possible to declare interface fix this
        return false;
    }

    @Override
    public boolean hasManyConstructors() {
        return false;
    }

    @Override
    public boolean isMethodOverloaded(String methodName) {
        return false;
    }

    @Override
    public List<FormalParameter> getConstructorParams(List<ActualParam> actualParams, SymbolResolver resolver) {
        return this.assignableProperties(resolver).stream()
                .map((p)->new FormalParameter(p.getTypeUsage(), p.getName(), p.getDefaultValue()))
                .collect(Collectors.toList());
    }

    @Override
    public List<FormalParameter> getMethodParams(String methodName, List<ActualParam> actualParams, SymbolResolver resolver, boolean staticContext) {
        for (Property property : getDirectProperties(resolver)) {
            if (methodName.equals(property.getterName()) && actualParams.size() == 0) {
                return Collections.emptyList();
            }
            if (methodName.equals(property.setterName()) && actualParams.size() == 1 ) {
                TypeUsage actualType = actualParams.get(0).getValue().calcType(resolver);
                if (actualType.canBeAssignedTo(property.getTypeUsage(), resolver)){
                    return ImmutableList.of(new FormalParameter(property.getTypeUsage(), property.getName()));
                }
            }
        }

        throw new UnsupportedOperationException(methodName);
    }

    @Override
    public boolean hasMethodFor(String methodName, List<ActualParam> actualParams, SymbolResolver resolver, boolean staticContext) {
        // TODO consider methods inherited from object and setters
        for (Property property : getDirectProperties(resolver)) {
            if (methodName.equals(property.getterName()) && actualParams.size() == 0) {
                return true;
            }
        }
        return false;
    }

    private List<TypeUsage> orderConstructorParamTypes(List<ActualParam> actualParams, SymbolResolver resolver) {
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

    private int findPosOfProperty(String name, SymbolResolver resolver) {
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
    public Optional<Node> findSymbol(String name, SymbolResolver resolver) {
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
    public boolean defineMethodToString(SymbolResolver resolver) {
        return isDefiningMethod("toString", Collections.emptyList(), resolver);
    }

    /**
     * Does it override the hashCode method defined in Object?
     */
    public boolean defineMethodHashCode(SymbolResolver resolver) {
        return isDefiningMethod("hashCode", Collections.emptyList(), resolver);
    }

    /**
     * Does it override the equals method defined in Object?
     */
    public boolean defineMethodEquals(SymbolResolver resolver) {
        return isDefiningMethod("equals", ImmutableList.of(ReferenceTypeUsage.OBJECT), resolver);
    }

    private boolean isDefiningMethod(String name, List<TypeUsage> paramTypes, SymbolResolver resolver) {
        return getDirectMethods().stream().filter((m)->m.getName().equals(name))
                .filter((m) -> m.getParameters().stream().map((p) -> p.calcType(resolver).jvmType(resolver)).collect(Collectors.toList())
                        .equals(paramTypes.stream().map((p) -> p.jvmType(resolver)).collect(Collectors.toList())))
                .count() > 0;
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.copyOf(members);
    }

    public List<Property> getDirectProperties(SymbolResolver resolver) {
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
    public List<Property> getAllProperties(SymbolResolver resolver) {
        // TODO consider also inherited properties
        return getDirectProperties(resolver);
    }

    public void add(MethodDefinition methodDefinition) {
        members.add(methodDefinition);
        methodDefinition.parent = this;
    }
}
