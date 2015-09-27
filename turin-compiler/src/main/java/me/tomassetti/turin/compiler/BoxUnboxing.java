package me.tomassetti.turin.compiler;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;
import me.tomassetti.turin.parser.ast.expressions.Creation;
import me.tomassetti.turin.parser.ast.expressions.Expression;
import me.tomassetti.turin.parser.ast.typeusage.PrimitiveTypeUsage;

public class BoxUnboxing {

    public static Expression box(Expression value, SymbolResolver resolver) {
        PrimitiveTypeUsage typeUsage = value.calcType(resolver).asPrimitiveTypeUsage();
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
            throw new UnsupportedOperationException();
        }
    }
}
