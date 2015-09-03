package me.tomassetti.turin.parser.ast;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.analysis.JvmType;
import me.tomassetti.turin.parser.analysis.Resolver;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Usage of a primitive type.
 */
public class PrimitiveTypeUsage extends TypeUsage {

    private String name;
    private JvmType jvmType;

    public static PrimitiveTypeUsage BOOLEAN = new PrimitiveTypeUsage("boolean", new JvmType("Z"));
    public static PrimitiveTypeUsage CHAR = new PrimitiveTypeUsage("char",  new JvmType("C"));
    public static PrimitiveTypeUsage BYTE = new PrimitiveTypeUsage("byte",  new JvmType("B"));
    public static PrimitiveTypeUsage SHORT = new PrimitiveTypeUsage("short",  new JvmType("S"));
    public static PrimitiveTypeUsage INT = new PrimitiveTypeUsage("int",  new JvmType("I"));
    public static PrimitiveTypeUsage LONG = new PrimitiveTypeUsage("long",  new JvmType("J"));
    public static PrimitiveTypeUsage FLOAT = new PrimitiveTypeUsage("float",  new JvmType("F"));
    public static PrimitiveTypeUsage DOUBLE = new PrimitiveTypeUsage("double",  new JvmType("D"));
    public static List<PrimitiveTypeUsage> ALL = ImmutableList.of(BOOLEAN, CHAR, BYTE, SHORT, INT, LONG, FLOAT, DOUBLE);

    public static Optional<PrimitiveTypeUsage> findByJvmType(JvmType jvmType) {
        for (PrimitiveTypeUsage primitiveTypeUsage : ALL) {
            if (primitiveTypeUsage.equals(jvmType)) {
                return Optional.of(primitiveTypeUsage);
            }
        }
        return Optional.empty();
    }

    private PrimitiveTypeUsage(String name, JvmType jvmType) {
        this.name = name;
        this.jvmType = jvmType;
    }

    @Override
    public JvmType jvmType(Resolver resolver) {
        return jvmType;
    }

    @Override
    public Iterable<Node> getChildren() {
        return Collections.emptyList();
    }

    public static TypeUsage getByName(String name) {
        for (PrimitiveTypeUsage primitiveTypeUsage : ALL) {
            if (primitiveTypeUsage.name.equals(name)) {
                return primitiveTypeUsage;
            }
        }
        throw new IllegalArgumentException(name);
    }
}
