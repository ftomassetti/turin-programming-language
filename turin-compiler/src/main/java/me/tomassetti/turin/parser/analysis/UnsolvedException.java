package me.tomassetti.turin.parser.analysis;

public abstract class UnsolvedException extends RuntimeException {

    public UnsolvedException(String message) {
        super(message);
    }

}
