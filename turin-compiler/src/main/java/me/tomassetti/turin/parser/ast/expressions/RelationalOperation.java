package me.tomassetti.turin.parser.ast.expressions;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.typeusage.PrimitiveTypeUsage;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsageNode;

public class RelationalOperation extends Expression {
    private Expression left;
    private Expression right;
    private Operator operator;

    @Override
    public String toString() {
        return "RelationalOperation{" +
                "left=" + left +
                ", right=" + right +
                ", operator=" + operator +
                '}';
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of(left, right);
    }

    @Override
    public TypeUsageNode calcType(SymbolResolver resolver) {
        return PrimitiveTypeUsage.BOOLEAN;
    }

    public Expression getLeft() {
        return left;
    }

    public Expression getRight() {
        return right;
    }

    public Operator getOperator() {
        return operator;
    }

    public enum Operator {
        EQUAL("=="),
        DIFFERENT("!="),
        LESS("<"),
        LESSEQ("<="),
        MORE(">"),
        MOREEQ(">=");

        private String symbol;

        private Operator(String symbol) {
            this.symbol = symbol;
        }

        public static Operator fromSymbol(String symbol) {
            for (Operator operator : Operator.values()) {
                if (operator.symbol.equals(symbol)) {
                    return operator;
                }
            }
            throw new IllegalArgumentException(symbol);
        }
    }

    public RelationalOperation(Operator operator, Expression left, Expression right) {
        this.operator = operator;
        this.left = left;
        this.left.setParent(this);
        this.right = right;
        this.right.setParent(this);
    }

}
