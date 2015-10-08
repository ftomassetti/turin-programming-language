package me.tomassetti.turin.parser.ast;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.compiler.ParamUtils;
import me.tomassetti.turin.compiler.errorhandling.ErrorCollector;
import me.tomassetti.jvm.JvmNameUtils;
import me.tomassetti.turin.parser.analysis.UnsolvedConstructorException;
import me.tomassetti.jvm.JvmConstructorDefinition;
import me.tomassetti.jvm.JvmMethodDefinition;
import me.tomassetti.jvm.JvmType;
import me.tomassetti.turin.parser.analysis.*;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.analysis.resolvers.jdk.ReflectionTypeDefinitionFactory;
import me.tomassetti.turin.parser.ast.annotations.AnnotationUsage;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;
import me.tomassetti.turin.parser.ast.typeusage.ReferenceTypeUsage;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;
import me.tomassetti.turin.parser.ast.typeusage.VoidTypeUsage;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Type defined in Turin.
 */
public class TurinTypeDefinition extends TypeDefinition {
    private List<Node> members = new ArrayList<>();
    private List<TypeUsage> interfaces = new ArrayList<>();
    private Optional<TypeUsage> baseType = Optional.empty();

    private List<AnnotationUsage> annotations = new ArrayList<>();

    public List<TurinTypeContructorDefinition> getExplicitConstructors() {
        return members.stream()
                .filter((m) -> m instanceof TurinTypeContructorDefinition)
                .map((m) -> (TurinTypeContructorDefinition) m)
                .collect(Collectors.toList());
    }

    @Override
    protected boolean specificValidate(SymbolResolver resolver, ErrorCollector errorCollector) {
        if (baseType.isPresent()) {
            if (!baseType.get().isReferenceTypeUsage() || !baseType.get().asReferenceTypeUsage().isClass(resolver)) {
                errorCollector.recordSemanticError(baseType.get().getPosition(), "Only classes can be extended");
                return false;
            }
        }

        for (TypeUsage typeUsage : interfaces) {
            if (!typeUsage.isReferenceTypeUsage() || !typeUsage.asReferenceTypeUsage().isInterface(resolver)) {
                errorCollector.recordSemanticError(typeUsage.getPosition(), "Only interfaces can be implemented");
                return false;
            }
        }

        if (getExplicitConstructors().size() > 1) {
            for (TurinTypeContructorDefinition contructorDefinition : getExplicitConstructors()) {
                errorCollector.recordSemanticError(contructorDefinition.getPosition(), "At most one explicit constructor can be defined");
            }
            return false;
        }

        return super.specificValidate(resolver, errorCollector);
    }

    public void setBaseType(TypeUsage baseType) {
        baseType.setParent(this);
        this.baseType = Optional.of(baseType);
    }

    public void addInterface(TypeUsage interfaze) {
        interfaze.setParent(this);
        interfaces.add(interfaze);
    }

    public void addAnnotation(AnnotationUsage annotation) {
        annotation.setParent(this);
        annotations.add(annotation);
    }

    public String getQualifiedName() {
        String contextName = contextName();
        if (contextName.isEmpty()) {
            return name;
        } else {
            return contextName + "." + name;
        }
    }

    private Map<String, List<InternalMethodDefinition>> methodsByName;
    private List<InternalConstructorDefinition> constructors;

    private void registerMethod(InternalMethodDefinition method) {
        if (!methodsByName.containsKey(method.getMethodName())){
            methodsByName.put(method.getMethodName(), new ArrayList<>());
        }
        methodsByName.get(method.getMethodName()).add(method);
    }

    private void initializeMethodsByName(SymbolResolver resolver) {
        methodsByName = new HashMap<>();
        // TODO methods inherited by Object
        // TODO if we implement inheritance also other methods inherited from classes or interfaces
        for (Property property : getDirectProperties(resolver)) {
            {
                String descriptor = "()" + property.getTypeUsage().jvmType(resolver).getDescriptor();
                JvmMethodDefinition jvmMethodDefinition = new JvmMethodDefinition(getInternalName(), property.getterName(), descriptor, false, false);
                InternalMethodDefinition getter = new InternalMethodDefinition(property.getterName(), Collections.emptyList(), property.getTypeUsage(), jvmMethodDefinition);
                registerMethod(getter);
            }
            {
                String descriptor = "(" + property.getTypeUsage().jvmType(resolver).getDescriptor() + ")V";
                JvmMethodDefinition jvmMethodDefinition = new JvmMethodDefinition(getInternalName(), property.setterName(), descriptor, false, false);
                FormalParameter param = new FormalParameter(property.getTypeUsage(), property.getName());
                InternalMethodDefinition setter = new InternalMethodDefinition(property.setterName(), ImmutableList.of(param), new VoidTypeUsage(), jvmMethodDefinition);
                registerMethod(setter);
            }
        }
    }

    public List<InternalConstructorDefinition> getConstructors() {
        return constructors;
    }

    public InternalConstructorDefinition getOnlyConstructor(SymbolResolver resolver) {
        if (constructors == null) {
            initializeConstructors(resolver);
        }
        if (constructors.size() != 1) {
            throw new IllegalStateException();
        }
        return constructors.get(0);
    }

    private void initializeImplicitConstructor(SymbolResolver resolver) {
        List<FormalParameter> inheritedParams = Collections.emptyList();
        if (getBaseType().isPresent()) {
            List<InternalConstructorDefinition> constructors = getBaseType().get().asReferenceTypeUsage().getTypeDefinition(resolver).getConstructors();
            if (constructors.size() != 1) {
                throw new UnsupportedOperationException();
            }
            inheritedParams = constructors.get(0).getFormalParameters();
        }

        List<FormalParameter> newParams = this.assignableProperties(resolver).stream()
                .map((p)->new FormalParameter(p.getTypeUsage(), p.getName(), p.getDefaultValue()))
                .collect(Collectors.toList());
        List<FormalParameter> allParams = new LinkedList<>();
        allParams.addAll(inheritedParams);
        allParams.addAll(newParams);
        allParams.sort(new Comparator<FormalParameter>() {
            @Override
            public int compare(FormalParameter o1, FormalParameter o2) {
                return Boolean.compare(o1.hasDefaultValue(), o2.hasDefaultValue());
            }
        });
        addConstructorWithParams(allParams, resolver);
    }

    private void initializeConstructors(SymbolResolver resolver) {
        constructors = new ArrayList<>();
        if (getExplicitConstructors().isEmpty()) {
            initializeImplicitConstructor(resolver);
        } else {
            if (getExplicitConstructors().size() > 1) {
                throw new IllegalStateException();
            }
            getExplicitConstructors().forEach((c)->initializeExplicitConstructor(c, resolver));
        }
    }

    private void addConstructorWithParams(List<FormalParameter> allParams, SymbolResolver resolver) {
        List<FormalParameter> paramsWithoutDefaultValues = allParams.stream().filter((p)->!p.hasDefaultValue()).collect(Collectors.toList());
        List<String> paramSignatures = paramsWithoutDefaultValues.stream()
                .map((p) -> p.getType().jvmType(resolver).getSignature())
                .collect(Collectors.toList());
        boolean hasDefaultParameters = allParams.stream().filter((p)->p.hasDefaultValue()).findFirst().isPresent();
        if (hasDefaultParameters) {
            paramSignatures.add("Ljava/util/Map;");
        }
        JvmConstructorDefinition constructorDefinition = new JvmConstructorDefinition(jvmType().getInternalName(), "(" + String.join("", paramSignatures) + ")V");
        constructors.add(new InternalConstructorDefinition(allParams, constructorDefinition));
    }

    private void initializeExplicitConstructor(TurinTypeContructorDefinition constructor, SymbolResolver resolver) {
        List<FormalParameter> allParams = constructor.getParameters();
        List<FormalParameter> paramsWithoutDefaultValues = allParams.stream().filter((p)->!p.hasDefaultValue()).collect(Collectors.toList());
        List<String> paramSignatures = paramsWithoutDefaultValues.stream()
                .map((p) -> p.getType().jvmType(resolver).getSignature())
                .collect(Collectors.toList());
        boolean hasDefaultParameters = allParams.stream().filter((p)->p.hasDefaultValue()).findFirst().isPresent();
        if (hasDefaultParameters) {
            paramSignatures.add("Ljava/util/Map;");
        }
        JvmConstructorDefinition constructorDefinition = new JvmConstructorDefinition(jvmType().getInternalName(), "(" + String.join("", paramSignatures) + ")V");
        constructors.add(new InternalConstructorDefinition(allParams, constructorDefinition));
    }

    private void ensureIsInitialized(SymbolResolver resolver) {
        if (constructors == null) {
            initializeConstructors(resolver);
        }
        if (methodsByName == null) {
            initializeMethodsByName(resolver);
        }
    }

    @Override
    public JvmMethodDefinition findMethodFor(String methodName, List<JvmType> actualParams, SymbolResolver resolver, boolean staticContext) {
        ensureIsInitialized(resolver);
        List<InternalMethodDefinition> methods = methodsByName.get(methodName);
        if (methods.size() == 0) {
            throw new IllegalArgumentException("No method found with name " + methodName);
        } else if (methods.size() == 1) {
            if (methods.get(0).matchJvmTypes(resolver, actualParams)) {
                return methods.get(0).getJvmMethodDefinition();
            } else {
                throw new IllegalArgumentException("No method found with name " + methodName + " which matches " + actualParams);
            }
        } else {
            throw new IllegalStateException("No overloaded methods should be present in Turin types");
        }
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

    /**
     * Properties which can be referred to in the constructor
     */
    public List<Property> assignableProperties(SymbolResolver resolver) {
        return getDirectProperties(resolver).stream().filter((p)->!p.hasInitialValue()).collect(Collectors.toList());
    }

    public List<Property> propertiesAppearingInDefaultConstructor(SymbolResolver resolver) {
        return getDirectProperties(resolver).stream().filter((p)->!p.hasInitialValue() && !p.hasDefaultValue()).collect(Collectors.toList());
    }

    public List<Property> defaultPropeties(SymbolResolver resolver) {
        return getDirectProperties(resolver).stream().filter((p)->p.hasDefaultValue()).collect(Collectors.toList());
    }

    public boolean hasDefaultProperties(SymbolResolver resolver) {
        return getDirectProperties(resolver).stream().filter((p)->p.hasDefaultValue()).findFirst().isPresent();
    }

    @Override
    public JvmConstructorDefinition resolveConstructorCall(SymbolResolver resolver, List<ActualParam> actualParams) {
        // all named parameters should be after the named ones
        if (!ParamUtils.verifyOrder(actualParams)) {
            throw new IllegalArgumentException("Named params should all be grouped after the positional ones");
        }

        ensureIsInitialized(resolver);
        Optional<InternalConstructorDefinition> constructor = constructors.stream().filter((c)->c.match(resolver, actualParams)).findFirst();

        if (!constructor.isPresent()){
            throw new UnsolvedConstructorException(getQualifiedName(), actualParams);
        }

        return constructor.get().getJvmConstructorDefinition();
    }

    @Override
    public TypeUsage getFieldType(String fieldName, boolean staticContext, SymbolResolver resolver) {
        for (Property property : getAllProperties(resolver)) {
            if (property.getName().equals(fieldName)) {
                return property.getTypeUsage();
            }
        }
        throw new IllegalArgumentException(fieldName);
    }

    @Override
    public List<ReferenceTypeUsage> getAllAncestors(SymbolResolver resolver) {
        if (getBaseType().isPresent()) {
            List<ReferenceTypeUsage> res = new ArrayList<>();
            res.add(getBaseType().get().asReferenceTypeUsage());
            res.addAll(getBaseType().get().asReferenceTypeUsage().getAllAncestors(resolver));
            return res;
        }
        return ImmutableList.of(ReferenceTypeUsage.OBJECT);
    }

    @Override
    public boolean isInterface() {
        // TODO when it will be possible to declare interface fix this
        return false;
    }

    @Override
    public boolean isClass() {
        // TODO when it will be possible to declare interface fix this
        return true;
    }

    @Override
    public boolean hasManyConstructors() {
        return false;
    }

    @Override
    public boolean isMethodOverloaded(String methodName, SymbolResolver resolver) {
        ensureIsInitialized(resolver);
        return methodsByName.get(methodName).size() > 1;
    }

    @Override
    public List<FormalParameter> getConstructorParams(List<ActualParam> actualParams, SymbolResolver resolver) {
        // all named parameters should be after the named ones
        if (!ParamUtils.verifyOrder(actualParams)) {
            throw new IllegalArgumentException("Named params should all be grouped after the positional ones");
        }

        ensureIsInitialized(resolver);
        Optional<InternalConstructorDefinition> constructor = constructors.stream().filter((c)->c.match(resolver, actualParams)).findFirst();

        if (!constructor.isPresent()){
            throw new UnsolvedConstructorException(getQualifiedName(), actualParams);
        }

        return constructor.get().getFormalParameters();
    }

    @Override
    public Optional<InternalMethodDefinition> findMethod(String methodName, List<ActualParam> actualParams, SymbolResolver resolver, boolean staticContext) {
        // all named parameters should be after the named ones
        if (!ParamUtils.verifyOrder(actualParams)) {
            throw new IllegalArgumentException("Named params should all be grouped after the positional ones");
        }

        ensureIsInitialized(resolver);
        if (!methodsByName.containsKey(methodName)) {
            return Optional.empty();
        }
        Optional<InternalMethodDefinition> method = methodsByName.get(methodName).stream().filter((m)->m.match(resolver, actualParams)).findFirst();
        return method;
    }

    @Override
    public boolean hasField(String name, boolean staticContext) {
        throw new UnsupportedOperationException();
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

    public List<AnnotationUsage> getAnnotations() {
        return annotations;
    }

    @Override
    public Iterable<Node> getChildren() {
        List<Node> children = new LinkedList<>();
        children.addAll(members);
        children.addAll(annotations);
        if (baseType.isPresent()) {
            children.add(baseType.get());
        }
        children.addAll(interfaces);
        return children;
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

    public List<TurinTypeMethodDefinition> getDirectMethods() {
        List<TurinTypeMethodDefinition> methods = new ArrayList<>();
        for (Node member : members) {
            if (member instanceof TurinTypeMethodDefinition) {
                methods.add((TurinTypeMethodDefinition)member);
            }
        }
        return methods;
    }

    public List<TypeUsage> getInterfaces() {
        return interfaces;
    }

    public Optional<TypeUsage> getBaseType() {
        return baseType;
    }

    /**
     * Get direct and inherited properties.
     */
    public List<Property> getAllProperties(SymbolResolver resolver) {
        // TODO consider also inherited properties
        return getDirectProperties(resolver);
    }

    public void add(TurinTypeMethodDefinition methodDefinition) {
        members.add(methodDefinition);
        methodDefinition.parent = this;
    }

    public void add(TurinTypeContructorDefinition contructorDefinition) {
        members.add(contructorDefinition);
        contructorDefinition.parent = this;
    }

    @Override
    public boolean canFieldBeAssigned(String field, SymbolResolver resolver) {
        return true;
    }

    public boolean defineExplicitConstructor(SymbolResolver resolver) {
        return !getExplicitConstructors().isEmpty();
    }

    @Override
    public TypeDefinition getSuperclass(SymbolResolver resolver) {
        if (this.baseType.isPresent()) {
            return this.baseType.get().asReferenceTypeUsage().getTypeDefinition(resolver);
        }
        return ReflectionTypeDefinitionFactory.getInstance().getTypeDefinition(Object.class);
    }

    @Override
    public Optional<JvmConstructorDefinition> getConstructor(List<ActualParam> actualParams, SymbolResolver resolver) {
        for (InternalConstructorDefinition constructor : constructors) {
            if (constructor.match(resolver, actualParams)) {
                return Optional.of(constructor.getJvmConstructorDefinition());
            }
        }
        return Optional.empty();
    }
}
