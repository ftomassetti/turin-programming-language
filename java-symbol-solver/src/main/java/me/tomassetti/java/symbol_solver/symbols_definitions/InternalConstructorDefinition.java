package me.tomassetti.java.symbol_solver.symbols_definitions;

import me.tomassetti.java.symbol_solver.FormalParameter;
import me.tomassetti.jvm.JvmConstructorDefinition;

import java.util.List;

public class InternalConstructorDefinition extends InternalInvokableDefinition {

    private JvmConstructorDefinition jvmConstructorDefinition;

    public JvmConstructorDefinition getJvmConstructorDefinition() {
        return jvmConstructorDefinition;
    }

    public InternalConstructorDefinition(List<FormalParameter> formalParameters, JvmConstructorDefinition jvmConstructorDefinition) {
        super(formalParameters);
        this.jvmConstructorDefinition = jvmConstructorDefinition;
    }

}
