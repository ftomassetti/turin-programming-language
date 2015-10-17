package me.tomassetti.turin.parser.ast.typeusage;

import com.google.common.collect.ImmutableList;
import me.tomassetti.jvm.JvmType;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.typesystem.PrimitiveTypeUsage;
import me.tomassetti.turin.typesystem.TypeUsage;

import java.util.Collections;
import java.util.Optional;

/**
 * NOTE: Being a Node we could need to have separate instances for each occurrence, so that each one can have a proper
 *       parent.
 */
public class BasicTypeUsageNode extends TypeUsageNode {

    public static BasicTypeUsageNode UBYTE = new BasicTypeUsageNode("ubyte", PrimitiveTypeUsage.BYTE);
    public static BasicTypeUsageNode USHORT = new BasicTypeUsageNode("ushort", PrimitiveTypeUsage.SHORT);
    public static BasicTypeUsageNode UINT = new BasicTypeUsageNode("uint", PrimitiveTypeUsage.INT);
    public static BasicTypeUsageNode ULONG = new BasicTypeUsageNode("ulong", PrimitiveTypeUsage.LONG);
    public static BasicTypeUsageNode UFLOAT = new BasicTypeUsageNode("ufloat", PrimitiveTypeUsage.FLOAT);
    public static BasicTypeUsageNode UDOUBLE = new BasicTypeUsageNode("udouble", PrimitiveTypeUsage.DOUBLE);

    private static ImmutableList<BasicTypeUsageNode> BASIC_TYPES = ImmutableList.of(UBYTE, USHORT, UINT, ULONG, UFLOAT, UDOUBLE);

    private String name;
    private PrimitiveTypeUsage correspondingPrimitiveTypeUsage;

    private BasicTypeUsageNode(String name, PrimitiveTypeUsage correspondingPrimitiveTypeUsage) {
        this.name = name;
        this.correspondingPrimitiveTypeUsage = correspondingPrimitiveTypeUsage;
    }

    @Override
    public TypeUsageNode copy() {
        return this;
    }

    @Override
    public boolean isPrimitive() {
        return true;
    }

    @Override
    public PrimitiveTypeUsage asPrimitiveTypeUsage() {
        return correspondingPrimitiveTypeUsage.asPrimitiveTypeUsage();
    }

    @Override
    public boolean isMethodOverloaded(SymbolResolver resolver, String methodName) {
        return false;
    }

    @Override
    public boolean sameType(TypeUsage other, SymbolResolver resolver) {
        return other == this;
    }

    @Override
    public JvmType jvmType(SymbolResolver resolver) {
        return correspondingPrimitiveTypeUsage.jvmType(resolver);
    }

    @Override
    public Iterable<Node> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return "BasicTypeUsage{" +
                "name='" + name + '\'' +
                ", correspondingPrimitiveTypeUsage=" + correspondingPrimitiveTypeUsage +
                '}';
    }

    public static Optional<BasicTypeUsageNode> findByName(String typeName) {
        for (BasicTypeUsageNode basicTypeUsage : BASIC_TYPES) {
            if (basicTypeUsage.name.equals(typeName)) {
                return Optional.of(basicTypeUsage);
            }
        }
        return Optional.empty();
    }

    public static BasicTypeUsageNode getByName(String typeName) {
        for (BasicTypeUsageNode basicTypeUsage : BASIC_TYPES) {
            if (basicTypeUsage.name.equals(typeName)) {
                return basicTypeUsage;
            }
        }
        throw new IllegalArgumentException(typeName);
    }

}
