package me.tomassetti.java.symbol_solver.type_usage;

import me.tomassetti.java.symbol_solver.JavaTypeDefinition;
import me.tomassetti.java.symbol_solver.JavaTypeResolver;
import me.tomassetti.jvm.JvmNameUtils;
import me.tomassetti.jvm.JvmType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * It could represent also a reference to a Type Variable.
 */
public class ReferenceTypeUsage extends JavaTypeUsage {

    public static final ReferenceTypeUsage OBJECT = new ReferenceTypeUsage(Object.class.getCanonicalName());
    public static final ReferenceTypeUsage STRING = new ReferenceTypeUsage(String.class.getCanonicalName());
    private List<JavaTypeUsage> typeParams;
    private TypeParameterValues typeParameterValues = new TypeParameterValues();
    private String fullyQualifiedName;
    private JavaTypeDefinition cachedTypeDefinition;

    public ReferenceTypeUsage(JavaTypeDefinition typeDefinition, List<JavaTypeUsage> typeParams) {
        this(typeDefinition.getQualifiedName());
        this.typeParams = typeParams;
        this.cachedTypeDefinition = typeDefinition;
    }

    public ReferenceTypeUsage(String fullyQualifiedName) {
        if (JvmNameUtils.isPrimitiveTypeName(fullyQualifiedName)) {
            throw new IllegalArgumentException(fullyQualifiedName);
        }
        if (!JvmNameUtils.isValidQualifiedName(fullyQualifiedName)) {
            throw new IllegalArgumentException(fullyQualifiedName);
        }
        this.fullyQualifiedName = fullyQualifiedName;
        this.typeParams = Collections.emptyList();
    }

    public ReferenceTypeUsage(JavaTypeDefinition td) {
        this(td.getQualifiedName());
        this.cachedTypeDefinition = td;
    }

    public boolean isInterface(JavaTypeResolver resolver) {
        return getTypeDefinition(resolver).isInterface();
    }

    public boolean isClass(JavaTypeResolver resolver) {
        return getTypeDefinition(resolver).isClass();
    }

    public boolean isEnum(JavaTypeResolver resolver) {
        throw new UnsupportedOperationException();
    }

    public boolean isTypeVariable(JavaTypeResolver resolver) {
        throw new UnsupportedOperationException();
    }

    public TypeParameterValues getTypeParameterValues() {
        return typeParameterValues;
    }


    public JavaTypeDefinition getTypeDefinition(JavaTypeResolver resolver) {
        if (cachedTypeDefinition != null) {
            return cachedTypeDefinition;
        }
        JavaTypeDefinition typeDefinition = resolver.resolveAbsoluteTypeName(this.getQualifiedName(resolver)).get();
        return typeDefinition;
    }


    @Override
    public JvmType jvmType(JavaTypeResolver resolver) {
        return getTypeDefinition(resolver).jvmType(resolver);
    }

    public String getQualifiedName(JavaTypeResolver resolver) {
        return fullyQualifiedName;
    }

    @Override
    public boolean isReferenceTypeUsage() {
        return true;
    }

    @Override
    public ReferenceTypeUsage asReferenceTypeUsage() {
        return this;
    }

    public List<ReferenceTypeUsage> getAllAncestors(JavaTypeResolver resolver) {
        // TODO perhaps some generic type substitution needs to be done
        return getTypeDefinition(resolver).getAllAncestors(resolver);
    }

    public class TypeParameterValues {
        private List<JavaTypeUsage> usages = new ArrayList<>();
        private List<String> names = new ArrayList<>();

        public void add(String name, JavaTypeUsage typeUsage) {
            names.add(name);
            usages.add(typeUsage);
        }

        public List<JavaTypeUsage> getInOrder() {
            return usages;
        }

        public List<String> getNamesInOrder() {
            return names;
        }

        public JavaTypeUsage getByName(String name) {
            for (int i=0; i<names.size(); i++) {
                if (names.get(i).equals(name)) {
                    return usages.get(i);
                }
            }
            throw new IllegalArgumentException(name);
        }
    }
}
