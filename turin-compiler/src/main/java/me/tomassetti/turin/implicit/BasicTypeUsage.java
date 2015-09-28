package me.tomassetti.turin.implicit;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.jvm.JvmType;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.typeusage.PrimitiveTypeUsage;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

import java.util.Collections;
import java.util.Optional;

/**
 * NOTE: Being a Node we could need to have separate instances for each occurrence, so that each one can have a proper
 *       parent.
 */
public class BasicTypeUsage extends TypeUsage {

    public static BasicTypeUsage UINT = new BasicTypeUsage("uint", PrimitiveTypeUsage.INT);

    private static ImmutableList<BasicTypeUsage> BASIC_TYPES = ImmutableList.of(UINT);

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
        return correspondingPrimitiveTypeUsage;
    }

    @Override
    public boolean isMethodOverloaded(SymbolResolver resolver, String methodName) {
        return false;
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
