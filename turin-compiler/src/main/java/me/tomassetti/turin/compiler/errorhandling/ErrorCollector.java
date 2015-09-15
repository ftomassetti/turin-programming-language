package me.tomassetti.turin.compiler.errorhandling;

import me.tomassetti.turin.parser.ast.Position;

public interface ErrorCollector {

    void recordSemanticError(Position position, String description);

}
