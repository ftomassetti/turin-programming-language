package me.tomassetti.turin.parser.ast.reflection;

import me.tomassetti.turin.compiler.AmbiguousCallException;
import me.tomassetti.turin.parser.analysis.JvmConstructorDefinition;
import me.tomassetti.turin.parser.analysis.JvmMethodDefinition;
import me.tomassetti.turin.parser.analysis.JvmType;
import me.tomassetti.turin.parser.analysis.Resolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.TypeDefinition;
import me.tomassetti.turin.parser.ast.typeusage.ReferenceTypeUsage;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class ReflectionBasedTypeDefinition extends TypeDefinition {

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
        List<Method> suitableMethods = new ArrayList<>();
        for (Method method : clazz.getMethods()) {
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
            return ReflectionTypeDefinitionFactory.toMethodDefinition(findMostSpecific(suitableMethods, new AmbiguousCallException(this, name, argsTypes), resolver));
        }
    }

    private Method findMostSpecific(List<Method> methods, AmbiguousCallException exceptionToThrow, Resolver resolver) {
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

    private boolean isTheFirstMoreSpecific(Method first, Method second, Resolver resolver) {
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

    private boolean isTheFirstMoreSpecific(Class<?> firstType, Class<?> secondType, Resolver resolver) {
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

    @Override
    public JvmConstructorDefinition resolveConstructorCall(Resolver resolver, List<ActualParam> actualParams) {
        throw new UnsupportedOperationException();
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
            TypeDefinition superTypeDefinition = new ReflectionBasedTypeDefinition(clazz.getSuperclass());
            ancestors.add(new ReferenceTypeUsage(superTypeDefinition));
            ancestors.addAll(superTypeDefinition.getAllAncestors(resolver));
        }
        for (Class<?> interfaze : clazz.getInterfaces()) {
            TypeDefinition superTypeDefinition = new ReflectionBasedTypeDefinition(interfaze);
            ancestors.add(new ReferenceTypeUsage(superTypeDefinition));
            ancestors.addAll(superTypeDefinition.getAllAncestors(resolver));
        }
        return ancestors;
    }

    @Override
    public Iterable<Node> getChildren() {
        return Collections.emptyList();
    }
}
