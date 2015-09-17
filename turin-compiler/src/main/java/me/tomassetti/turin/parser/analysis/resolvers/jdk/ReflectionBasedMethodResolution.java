package me.tomassetti.turin.parser.analysis.resolvers.jdk;

import me.tomassetti.turin.compiler.AmbiguousCallException;
import me.tomassetti.turin.jvm.JvmConstructorDefinition;
import me.tomassetti.turin.jvm.JvmMethodDefinition;
import me.tomassetti.turin.jvm.JvmType;
import me.tomassetti.turin.parser.analysis.resolvers.Resolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.typeusage.ReferenceTypeUsage;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
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

    public static JvmConstructorDefinition findConstructorAmong(List<JvmType> argsTypes, Resolver resolver, List<Constructor> constructors, Node context) {
        List<MethodOrConstructor> methodOrConstructors = constructors.stream().map((m)->new MethodOrConstructor(m)).collect(Collectors.toList());
        MethodOrConstructor methodOrConstructor = findMethodAmong(argsTypes, resolver, methodOrConstructors, context, "constructor");
        if (methodOrConstructor == null) {
            throw new RuntimeException("unresolved constructor for " + argsTypes);
        }
        return ReflectionTypeDefinitionFactory.toConstructorDefinition(methodOrConstructor.constructor);
    }

    public static Method findMethodAmong(String name, List<JvmType> argsTypes, Resolver resolver, boolean staticContext, List<Method> methods, Node context) {
        List<MethodOrConstructor> methodOrConstructors = methods.stream()
                .filter((m) -> Modifier.isStatic(m.getModifiers()) == staticContext)
                .filter((m) -> m.getName().equals(name))
                .map((m) -> new MethodOrConstructor(m)).collect(Collectors.toList());
        MethodOrConstructor methodOrConstructor = findMethodAmong(argsTypes, resolver, methodOrConstructors, context, name);
        if (methodOrConstructor == null) {
            throw new RuntimeException("unresolved method " + name + " for " + argsTypes);
        }
        return methodOrConstructor.method;
    }

    private static MethodOrConstructor findMethodAmong(List<JvmType> argsTypes, Resolver resolver, List<MethodOrConstructor> methods, Node context, String desc) {
        List<MethodOrConstructor> suitableMethods = new ArrayList<>();
        for (MethodOrConstructor method : methods) {
            if (method.getParameterCount() == argsTypes.size()) {
                boolean match = true;
                for (int i = 0; i < argsTypes.size(); i++) {
                    TypeUsage actualType = argsTypes.get(i).toTypeUsage();
                    TypeUsage formalType = ReflectionTypeDefinitionFactory.toTypeUsage(method.getParameterType(i));
                    if (!actualType.canBeAssignedTo(formalType, resolver)) {
                        match = false;
                    }
                }
                if (match) {
                    suitableMethods.add(method);
                }
            }
        }

        if (suitableMethods.size() == 0) {
            return null;
        } else if (suitableMethods.size() == 1) {
            return suitableMethods.get(0);
        } else {
            return findMostSpecific(suitableMethods, new AmbiguousCallException(context, desc, argsTypes), resolver);
        }
    }

    private static MethodOrConstructor findMostSpecific(List<MethodOrConstructor> methods, AmbiguousCallException exceptionToThrow, Resolver resolver) {
        MethodOrConstructor winningMethod = methods.get(0);
        for (MethodOrConstructor other : methods.subList(1, methods.size())) {
            if (isTheFirstMoreSpecific(winningMethod, other, resolver)) {
            } else if (isTheFirstMoreSpecific(other, winningMethod, resolver)) {
                winningMethod = other;
            } else if (!isTheFirstMoreSpecific(winningMethod, other, resolver)) {
                // neither is more specific
                throw exceptionToThrow;
            }
        }
        return winningMethod;
    }

    private static boolean isTheFirstMoreSpecific(MethodOrConstructor first, MethodOrConstructor second, Resolver resolver) {
        boolean atLeastOneParamIsMoreSpecific = false;
        if (first.getParameterCount() != second.getParameterCount()) {
            throw new IllegalArgumentException();
        }
        for (int i=0;i<first.getParameterCount();i++){
            Class<?> paramFirst = first.getParameterType(i);
            Class<?> paramSecond = second.getParameterType(i);
            if (isTheFirstMoreSpecific(paramFirst, paramSecond, resolver)) {
                atLeastOneParamIsMoreSpecific = true;
            } else if (isTheFirstMoreSpecific(paramSecond, paramFirst, resolver)) {
                return false;
            }
        }

        return atLeastOneParamIsMoreSpecific;
    }

    private static boolean isTheFirstMoreSpecific(Class<?> firstType, Class<?> secondType, Resolver resolver) {
        if (firstType.isPrimitive() || firstType.isArray()) {
            return false;
        }
        if (secondType.isPrimitive() || secondType.isArray()) {
            return false;
        }
        // TODO consider generic parameters?
        ReflectionBasedTypeDefinition firstDef = new ReflectionBasedTypeDefinition(firstType);
        ReflectionBasedTypeDefinition secondDef = new ReflectionBasedTypeDefinition(secondType);
        TypeUsage firstTypeUsage = new ReferenceTypeUsage(firstDef);
        TypeUsage secondTypeUsage = new ReferenceTypeUsage(secondDef);
        return firstTypeUsage.canBeAssignedTo(secondTypeUsage, resolver) && !secondTypeUsage.canBeAssignedTo(firstTypeUsage, resolver);
    }

}
