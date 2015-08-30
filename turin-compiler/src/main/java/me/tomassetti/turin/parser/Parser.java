package me.tomassetti.turin.parser;

import me.tomassetti.turin.parser.ast.TurinFile;

import java.io.IOException;
import java.io.InputStream;

public class Parser {

    private InternalParser internalParser = new InternalParser();

    public TurinFile parse(InputStream inputStream) throws IOException {
        return new ParseTreeToAst().toAst(internalParser.produceParseTree(inputStream));
    }
}
