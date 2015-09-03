package me.tomassetti.turin.parser.ast;

public class UnsolvedTypeException extends RuntimeException {
    private String name;

    public UnsolvedTypeException(String name) {
        this.name = name;
    }
}
