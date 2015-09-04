package me.tomassetti.turin.compiler;

import me.tomassetti.turin.jvm.JvmType;
import me.tomassetti.turin.parser.ast.TypeDefinition;

import java.util.List;

public class UnsolvedMethodException extends RuntimeException {

    private TypeDefinition typeDefinition;
    private String methodName;
    private List<JvmType> argsTypes;
    private boolean staticContext;

    public UnsolvedMethodException(TypeDefinition typeDefinition, String methodName, List<JvmType> argsTypes, boolean staticContext) {
        this.typeDefinition = typeDefinition;
        this.methodName = methodName;
        this.argsTypes = argsTypes;
        this.staticContext = staticContext;
    }
}
