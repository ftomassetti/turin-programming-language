package me.tomassetti.turin.implicit;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.compiler.UnsolvedMethodException;
import me.tomassetti.turin.parser.analysis.JvmConstructorDefinition;
import me.tomassetti.turin.parser.analysis.JvmMethodDefinition;
import me.tomassetti.turin.parser.analysis.JvmType;
import me.tomassetti.turin.parser.analysis.Resolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.TypeDefinition;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;
import me.tomassetti.turin.parser.ast.reflection.ReflectionTypeDefinitionFactory;
import me.tomassetti.turin.parser.ast.typeusage.ReferenceTypeUsage;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class BasicTypes {

    public static TypeDefinition STRING = ReflectionTypeDefinitionFactory.getInstance().getTypeDefinition(String.class);
    public static TypeDefinition UINT;

    private static ImmutableList<TypeDefinition> BASIC_TYPES;

    static {
        UINT = new TypeDefinition("UInt") {
            @Override
            public Iterable<Node> getChildren() {
                return Collections.emptyList();
            }

            @Override
            public JvmType jvmType() {
                return new JvmType("I");
            }

            @Override
            public JvmMethodDefinition findMethodFor(String name, List<JvmType> argsTypes, Resolver resolver, boolean staticContext) {
                throw new UnsolvedMethodException(this, name, argsTypes, staticContext);
            }

            @Override
            public JvmConstructorDefinition resolveConstructorCall(Resolver resolver, List<ActualParam> actualParams) {
                throw new UnsupportedOperationException();
            }

            @Override
            public TypeUsage getField(String fieldName, boolean staticContext) {
                throw new IllegalArgumentException(fieldName);
            }

            @Override
            public List<ReferenceTypeUsage> getAllAncestors(Resolver resolver) {
                return Collections.emptyList();
            }

            @Override
            public String getQualifiedName() {
                return "turin.UInt";
            }
        };
        BASIC_TYPES = ImmutableList.of(STRING, UINT);
    }

    public static Optional<TypeDefinition> getBasicType(String name) {
        return BASIC_TYPES.stream().filter((tp)->tp.getName().equals(name)).findFirst();
    }
}
