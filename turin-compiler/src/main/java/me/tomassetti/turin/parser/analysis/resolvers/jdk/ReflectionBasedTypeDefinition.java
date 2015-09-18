package me.tomassetti.turin.parser.analysis.resolvers.jdk;

import me.tomassetti.turin.compiler.SemanticErrorException;
import me.tomassetti.turin.jvm.JvmConstructorDefinition;
import me.tomassetti.turin.jvm.JvmMethodDefinition;
import me.tomassetti.turin.jvm.JvmType;
import me.tomassetti.turin.parser.analysis.UnsolvedSymbolException;
import me.tomassetti.turin.parser.analysis.resolvers.Resolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.TypeDefinition;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;
import me.tomassetti.turin.parser.ast.typeusage.FunctionReferenceTypeUsage;
import me.tomassetti.turin.parser.ast.typeusage.ReferenceTypeUsage;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;
import me.tomassetti.turin.parser.ast.typeusage.TypeVariableTypeUsage;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

class ReflectionBasedTypeDefinition extends TypeDefinition {

    @Override
    public String toString() {
        return "ReflectionBasedTypeDefinition{" +
                "clazz=" + clazz +
                '}';
    }

    private Class<?> clazz;

    public ReflectionBasedTypeDefinition(Class<?> clazz) {
        super(clazz.getCanonicalName());
        this.clazz = clazz;
    }

    @Override
    public String getQualifiedName() {
        return clazz.getCanonicalName();
    }

    @Override
    public JvmMethodDefinition findMethodFor(String name, List<JvmType> argsTypes, Resolver resolver, boolean staticContext) {
        return ReflectionTypeDefinitionFactory.toMethodDefinition(ReflectionBasedMethodResolution.findMethodAmong(name, argsTypes, resolver, staticContext, Arrays.asList(clazz.getMethods()), this));
    }

    @Override
    public JvmConstructorDefinition resolveConstructorCall(Resolver resolver, List<ActualParam> actualParams) {
        List<JvmType> argsTypes = new ArrayList<>();
        for (ActualParam actualParam : actualParams) {
            if (actualParam.isNamed()) {
                throw new SemanticErrorException(actualParam, "It is not possible to use named parameters on Java classes");
            } else {
                argsTypes.add(actualParam.getValue().calcType(resolver).jvmType(resolver));
            }
        }
        return ReflectionBasedMethodResolution.findConstructorAmong(argsTypes, resolver, Arrays.asList(clazz.getConstructors()), this);
    }

    @Override
    public TypeUsage returnTypeWhenInvokedWith(String methodName, List<ActualParam> actualParams, Resolver resolver, boolean staticContext) {
        List<JvmType> argsTypes = new ArrayList<>();
        for (ActualParam actualParam : actualParams) {
            if (actualParam.isNamed()) {
                throw new SemanticErrorException(actualParam, "It is not possible to use named parameters on Java classes");
            } else {
                argsTypes.add(actualParam.getValue().calcType(resolver).jvmType(resolver));
            }
        }
        Method method = ReflectionBasedMethodResolution.findMethodAmong(methodName, argsTypes, resolver, staticContext, Arrays.asList(clazz.getMethods()), this);
        return toTypeUsage(method.getReturnType());
    }

    @Override
    public TypeUsage getField(String fieldName, boolean staticContext) {
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
            return ReflectionBasedTypeDefinition.typeFor(methods, this);
        }

        // TODO consider inherited fields and methods
        throw new UnsupportedOperationException(fieldName);
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

    @Override
    public List<ReferenceTypeUsage> getAllAncestors(Resolver resolver) {
        List<ReferenceTypeUsage> ancestors = new ArrayList<>();
        if (clazz.getSuperclass() != null) {
            ReferenceTypeUsage superTypeDefinition = toReferenceTypeUsage(clazz.getSuperclass(), clazz.getGenericSuperclass());
            ancestors.add(superTypeDefinition);
            ancestors.addAll(superTypeDefinition.getAllAncestors(resolver));
        }
        int i = 0;
        for (Class<?> interfaze : clazz.getInterfaces()) {
            Type genericInterfaze = clazz.getGenericInterfaces()[i];
            ReferenceTypeUsage superTypeDefinition = toReferenceTypeUsage(interfaze, genericInterfaze);
            ancestors.add(superTypeDefinition);
            ancestors.addAll(superTypeDefinition.getAllAncestors(resolver));
            i++;
        }
        return ancestors;
    }

    @Override
    public boolean isInterface() {
        return clazz.isInterface();
    }

    private ReferenceTypeUsage toReferenceTypeUsage(Class<?> clazz, Type type) {
        TypeDefinition typeDefinition = new ReflectionBasedTypeDefinition(clazz);
        ReferenceTypeUsage referenceTypeUsage = new ReferenceTypeUsage(typeDefinition);
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType)type;
            for (int tp=0;tp<clazz.getTypeParameters().length;tp++) {
                TypeVariable<? extends Class<?>> typeVariable = clazz.getTypeParameters()[tp];
                Type parameterType = parameterizedType.getActualTypeArguments()[tp];
                referenceTypeUsage.getTypeParameterValues().add(typeVariable.getName(), toTypeUsage(parameterType));
            }
        }
        return referenceTypeUsage;
    }

    private static TypeUsage toTypeUsage(Type type) {
        if (type instanceof Class) {
            TypeDefinition typeDefinition = new ReflectionBasedTypeDefinition((Class) type);
            ReferenceTypeUsage referenceTypeUsage = new ReferenceTypeUsage(typeDefinition);
            return referenceTypeUsage;
        } else if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            TypeDefinition typeDefinition = new ReflectionBasedTypeDefinition((Class) parameterizedType.getRawType());
            List<TypeUsage> typeParams = Arrays.stream(parameterizedType.getActualTypeArguments()).map((pt) -> toTypeUsage(pt)).collect(Collectors.toList());
            return new ReferenceTypeUsage(typeDefinition, typeParams);
        } else if (type instanceof TypeVariable) {
            TypeVariable typeVariable = (TypeVariable)type;
            return toTypeUsage(typeVariable);
        } else {
            throw new UnsupportedOperationException(type.getClass().getCanonicalName());
        }
    }

    private static TypeUsage toTypeUsage(TypeVariable typeVariable) {
        TypeVariableTypeUsage.GenericDeclaration genericDeclaration = null;
        List<TypeUsage> bounds = Arrays.stream(typeVariable.getBounds()).map((b)->toTypeUsage(b)).collect(Collectors.toList());
        if (typeVariable.getGenericDeclaration() instanceof Class) {
            throw new UnsupportedOperationException();
        } else if (typeVariable.getGenericDeclaration() instanceof Method) {
            Method method = (Method)typeVariable.getGenericDeclaration();
            genericDeclaration = TypeVariableTypeUsage.GenericDeclaration.onMethod(method.getDeclaringClass().getCanonicalName(), ReflectionTypeDefinitionFactory.toMethodDefinition(method).getDescriptor());
        } else {
            throw new UnsupportedOperationException(typeVariable.getGenericDeclaration().getClass().getCanonicalName());
        }
        return new TypeVariableTypeUsage(genericDeclaration, typeVariable.getName(), bounds);
    }

    @Override
    public Iterable<Node> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public Node getField(String fieldName, Resolver resolver) {
        return internalGetField(fieldName, null);
    }

    @Override
    public Node getFieldOnInstance(String fieldName, Node instance, Resolver resolver) {
        return internalGetField(fieldName, instance);
    }

    /**
     * Instance null means get static fields.
     */
    private Node internalGetField(String fieldName, Node instance) {
        boolean isStatic = instance == null;
        for (Field field : clazz.getFields()) {
            if (field.getName().equals(fieldName) && Modifier.isStatic(field.getModifiers()) == isStatic) {
                return new ReflectionBaseField(field);
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
            return new ReflectionBasedSetOfOverloadedMethods(matchingMethods, instance);
        }
    }
}
