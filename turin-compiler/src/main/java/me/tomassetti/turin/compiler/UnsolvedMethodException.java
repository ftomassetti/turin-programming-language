package me.tomassetti.turin.compiler;

import me.tomassetti.turin.parser.analysis.JvmType;
import me.tomassetti.turin.parser.ast.TypeDefinition;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;

import java.util.List;

public class UnsolvedMethodException extends RuntimeException {

    public UnsolvedMethodException(TypeDefinition typeDefinition, String name, List<JvmType> argsTypes, boolean staticContext) {

    }
}
