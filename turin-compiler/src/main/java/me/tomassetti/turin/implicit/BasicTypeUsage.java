package me.tomassetti.turin.implicit;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.jvm.JvmType;
import me.tomassetti.turin.parser.analysis.resolvers.Resolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

import java.util.Collections;
import java.util.Optional;

/**
 * NOTE: Being a Node we could need to have separate instances for each occurrence, so that each one can have a proper
 *       parent.
 */
public class BasicTypeUsage extends TypeUsage {

    public static BasicTypeUsage UINT = new BasicTypeUsage("UInt", "I");

    private static ImmutableList<BasicTypeUsage> BASIC_TYPES = ImmutableList.of(UINT);

    private String name;
    private String jvmType;

    private BasicTypeUsage(String name, String jvmType) {
        this.name = name;
        this.jvmType = jvmType;
    }

    @Override
    public boolean isPrimitive() {
        return true;
    }

    @Override
    public JvmType jvmType(Resolver resolver) {
        return new JvmType(jvmType);
    }

    @Override
    public Iterable<Node> getChildren() {
        return Collections.emptyList();
    }

    public static Optional<BasicTypeUsage> getBasicType(String typeName) {
        for (BasicTypeUsage basicTypeUsage : BASIC_TYPES) {
            if (basicTypeUsage.name.equals(typeName)) {
                return Optional.of(basicTypeUsage);
            }
        }
        return Optional.empty();
    }
}
