package me.tomassetti.turin.implicit;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.ast.TypeDefinition;

import java.util.Optional;

/**
 * Created by federico on 29/08/15.
 */
public class BasicTypes {

    public static TypeDefinition STRING;
    public static TypeDefinition UINT;
    private static ImmutableList<TypeDefinition> BASIC_TYPES;

    static {
        STRING = new TypeDefinition("String") {
            @Override
            public String jvmType() {
                return "Ljava/lang/String;";
            }

            @Override
            public String getQualifiedName() {
                return "java.lang.String";
            }
        };
        UINT = new TypeDefinition("UInt") {
            @Override
            public String jvmType() {
                return "I";
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
