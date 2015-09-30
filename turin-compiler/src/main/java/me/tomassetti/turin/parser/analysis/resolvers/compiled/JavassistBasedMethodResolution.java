package me.tomassetti.turin.parser.analysis.resolvers.compiled;

import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.NotFoundException;
import me.tomassetti.turin.compiler.AmbiguousCallException;
import me.tomassetti.turin.jvm.JvmType;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;
import me.tomassetti.turin.parser.ast.typeusage.ReferenceTypeUsage;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JavassistBasedMethodResolution {

    private static class MethodOrConstructor {
        private CtConstructor constructor;
        private CtMethod method;

        public MethodOrConstructor(CtConstructor constructor) {
            this.constructor = constructor;
        }

        public MethodOrConstructor(CtMethod method) {
            this.method = method;
        }

        public int getParameterCount() throws NotFoundException {
            if (method != null) {
                return method.getParameterTypes().length;
            } else {
                return constructor.getParameterTypes().length;
            }
        }

        public CtClass getParameterType(int i) throws NotFoundException {
            if (method != null) {
                return method.getParameterTypes()[i];
            } else {
                return constructor.getParameterTypes()[i];
            }
        }
    }

    public static CtConstructor findConstructorAmong(List<JvmType> argsTypes, SymbolResolver resolver, List<CtConstructor> constructors, Node context) {
        try {
            List<MethodOrConstructor> methodOrConstructors = constructors.stream().map((m) -> new MethodOrConstructor(m)).collect(Collectors.toList());
            MethodOrConstructor methodOrConstructor = findMethodAmong(argsTypes, resolver, methodOrConstructors, context, "constructor");
            if (methodOrConstructor == null) {
                throw new RuntimeException("unresolved constructor for " + argsTypes);
            }
            return methodOrConstructor.constructor;
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static CtConstructor findConstructorAmongActualParams(List<ActualParam> argsTypes, SymbolResolver resolver, List<CtConstructor> constructors, Node context) {
        try {
            List<MethodOrConstructor> methodOrConstructors = constructors.stream().map((m) -> new MethodOrConstructor(m)).collect(Collectors.toList());
            MethodOrConstructor methodOrConstructor = findMethodAmongActualParams(argsTypes, resolver, methodOrConstructors, context, "constructor");
            if (methodOrConstructor == null) {
                throw new RuntimeException("unresolved constructor for " + argsTypes);
            }
            return methodOrConstructor.constructor;
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    public static CtMethod findMethodAmong(String name, List<JvmType> argsTypes, SymbolResolver resolver, boolean staticContext, List<CtMethod> methods, Node context) {
        try {
            List<MethodOrConstructor> methodOrConstructors = methods.stream()
                    .filter((m) -> Modifier.isStatic(m.getModifiers()) == staticContext)
                    .filter((m) -> m.getName().equals(name))
                    .map((m) -> new MethodOrConstructor(m)).collect(Collectors.toList());
            MethodOrConstructor methodOrConstructor = findMethodAmong(argsTypes, resolver, methodOrConstructors, context, name);
            if (methodOrConstructor == null) {
                throw new RuntimeException("unresolved method " + name + " for " + argsTypes);
            }
            return methodOrConstructor.method;
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Optional<CtMethod> findMethodAmongActualParams(String name, List<ActualParam> argsTypes, SymbolResolver resolver, boolean staticContext, List<CtMethod> methods, Node context) {
        try {
            List<MethodOrConstructor> methodOrConstructors = methods.stream()
                    .filter((m) -> Modifier.isStatic(m.getModifiers()) == staticContext)
                    .filter((m) -> m.getName().equals(name))
                    .map((m) -> new MethodOrConstructor(m)).collect(Collectors.toList());
            MethodOrConstructor methodOrConstructor = findMethodAmongActualParams(argsTypes, resolver, methodOrConstructors, context, name);
            if (methodOrConstructor == null) {
                return Optional.empty();
            } else {
                return Optional.of(methodOrConstructor.method);
            }
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static MethodOrConstructor findMethodAmong(List<JvmType> argsTypes, SymbolResolver resolver, List<MethodOrConstructor> methods, Node context, String desc) throws NotFoundException {
        List<MethodOrConstructor> suitableMethods = new ArrayList<>();
        for (MethodOrConstructor method : methods) {
            if (method.getParameterCount() == argsTypes.size()) {
                boolean match = true;
                for (int i = 0; i < argsTypes.size(); i++) {
                    TypeUsage actualType = argsTypes.get(i).toTypeUsage();
                    TypeUsage formalType = JavassistTypeDefinitionFactory.toTypeUsage(method.getParameterType(i));
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
            return findMostSpecific(suitableMethods, new AmbiguousCallException(context, desc, argsTypes), argsTypes, resolver);
        }
    }

    private static MethodOrConstructor findMethodAmongActualParams(List<ActualParam> argsTypes, SymbolResolver resolver, List<MethodOrConstructor> methods, Node context, String desc) throws NotFoundException {
        // TODO reorder params considering name
        List<MethodOrConstructor> suitableMethods = new ArrayList<>();
        for (MethodOrConstructor method : methods) {
            if (method.getParameterCount() == argsTypes.size()) {
                boolean match = true;
                for (int i = 0; i < argsTypes.size(); i++) {
                    TypeUsage actualType = argsTypes.get(i).getValue().calcType(resolver);
                    TypeUsage formalType = JavassistTypeDefinitionFactory.toTypeUsage(method.getParameterType(i));
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
            return findMostSpecific(suitableMethods,
                    new AmbiguousCallException(context, argsTypes, desc),
                    argsTypes.stream().map((ap)->ap.getValue().calcType(resolver).jvmType(resolver)).collect(Collectors.toList()),
                    resolver);
        }
    }

    private static MethodOrConstructor findMostSpecific(List<MethodOrConstructor> methods,
                                                        AmbiguousCallException exceptionToThrow,
                                                        List<JvmType> argsTypes,
                                                        SymbolResolver resolver) throws NotFoundException {
        MethodOrConstructor winningMethod = methods.get(0);
        for (MethodOrConstructor other : methods.subList(1, methods.size())) {
            if (isTheFirstMoreSpecific(winningMethod, other, argsTypes, resolver)) {
            } else if (isTheFirstMoreSpecific(other, winningMethod, argsTypes, resolver)) {
                winningMethod = other;
            } else if (!isTheFirstMoreSpecific(winningMethod, other, argsTypes, resolver)) {
                // neither is more specific
                throw exceptionToThrow;
            }
        }
        return winningMethod;
    }

    private static boolean isTheFirstMoreSpecific(MethodOrConstructor first, MethodOrConstructor second,
                                                  List<JvmType> argsTypes,
                                                  SymbolResolver resolver) throws NotFoundException {
        boolean atLeastOneParamIsMoreSpecific = false;
        if (first.getParameterCount() != second.getParameterCount()) {
            throw new IllegalArgumentException();
        }
        for (int i=0;i<first.getParameterCount();i++){
            CtClass paramFirst = first.getParameterType(i);
            CtClass paramSecond = second.getParameterType(i);
            if (isTheFirstMoreSpecific(paramFirst, paramSecond, argsTypes.get(i), resolver)) {
                atLeastOneParamIsMoreSpecific = true;
            } else if (isTheFirstMoreSpecific(paramSecond, paramFirst, argsTypes.get(i), resolver)) {
                return false;
            }
        }

        return atLeastOneParamIsMoreSpecific;
    }

    private static boolean isTheFirstMoreSpecific(CtClass firstType, CtClass secondType, JvmType targetType, SymbolResolver resolver) {
        boolean firstIsPrimitive = firstType.isPrimitive();
        boolean secondIsPrimitive = secondType.isPrimitive();
        boolean targetTypeIsPrimitive = targetType.isPrimitive();

        // it is a match or a primitive promotion
        if (targetTypeIsPrimitive && firstIsPrimitive && !secondIsPrimitive) {
            return true;
        }
        if (targetTypeIsPrimitive && !firstIsPrimitive && secondIsPrimitive) {
            return false;
        }

        if (firstType.isPrimitive() || firstType.isArray()) {
            return false;
        }
        if (secondType.isPrimitive() || secondType.isArray()) {
            return false;
        }
        // TODO consider generic parameters?
        JavassistTypeDefinition firstDef = new JavassistTypeDefinition(firstType);
        JavassistTypeDefinition secondDef = new JavassistTypeDefinition(secondType);
        TypeUsage firstTypeUsage = new ReferenceTypeUsage(firstDef);
        TypeUsage secondTypeUsage = new ReferenceTypeUsage(secondDef);
        return firstTypeUsage.canBeAssignedTo(secondTypeUsage, resolver) && !secondTypeUsage.canBeAssignedTo(firstTypeUsage, resolver);
    }

}
