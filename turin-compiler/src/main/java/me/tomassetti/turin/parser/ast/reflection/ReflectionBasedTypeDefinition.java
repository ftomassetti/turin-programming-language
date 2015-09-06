package me.tomassetti.turin.parser.ast.reflection;

import me.tomassetti.turin.compiler.SemanticErrorException;
import me.tomassetti.turin.jvm.JvmConstructorDefinition;
import me.tomassetti.turin.jvm.JvmMethodDefinition;
import me.tomassetti.turin.jvm.JvmType;
import me.tomassetti.turin.parser.analysis.UnsolvedSymbolException;
import me.tomassetti.turin.parser.analysis.resolvers.Resolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.TypeDefinition;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;
import me.tomassetti.turin.parser.ast.typeusage.ReferenceTypeUsage;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
        return ReflectionBasedMethodResolution.findMethodAmong(name, argsTypes, resolver, staticContext, Arrays.asList(clazz.getMethods()), this);
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
    public TypeUsage getField(String fieldName, boolean staticContext) {
        for (Field field : clazz.getFields()) {
            if (field.getName().equals(fieldName)) {
                if (Modifier.isStatic(field.getModifiers()) == staticContext) {
                    return ReflectionTypeDefinitionFactory.toTypeUsage(field.getType());
                }
            }
        }
        throw new UnsupportedOperationException();
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

    private TypeUsage toTypeUsage(Type parameterType) {
        if (parameterType instanceof Class){
            TypeDefinition typeDefinition = new ReflectionBasedTypeDefinition(clazz);
            ReferenceTypeUsage referenceTypeUsage = new ReferenceTypeUsage(typeDefinition);
            return referenceTypeUsage;
        } else {
            throw new UnsupportedOperationException(parameterType.getClass().getCanonicalName());
        }
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
