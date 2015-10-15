package me.tomassetti.turin.parser.analysis.symbols_definitions;

import me.tomassetti.jvm.JvmMethodDefinition;
import me.tomassetti.turin.parser.ast.FormalParameter;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsageNode;

import java.util.List;

public class InternalMethodDefinition extends InternalInvokableDefinition {

    private String methodName;
    private JvmMethodDefinition jvmMethodDefinition;
    private TypeUsageNode returnType;

    public InternalMethodDefinition(String methodName, List<FormalParameter> formalParameters, TypeUsageNode returnType, JvmMethodDefinition jvmMethodDefinition) {
        super(formalParameters);
        this.methodName = methodName;
        this.returnType = returnType;
        this.jvmMethodDefinition = jvmMethodDefinition;
    }

    public String getMethodName() {
        return methodName;
    }

    public JvmMethodDefinition getJvmMethodDefinition() {
        return jvmMethodDefinition;
    }

    public TypeUsageNode getReturnType() {
        return returnType;
    }
}
