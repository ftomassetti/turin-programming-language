package me.tomassetti.java.symbol_solver;

import me.tomassetti.java.symbol_solver.type_usage.*;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class ReflectionBasedMethodResolution {

    private static class MethodOrConstructor {
        private Constructor constructor;
        private Method method;

        public MethodOrConstructor(Constructor constructor) {
            this.constructor = constructor;
        }

        public MethodOrConstructor(Method method) {
            this.method = method;
        }

        public int getParameterCount() {
            if (method != null) {
                return method.getParameterCount();
            } else {
                return constructor.getParameterCount();
            }
        }

        public Class<?> getParameterType(int i) {
            if (method != null) {
                return method.getParameterTypes()[i];
            } else {
                return constructor.getParameterTypes()[i];
            }
        }
    }

    public static List<FormalParameter> formalParameters(Constructor constructor) {
        List<FormalParameter> formalParameters = new ArrayList<>();
        int i=0;
        for (Type type : constructor.getGenericParameterTypes()) {
            formalParameters.add(new FormalParameter(toTypeUsage(type), constructor.getParameters()[i].getName()));
            i++;
        }
        return formalParameters;
    }

    public static List<FormalParameter> formalParameters(Method method) {
        List<FormalParameter> formalParameters = new ArrayList<>();
        int i=0;
        for (Type type : method.getGenericParameterTypes()) {
            formalParameters.add(new FormalParameter(toTypeUsage(type), method.getParameters()[i].getName()));
            i++;
        }
        return formalParameters;
    }

    public static JavaTypeUsage toTypeUsage(Type type) {
        if (type instanceof Class) {
            Class clazz = (Class)type;
            if (clazz.getCanonicalName().equals(void.class.getCanonicalName())) {
                return new VoidTypeUsage();
            }
            if (clazz.isPrimitive()) {
                return PrimitiveTypeUsage.getByName(clazz.getName());
            }
            if (clazz.isArray()) {
                return new ArrayTypeUsage(toTypeUsage(clazz.getComponentType()));
            }
            JavaTypeDefinition typeDefinition = new ReflectionBasedTypeDefinition((Class) type);
            ReferenceTypeUsage referenceTypeUsage = new ReferenceTypeUsage(typeDefinition);
            return referenceTypeUsage;
        } else if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            JavaTypeDefinition typeDefinition = new ReflectionBasedTypeDefinition((Class) parameterizedType.getRawType());
            List<JavaTypeUsage> typeParams = Arrays.stream(parameterizedType.getActualTypeArguments()).map((pt) -> toTypeUsage(pt)).collect(Collectors.toList());
            return new ReferenceTypeUsage(typeDefinition, typeParams);
        } else if (type instanceof TypeVariable) {
            TypeVariable typeVariable = (TypeVariable)type;
            return toTypeUsage(typeVariable);
        } else {
            throw new UnsupportedOperationException(type.getClass().getCanonicalName());
        }
    }

    public static JavaTypeUsage toTypeUsage(TypeVariable typeVariable) {
        TypeVariableTypeUsage.GenericDeclaration genericDeclaration = null;
        List<JavaTypeUsage> bounds = Arrays.stream(typeVariable.getBounds()).map((b)->toTypeUsage(b)).collect(Collectors.toList());
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



}
