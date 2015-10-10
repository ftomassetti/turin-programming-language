package me.tomassetti.java.symbol_solver.symbols_definitions;

import me.tomassetti.java.symbol_solver.FormalParameter;
import me.tomassetti.java.symbol_solver.type_usage.JavaTypeUsage;
import me.tomassetti.jvm.JvmMethodDefinition;
import java.util.List;

public class InternalMethodDefinition extends InternalInvokableDefinition {

    private String methodName;
    private JvmMethodDefinition jvmMethodDefinition;
    private JavaTypeUsage returnType;

    public InternalMethodDefinition(String methodName, List<FormalParameter> formalParameters, JavaTypeUsage returnType, JvmMethodDefinition jvmMethodDefinition) {
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

    public JavaTypeUsage getReturnType() {
        return returnType;
    }
}
