package me.tomassetti.turin.parser.ast.virtual;

import me.tomassetti.turin.symbols.Symbol;
import me.tomassetti.turin.typesystem.PrimitiveTypeUsage;
import me.tomassetti.turin.typesystem.TypeUsage;

public class ArrayLength implements Symbol {

    private Symbol array;

    public ArrayLength(Symbol array) {
        // non setting the parent of array on purpose
        this.array = array;
    }

    @Override
    public TypeUsage calcType() {
        return PrimitiveTypeUsage.INT;
    }
}
