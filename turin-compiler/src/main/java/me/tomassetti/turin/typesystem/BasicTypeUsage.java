package me.tomassetti.turin.typesystem;

import com.google.common.collect.ImmutableList;
import me.tomassetti.jvm.JvmMethodDefinition;
import me.tomassetti.jvm.JvmType;
import me.tomassetti.turin.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;
import me.tomassetti.turin.symbols.Symbol;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * NOTE: Being a Node we could need to have separate instances for each occurrence, so that each one can have a proper
 *       parent.
 */
public class BasicTypeUsage implements TypeUsage {

    public static BasicTypeUsage UBYTE = new BasicTypeUsage("ubyte", PrimitiveTypeUsage.BYTE);
    public static BasicTypeUsage USHORT = new BasicTypeUsage("ushort", PrimitiveTypeUsage.SHORT);
    public static BasicTypeUsage UINT = new BasicTypeUsage("uint", PrimitiveTypeUsage.INT);
    public static BasicTypeUsage ULONG = new BasicTypeUsage("ulong", PrimitiveTypeUsage.LONG);
    public static BasicTypeUsage UFLOAT = new BasicTypeUsage("ufloat", PrimitiveTypeUsage.FLOAT);
    public static BasicTypeUsage UDOUBLE = new BasicTypeUsage("udouble", PrimitiveTypeUsage.DOUBLE);

    private static ImmutableList<BasicTypeUsage> BASIC_TYPES = ImmutableList.of(UBYTE, USHORT, UINT, ULONG, UFLOAT, UDOUBLE);

    private String name;
    private PrimitiveTypeUsage correspondingPrimitiveTypeUsage;

    private BasicTypeUsage(String name, PrimitiveTypeUsage correspondingPrimitiveTypeUsage) {
        this.name = name;
        this.correspondingPrimitiveTypeUsage = correspondingPrimitiveTypeUsage;
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
    public <T extends TypeUsage> TypeUsage replaceTypeVariables(Map<String, T> typeParams) {
        return null;
    }

    @Override
    public boolean sameType(TypeUsage other) {
        return other == this;
    }

    @Override
    public JvmType jvmType() {
        return correspondingPrimitiveTypeUsage.jvmType();
    }

    @Override
    public boolean canBeAssignedTo(TypeUsage type) {
        return false;
    }

    @Override
    public Symbol getInstanceField(String fieldName, Symbol instance) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return "BasicTypeUsage{" +
                "name='" + name + '\'' +
                ", correspondingPrimitiveTypeUsage=" + correspondingPrimitiveTypeUsage +
                '}';
    }

    public static Optional<BasicTypeUsage> findByName(String typeName) {
        for (BasicTypeUsage basicTypeUsage : BASIC_TYPES) {
            if (basicTypeUsage.name.equals(typeName)) {
                return Optional.of(basicTypeUsage);
            }
        }
        return Optional.empty();
    }

    public static BasicTypeUsage getByName(String typeName) {
        for (BasicTypeUsage basicTypeUsage : BASIC_TYPES) {
            if (basicTypeUsage.name.equals(typeName)) {
                return basicTypeUsage;
            }
        }
        throw new IllegalArgumentException(typeName);
    }

}
