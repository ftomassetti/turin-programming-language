package me.tomassetti.turin.parser.ast;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.jvm.JvmNameUtils;

public class QualifiedName extends Node {

    private QualifiedName base;
    private String name;

    public QualifiedName(QualifiedName base, String name) {
        if (!JvmNameUtils.isValidQualifiedName(name)) {
            throw new IllegalArgumentException();
        }
        this.base = base;
        this.name = name;
    }

    public QualifiedName(String name) {
        if (!JvmNameUtils.isValidQualifiedName(name)) {
            throw new IllegalArgumentException();
        }
        this.name = name;
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of(base);
    }

    public String qualifiedName() {
        if (base == null) {
            return name;
        } else {
            return base + "." + name;
        }
    }

    @Override
    public String toString() {
        return qualifiedName();
    }
}
