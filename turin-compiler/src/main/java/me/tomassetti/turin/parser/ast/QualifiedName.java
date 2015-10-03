package me.tomassetti.turin.parser.ast;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.jvm.JvmNameUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class QualifiedName extends Node {

    private QualifiedName base;
    private String name;

    public static QualifiedName create(List<String> base) {
        if (base.isEmpty()) {
            throw new IllegalArgumentException();
        } else if (base.size() == 1) {
            return new QualifiedName(base.get(0));
        } else {
            return new QualifiedName(QualifiedName.create(base.subList(0, base.size() - 1)), base.get(base.size() - 1));
        }
    }

    public QualifiedName getBase() {
        return base;
    }

    public String getName() {
        return name;
    }

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

    public boolean isSimpleName() {
        return base == null;
    }

    @Override
    public Iterable<Node> getChildren() {
        if (base == null) {
            return Collections.emptyList();
        } else {
            return ImmutableList.of(base);
        }
    }

    public String qualifiedName() {
        if (base == null) {
            return name;
        } else {
            return base + "." + name;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QualifiedName that = (QualifiedName) o;

        if (base != null ? !base.equals(that.base) : that.base != null) return false;
        if (!name.equals(that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = base != null ? base.hashCode() : 0;
        result = 31 * result + name.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return qualifiedName();
    }

    public String firstSegment() {
        if (isSimpleName()) {
            return name;
        } else {
            return base.firstSegment();
        }
    }

    public QualifiedName rest() {
        if (isSimpleName()) {
            throw new UnsupportedOperationException();
        }
        if (base.isSimpleName()) {
            return new QualifiedName(name);
        } else {
            return new QualifiedName(base.rest(), name);
        }
    }

    public static QualifiedName create(String path) {
        return create(Arrays.stream(path.split("\\.")).collect(Collectors.toList()));
    }
}
