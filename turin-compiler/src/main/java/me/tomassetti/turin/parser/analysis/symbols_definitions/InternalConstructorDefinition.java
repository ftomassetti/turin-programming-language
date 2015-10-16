package me.tomassetti.turin.parser.analysis.symbols_definitions;

import me.tomassetti.jvm.JvmConstructorDefinition;
import me.tomassetti.turin.parser.ast.FormalParameterNode;

import java.util.List;

public class InternalConstructorDefinition extends InternalInvokableDefinition {

    private JvmConstructorDefinition jvmConstructorDefinition;

    public JvmConstructorDefinition getJvmConstructorDefinition() {
        return jvmConstructorDefinition;
    }

    public InternalConstructorDefinition(List<FormalParameterNode> formalParameters, JvmConstructorDefinition jvmConstructorDefinition) {
        super(formalParameters);
        this.jvmConstructorDefinition = jvmConstructorDefinition;
    }

}
