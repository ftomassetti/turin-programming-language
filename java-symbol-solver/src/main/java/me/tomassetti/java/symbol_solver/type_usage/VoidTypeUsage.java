package me.tomassetti.java.symbol_solver.type_usage;

import me.tomassetti.java.symbol_solver.JavaTypeResolver;
import me.tomassetti.jvm.JvmType;

public class VoidTypeUsage extends JavaTypeUsage {

    @Override
    public boolean isVoid() {
        return true;
    }

    @Override
    public JvmType jvmType(JavaTypeResolver resolver) {
        return JvmType.VOID;
    }

}
