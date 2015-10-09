package me.tomassetti.turin.parser.analysis.exceptions;

public abstract class UnsolvedException extends RuntimeException {

    public UnsolvedException(String message) {
        super(message);
    }

}
