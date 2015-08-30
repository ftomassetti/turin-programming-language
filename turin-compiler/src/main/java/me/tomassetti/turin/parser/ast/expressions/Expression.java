package me.tomassetti.turin.parser.ast.expressions;

import me.tomassetti.turin.parser.analysis.Resolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.TypeUsage;

/**
 * Created by federico on 29/08/15.
 */
public abstract class Expression extends Node {
    public abstract TypeUsage calcType(Resolver resolver);
}
