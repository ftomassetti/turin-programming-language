package me.tomassetti.turin.ast;

import com.google.common.collect.ImmutableList;

public class ReferenceTypeUsage extends TypeUsage {

    private String name;

    public ReferenceTypeUsage(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReferenceTypeUsage that = (ReferenceTypeUsage) o;

        if (!name.equals(that.name)) return false;

        return true;
    }

    @Override
    public String toString() {
        return "ReferenceTypeUsage{" +
                "name='" + name + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of();

    }
}
