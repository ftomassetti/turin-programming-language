package me.tomassetti.turin.parser.analysis;

import me.tomassetti.turin.jvm.JvmMethodDefinition;
import me.tomassetti.turin.parser.ast.FormalParameter;

import java.util.List;

public class InternalMethodDefinition extends InternalInvokableDefinition {

    private String methodName;
    private JvmMethodDefinition jvmMethodDefinition;

    public InternalMethodDefinition(String methodName, List<FormalParameter> formalParameters, JvmMethodDefinition jvmMethodDefinition) {
        super(formalParameters);
        this.methodName = methodName;
        this.jvmMethodDefinition = jvmMethodDefinition;
    }

    public String getMethodName() {
        return methodName;
    }

    public JvmMethodDefinition getJvmMethodDefinition() {
        return jvmMethodDefinition;
    }
}
