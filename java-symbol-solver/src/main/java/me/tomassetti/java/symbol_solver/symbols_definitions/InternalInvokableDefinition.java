package me.tomassetti.java.symbol_solver.symbols_definitions;

import me.tomassetti.java.symbol_solver.FormalParameter;
import me.tomassetti.java.symbol_solver.JavaTypeResolver;
import me.tomassetti.jvm.JvmType;

import java.util.List;

public abstract class InternalInvokableDefinition {

    private List<FormalParameter> formalParameters;

    public InternalInvokableDefinition(List<FormalParameter> formalParameters) {
        this.formalParameters = formalParameters;
    }

    public List<FormalParameter> getFormalParameters() {
        return formalParameters;
    }

    public boolean matchJvmTypes(JavaTypeResolver resolver, List<JvmType> jvmTypes) {
        List<FormalParameter> formalParameters = getFormalParameters();
        if (formalParameters.size() != jvmTypes.size()) {
            return false;
        }
        for (int i=0;i<jvmTypes.size();i++) {
            FormalParameter formalParameter = formalParameters.get(i);
            JvmType jvmType = jvmTypes.get(i);
            if (!formalParameter.getType().jvmType(resolver).isAssignableBy(jvmType)) {
                return false;
            }
        }
        return true;
    }

}
