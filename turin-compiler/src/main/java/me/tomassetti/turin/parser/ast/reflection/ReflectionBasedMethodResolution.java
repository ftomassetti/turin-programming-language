package me.tomassetti.turin.parser.ast.reflection;

import me.tomassetti.turin.compiler.AmbiguousCallException;
import me.tomassetti.turin.jvm.JvmMethodDefinition;
import me.tomassetti.turin.jvm.JvmType;
import me.tomassetti.turin.parser.analysis.resolvers.Resolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.typeusage.ReferenceTypeUsage;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

class ReflectionBasedMethodResolution {

    public static JvmMethodDefinition findMethodAmong(String name, List<JvmType> argsTypes, Resolver resolver, boolean staticContext, List<Method> methods, Node context) {
        List<Method> suitableMethods = new ArrayList<>();
        for (Method method : methods) {
            if (method.getName().equals(name)) {
                if (method.getParameterCount() == argsTypes.size()) {
                    if (Modifier.isStatic(method.getModifiers()) == staticContext) {
                        boolean match = true;
                        for (int i=0; i<argsTypes.size(); i++) {
                            TypeUsage actualType = argsTypes.get(i).toTypeUsage();
                            TypeUsage formalType = ReflectionTypeDefinitionFactory.toTypeUsage(method.getParameterTypes()[i]);
                            if (!actualType.canBeAssignedTo(formalType, resolver)) {
                                match = false;
                            }
                        }
                        if (match) {
                            suitableMethods.add(method);
                        }
                    }
                }
            }
        }

        if (suitableMethods.size() == 0) {
            throw new RuntimeException("unresolved method " + name);
        } else if (suitableMethods.size() == 1) {
            return ReflectionTypeDefinitionFactory.toMethodDefinition(suitableMethods.get(0));
        } else {
            return ReflectionTypeDefinitionFactory.toMethodDefinition(findMostSpecific(suitableMethods, new AmbiguousCallException(context, name, argsTypes), resolver));
        }
    }

    private static Method findMostSpecific(List<Method> methods, AmbiguousCallException exceptionToThrow, Resolver resolver) {
        Method winningMethod = methods.get(0);
        for (Method other : methods.subList(1, methods.size())) {
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

    private static boolean isTheFirstMoreSpecific(Method first, Method second, Resolver resolver) {
        boolean atLeastOneParamIsMoreSpecific = false;
        if (first.getParameterCount() != second.getParameterCount()) {
            throw new IllegalArgumentException();
        }
        for (int i=0;i<first.getParameterTypes().length;i++){
            Class<?> paramFirst = first.getParameterTypes()[i];
            Class<?> paramSecond = second.getParameterTypes()[i];
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
