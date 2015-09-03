package me.tomassetti.turin.compiler;

import me.tomassetti.turin.parser.ast.expressions.ActualParam;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

import java.util.List;

public class UnsolvedConstructorException extends RuntimeException {
    public UnsolvedConstructorException(String type, List<ActualParam> paramList) {

    }
}
