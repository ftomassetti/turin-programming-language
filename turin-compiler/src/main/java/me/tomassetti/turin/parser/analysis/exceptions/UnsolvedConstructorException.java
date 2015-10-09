package me.tomassetti.turin.parser.analysis.exceptions;

import me.tomassetti.turin.parser.ast.expressions.ActualParam;

import java.util.List;

public class UnsolvedConstructorException extends UnsolvedException {

    private String typeCanonicalName;
    private List<ActualParam> paramList;

    public UnsolvedConstructorException(String typeCanonicalName, List<ActualParam> paramList) {
        super("Unsolved constructor for " + typeCanonicalName + " with params " + paramList);
        this.typeCanonicalName = typeCanonicalName;
        this.paramList = paramList;
    }

    public UnsolvedConstructorException(String typeCanonicalName, List<ActualParam> paramList, String detail) {
        super("Unsolved constructor for " + typeCanonicalName + " with params " + paramList + ": " + detail);
        this.typeCanonicalName = typeCanonicalName;
        this.paramList = paramList;
    }
}
