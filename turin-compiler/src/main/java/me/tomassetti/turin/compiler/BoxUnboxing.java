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
            Creation creation = new Creation("java.lang.Integer", ImmutableList.of(new ActualParam(value)));
            creation.setParent(parent);
            return creation;
        } else {
            throw new UnsupportedOperationException();
        }
    }
}
