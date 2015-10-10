package me.tomassetti.java.symbol_solver;

import me.tomassetti.java.symbol_solver.symbols_definitions.InternalConstructorDefinition;
import me.tomassetti.java.symbol_solver.symbols_definitions.InternalMethodDefinition;
import me.tomassetti.java.symbol_solver.type_usage.JavaTypeUsage;
import me.tomassetti.java.symbol_solver.type_usage.ReferenceTypeUsage;
import me.tomassetti.jvm.JvmConstructorDefinition;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class ReflectionBasedTypeDefinition extends JavaTypeDefinition {

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

    public List<InternalConstructorDefinition> getConstructors(JavaTypeResolver resolver) {
        return Arrays.stream(clazz.getConstructors())
                .map((c) -> toInternalConstructorDefinition(c))
                .collect(Collectors.toList());
    }

    public JavaTypeDefinition getSuperclass(JavaTypeResolver resolver) {
        throw new UnsupportedOperationException();
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

    private Class<?> clazz;

    public ReflectionBasedTypeDefinition(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public String getQualifiedName() {
        return clazz.getCanonicalName();
    }

    private InternalMethodDefinition toInternalMethodDefinition(Method method) {
        return new InternalMethodDefinition(method.getName(), formalParameters(method), toTypeUsage(method.getReturnType()),
                ReflectionTypeDefinitionFactory.toMethodDefinition(method));
    }

    private List<FormalParameter> formalParameters(Constructor constructor) {
        return ReflectionBasedMethodResolution.formalParameters(constructor);
    }

    private List<FormalParameter> formalParameters(Method method) {
        return ReflectionBasedMethodResolution.formalParameters(method);
    }

    @Override
    public List<ReferenceTypeUsage> getAllAncestors(JavaTypeResolver resolver) {
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

    @Override
    public boolean isClass() {
        return !clazz.isInterface() && !clazz.isEnum() && !clazz.isAnnotation() && !clazz.isArray() && !clazz.isPrimitive();
    }

    private ReferenceTypeUsage toReferenceTypeUsage(Class<?> clazz, Type type) {
        JavaTypeDefinition typeDefinition = new ReflectionBasedTypeDefinition(clazz);
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

    private static JavaTypeUsage toTypeUsage(Type type) {
        return ReflectionBasedMethodResolution.toTypeUsage(type);
    }

    private static JavaTypeUsage toTypeUsage(TypeVariable typeVariable) {
        return ReflectionBasedMethodResolution.toTypeUsage(typeVariable);
    }

}
