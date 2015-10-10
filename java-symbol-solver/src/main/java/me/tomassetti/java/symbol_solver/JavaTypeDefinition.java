package me.tomassetti.java.symbol_solver;

import me.tomassetti.java.symbol_solver.type_usage.ReferenceTypeUsage;
import me.tomassetti.jvm.JvmType;

import java.util.List;

public class JavaTypeDefinition {
    public String getQualifiedName() {
        throw new UnsupportedOperationException();
    }

    public boolean isInterface() {
        throw new UnsupportedOperationException();
    }

    public boolean isClass() {
        throw new UnsupportedOperationException();
    }

    public JvmType jvmType(JavaTypeResolver resolver) {
        throw new UnsupportedOperationException();
    }

    public List<ReferenceTypeUsage> getAllAncestors(JavaTypeResolver resolver) {
        throw new UnsupportedOperationException();
    }
}
