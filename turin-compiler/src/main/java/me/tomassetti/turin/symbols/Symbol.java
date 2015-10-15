package me.tomassetti.turin.symbols;

import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

public interface Symbol {

    TypeUsage calcType(SymbolResolver resolver);
}
