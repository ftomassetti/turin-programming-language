package me.tomassetti.turin.compiler;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;
import me.tomassetti.turin.parser.ast.expressions.Creation;
import me.tomassetti.turin.parser.ast.expressions.Expression;
import me.tomassetti.turin.typesystem.PrimitiveTypeUsage;
import me.tomassetti.turin.typesystem.TypeUsage;

public final class BoxUnboxing {

    private BoxUnboxing() {
        // prevent instantiation
    }

    public static Expression box(Expression value, SymbolResolver resolver) {
        TypeUsage type = value.calcType();
        if (!type.isPrimitive()) {
            throw new IllegalArgumentException("type is not primitive, cannot box this value");
        }
        PrimitiveTypeUsage typeUsage = type.asPrimitiveTypeUsage();
        if (typeUsage.isInt()) {
            Node parent = value.getParent();
            Creation creation = new Creation(Integer.class.getCanonicalName(), ImmutableList.of(new ActualParam(value)));
            creation.setParent(parent);
            return creation;
        } else if (typeUsage.isChar()) {
            Node parent = value.getParent();
            Creation creation = new Creation(Character.class.getCanonicalName(), ImmutableList.of(new ActualParam(value)));
            creation.setParent(parent);
            return creation;
        } else if (typeUsage.isBoolean()) {
            Node parent = value.getParent();
            Creation creation = new Creation(Boolean.class.getCanonicalName(), ImmutableList.of(new ActualParam(value)));
            creation.setParent(parent);
            return creation;
        } else if (typeUsage.isByte()) {
            Node parent = value.getParent();
            Creation creation = new Creation(Byte.class.getCanonicalName(), ImmutableList.of(new ActualParam(value)));
            creation.setParent(parent);
            return creation;
        } else if (typeUsage.isShort()) {
            Node parent = value.getParent();
            Creation creation = new Creation(Short.class.getCanonicalName(), ImmutableList.of(new ActualParam(value)));
            creation.setParent(parent);
            return creation;
        } else if (typeUsage.isLong()) {
            Node parent = value.getParent();
            Creation creation = new Creation(Long.class.getCanonicalName(), ImmutableList.of(new ActualParam(value)));
            creation.setParent(parent);
            return creation;
        } else if (typeUsage.isFloat()) {
            Node parent = value.getParent();
            Creation creation = new Creation(Float.class.getCanonicalName(), ImmutableList.of(new ActualParam(value)));
            creation.setParent(parent);
            return creation;
        } else if (typeUsage.isDouble()) {
            Node parent = value.getParent();
            Creation creation = new Creation(Double.class.getCanonicalName(), ImmutableList.of(new ActualParam(value)));
            creation.setParent(parent);
            return creation;
        } else {
            throw new RuntimeException("Unexpected primitive type: " + typeUsage);
        }
    }
}
