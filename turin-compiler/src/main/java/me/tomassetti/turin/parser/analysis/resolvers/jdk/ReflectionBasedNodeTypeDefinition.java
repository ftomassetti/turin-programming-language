package me.tomassetti.turin.parser.analysis.resolvers.jdk;

import me.tomassetti.turin.compiler.errorhandling.SemanticErrorException;
import me.tomassetti.jvm.JvmConstructorDefinition;
import me.tomassetti.jvm.JvmMethodDefinition;
import me.tomassetti.jvm.JvmType;
import me.tomassetti.turin.parser.analysis.symbols_definitions.InternalConstructorDefinition;
import me.tomassetti.turin.parser.analysis.symbols_definitions.InternalMethodDefinition;
import me.tomassetti.turin.parser.analysis.exceptions.UnsolvedSymbolException;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.FormalParameter;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.NodeTypeDefinition;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;
import me.tomassetti.turin.parser.ast.expressions.Invokable;
import me.tomassetti.turin.parser.ast.typeusage.*;
import me.tomassetti.turin.typesystem.ReflectionBasedTypeDefinition;
import me.tomassetti.turin.typesystem.TypeDefinition;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

@Deprecated
class ReflectionBasedNodeTypeDefinition extends NodeTypeDefinition {

    private Class<?> clazz;
    private List<TypeUsage> typeParameters = new LinkedList<>();

    public ReflectionBasedNodeTypeDefinition(Class<?> clazz) {
        super(clazz.getCanonicalName());
        this.clazz = clazz;
        this.typeDefinition = new ReflectionBasedTypeDefinition(clazz);
    }

    public void addTypeParameter(TypeUsage typeUsage) {
        typeParameters.add(typeUsage);
    }

    private static TypeUsage typeFor(List<Method> methods, Node parentToAssign) {
        if (methods.isEmpty()) {
            throw new IllegalArgumentException();
        }
        methods.forEach((m)-> {
            if (!Modifier.isStatic(m.getModifiers())) {
                throw new IllegalArgumentException("Non static method given: " + m);
            }
        });
        if (methods.size() != 1) {
            throw new UnsupportedOperationException();
        }
        return typeFor(methods.get(0), parentToAssign);
    }

    private static TypeUsage typeFor(Method method, Node parentToAssign) {
        List<TypeUsage> paramTypes = Arrays.stream(method.getGenericParameterTypes()).map((pt)->toTypeUsage(pt)).collect(Collectors.toList());
        FunctionReferenceTypeUsage functionReferenceTypeUsage = new FunctionReferenceTypeUsage(paramTypes, toTypeUsage(method.getGenericReturnType()));
        functionReferenceTypeUsage.setParent(parentToAssign);
        return functionReferenceTypeUsage;
    }

    private static TypeUsage toTypeUsage(Type type) {
        return ReflectionBasedMethodResolution.toTypeUsage(type, Collections.emptyMap());
    }

    private static TypeUsage toTypeUsage(TypeVariable typeVariable) {
        return ReflectionBasedMethodResolution.toTypeUsage(typeVariable, Collections.emptyMap());
    }

    @Override
    public Optional<List<FormalParameter>> findFormalParametersFor(Invokable invokable, SymbolResolver resolver) {
        return super.findFormalParametersFor(invokable, resolver);
    }

    @Override
    public boolean hasField(String fieldName, boolean staticContext) {
        for (Field field : clazz.getFields()) {
            if (field.getName().equals(fieldName)) {
                if (Modifier.isStatic(field.getModifiers()) == staticContext) {
                    return true;
                }
            }
        }

        for (Method method : clazz.getMethods()) {
            if (method.getName().equals(fieldName)) {
                if (Modifier.isStatic(method.getModifiers()) == staticContext) {
                    return true;
                }
            }
        }

        // TODO consider inherited fields and methods
        return false;
    }

    @Override
    public List<InternalConstructorDefinition> getConstructors(SymbolResolver resolver) {
        return Arrays.stream(clazz.getConstructors())
                .map((c) -> toInternalConstructorDefinition(c))
                .collect(Collectors.toList());
    }

    @Override
    public boolean canFieldBeAssigned(String field, SymbolResolver resolver) {
        return true;
    }

    @Override
    public NodeTypeDefinition getSuperclass(SymbolResolver resolver) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, TypeUsage> associatedTypeParametersToName(SymbolResolver resolver, List<TypeUsage> typeParams) {
        if (typeParams.isEmpty()) {
            return Collections.emptyMap();
        }
        if (clazz.getTypeParameters().length != typeParams.size()) {
            throw new IllegalStateException("It should have " + clazz.getTypeParameters().length + " and it has " + typeParams.size());
        }
        Map<String, TypeUsage> map = new HashMap<>();
        int i=0;
        for (TypeVariable tv : clazz.getTypeParameters()) {
            map.put(tv.getName(), typeParams.get(i));
            i++;
        }
        return map;
    }

    @Override
    public Optional<InternalConstructorDefinition> findConstructor(List<ActualParam> actualParams, SymbolResolver resolver) {
        Constructor constructor = ReflectionBasedMethodResolution.findConstructorAmongActualParams(
                actualParams, resolver, Arrays.asList(clazz.getConstructors()), this);
        return Optional.of(toInternalConstructorDefinition(constructor));
    }

    private InternalConstructorDefinition toInternalConstructorDefinition(Constructor<?> constructor) {
        JvmConstructorDefinition jvmConstructorDefinition = ReflectionTypeDefinitionFactory.toConstructorDefinition(constructor);
        return new InternalConstructorDefinition(formalParameters(constructor), jvmConstructorDefinition);
    }

    @Override
    public String toString() {
        return "ReflectionBasedTypeDefinition{" +
                "clazz=" + clazz +
                '}';
    }

    @Override
    public JvmMethodDefinition findMethodFor(String name, List<JvmType> argsTypes, SymbolResolver resolver, boolean staticContext) {
        return ReflectionTypeDefinitionFactory.toMethodDefinition(ReflectionBasedMethodResolution.findMethodAmong(name, argsTypes, resolver, staticContext, Arrays.asList(clazz.getMethods()), this));
    }

    @Override
    public JvmConstructorDefinition resolveConstructorCall(SymbolResolver resolver, List<ActualParam> actualParams) {
        try {
            List<JvmType> argsTypes = new ArrayList<>();
            for (ActualParam actualParam : actualParams) {
                if (actualParam.isNamed()) {
                    throw new SemanticErrorException(actualParam, "It is not possible to use named parameters on Java classes");
                } else {
                    argsTypes.add(actualParam.getValue().calcType(resolver).jvmType(resolver));
                }
            }
            return ReflectionBasedMethodResolution.findConstructorAmong(argsTypes, resolver, Arrays.asList(clazz.getConstructors()), this);
        } catch (RuntimeException e){
            throw new RuntimeException("Resolving constructor call on " + clazz.getCanonicalName(), e);
        }
    }

    @Override
    public boolean isMethodOverloaded(String methodName, SymbolResolver resolver) {
        return Arrays.stream(clazz.getMethods()).filter((m)->m.getName().equals(methodName)).count() > 1;
    }

    @Override
    public Optional<InternalMethodDefinition> findMethod(String methodName, List<ActualParam> actualParams, SymbolResolver resolver, boolean staticContext) {
        Optional<Method> res = ReflectionBasedMethodResolution.findMethodAmongActualParams(methodName, actualParams, resolver, staticContext, Arrays.asList(clazz.getMethods()), this);
        if (res.isPresent()) {
            return Optional.of(toInternalMethodDefinition(res.get()));
        } else {
            return Optional.empty();
        }
    }

    private InternalMethodDefinition toInternalMethodDefinition(Method method) {
        return new InternalMethodDefinition(method.getName(), formalParameters(method), toTypeUsage(method.getGenericReturnType()),
                ReflectionTypeDefinitionFactory.toMethodDefinition(method));
    }

    private List<FormalParameter> formalParameters(Constructor constructor) {
        return ReflectionBasedMethodResolution.formalParameters(constructor);
    }

    private List<FormalParameter> formalParameters(Method method) {
        return ReflectionBasedMethodResolution.formalParameters(method, getTypeVariables());
    }

    // Type parameters should be part of the usage
    @Deprecated
    private Map<String, TypeUsage> getTypeVariables() {
        Map<String, TypeUsage> map = new HashMap<>();
        if (clazz.getTypeParameters().length != typeParameters.size()) {
            throw new IllegalStateException("It should have " + clazz.getTypeParameters().length + " and it has " + typeParameters.size());
        }
        int i=0;
        for (TypeVariable tv : clazz.getTypeParameters()) {
            map.put(tv.getName(), typeParameters.get(i));
            i++;
        }
        return map;
    }

    @Override
    public TypeUsage getFieldType(String fieldName, boolean staticContext, SymbolResolver resolver) {
        for (Field field : clazz.getFields()) {
            if (field.getName().equals(fieldName)) {
                if (Modifier.isStatic(field.getModifiers()) == staticContext) {
                    return ReflectionTypeDefinitionFactory.toTypeUsage(field.getType());
                }
            }
        }

        List<Method> methods = new LinkedList<>();
        for (Method method : clazz.getMethods()) {
            if (method.getName().equals(fieldName)) {
                if (Modifier.isStatic(method.getModifiers()) == staticContext) {
                    methods.add(method);
                }
            }
        }
        if (!methods.isEmpty()) {
            return ReflectionBasedNodeTypeDefinition.typeFor(methods, this);
        }

        // TODO consider inherited fields and methods
        throw new UnsupportedOperationException(fieldName);
    }



    private ReflectionBasedTypeDefinition typeDefinition;

    @Override
    public TypeDefinition typeDefinition() {
        return typeDefinition;
    }

    @Override
    public Iterable<Node> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public Node getField(String fieldName, SymbolResolver resolver) {
        return internalGetField(fieldName, null);
    }

    @Override
    public Node getFieldOnInstance(String fieldName, Node instance, SymbolResolver resolver) {
        return internalGetField(fieldName, instance);
    }

    /**
     * Instance null means get static fields.
     */
    private Node internalGetField(String fieldName, Node instance) {
        boolean isStatic = instance == null;
        for (Field field : clazz.getFields()) {
            if (field.getName().equals(fieldName) && Modifier.isStatic(field.getModifiers()) == isStatic) {
                ReflectionBasedField rbf = new ReflectionBasedField(field);
                rbf.setParent(instance);
                return rbf;
            }
        }
        List<Method> matchingMethods = new ArrayList<>();
        for (Method method : clazz.getMethods()) {
            if (method.getName().equals(fieldName) && Modifier.isStatic(method.getModifiers()) == isStatic){
                matchingMethods.add(method);
            }
        }
        if (matchingMethods.isEmpty()) {
            // TODO improve the error returned
            throw new UnsolvedSymbolException(fieldName);
        } else {
            ReflectionBasedSetOfOverloadedMethods rbsoom = new ReflectionBasedSetOfOverloadedMethods(matchingMethods, instance);
            rbsoom.setParent(instance);
            return rbsoom;
        }
    }
}
