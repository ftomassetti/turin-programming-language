package me.tomassetti.turin.parser.ast.reflection;

import me.tomassetti.turin.parser.analysis.JvmMethodDefinition;
import me.tomassetti.turin.parser.analysis.JvmType;
import me.tomassetti.turin.parser.analysis.Resolver;
import me.tomassetti.turin.parser.ast.TurinTypeDefinition;
import me.tomassetti.turin.parser.ast.TypeUsage;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

class ReflectionBasedTypeDefinition extends TurinTypeDefinition {

    private Class<?> clazz;

    ReflectionBasedTypeDefinition(String name) {
        super(name);
        try {
            this.clazz = this.getClass().getClassLoader().loadClass(name);
        } catch (ClassNotFoundException e){
            throw new RuntimeException(e);
        }
    }

    private ReflectionBasedTypeDefinition(Class<?> clazz) {
        super(clazz.getCanonicalName());
        this.clazz = clazz;
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
            throw new UnsupportedOperationException(suitableMethods.toString());
        }
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
    public List<TurinTypeDefinition> getAllAncestors(Resolver resolver) {
        List<TurinTypeDefinition> ancestors = new ArrayList<>();
        if (clazz.getSuperclass() != null) {
            TurinTypeDefinition superTypeDefinition = new ReflectionBasedTypeDefinition(clazz.getSuperclass());
            ancestors.addAll(superTypeDefinition.getAllAncestors(resolver));
        }
        for (Class<?> interfaze : clazz.getInterfaces()) {
            TurinTypeDefinition superTypeDefinition = new ReflectionBasedTypeDefinition(interfaze);
            ancestors.addAll(superTypeDefinition.getAllAncestors(resolver));
        }
        return ancestors;
    }
}
