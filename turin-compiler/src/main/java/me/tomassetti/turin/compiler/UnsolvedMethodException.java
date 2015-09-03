package me.tomassetti.turin.compiler;

import me.tomassetti.turin.jvm.JvmType;
import me.tomassetti.turin.parser.ast.TypeDefinition;

import java.util.List;

public class UnsolvedMethodException extends RuntimeException {

    public UnsolvedMethodException(TypeDefinition typeDefinition, String name, List<JvmType> argsTypes, boolean staticContext) {

    }
}
