package me.tomassetti.turin.parser.analysis;

import me.tomassetti.turin.jvm.JvmType;
import me.tomassetti.turin.parser.ast.TypeDefinition;
import me.tomassetti.turin.parser.ast.expressions.FunctionCall;

import java.util.List;

public class UnsolvedMethodException extends UnsolvedException {

    private TypeDefinition typeDefinition;
    private String methodName;
    private List<JvmType> argsTypes;
    private boolean staticContext;

    public UnsolvedMethodException(TypeDefinition typeDefinition, String methodName, List<JvmType> argsTypes, boolean staticContext) {
        super("Unsolved method " + methodName + " for " + typeDefinition + " with params " + argsTypes);
        this.typeDefinition = typeDefinition;
        this.methodName = methodName;
        this.argsTypes = argsTypes;
        this.staticContext = staticContext;
    }

    public UnsolvedMethodException(FunctionCall functionCall) {
        super("Unsolved function call " + functionCall.toString());
    }

    public UnsolvedMethodException(String name, List<JvmType> argsTypes, boolean staticContext) {
        super("Unsolved method call on " + name + " with params " + argsTypes);
    }
}
