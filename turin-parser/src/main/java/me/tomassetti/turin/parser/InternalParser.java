package me.tomassetti.turin.parser;

import me.tomassetti.parser.antlr.TurinLexer;
import me.tomassetti.parser.antlr.TurinParserParser;
import org.antlr.v4.runtime.*;

import java.io.IOException;
import java.io.InputStream;

public class InternalParser {

    public TurinParserParser.TurinFileContext produceParseTree(InputStream inputStream) throws IOException {
        CharStream charStream = new ANTLRInputStream(inputStream);
        TurinLexer l = new TurinLexer(charStream);
        TurinParserParser p = new TurinParserParser(new CommonTokenStream(l));
        p.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                throw new IllegalStateException("failed to parse at line " + line + " due to " + msg, e);
            }
        });
        TurinParserParser.TurinFileContext turinFileContext = p.turinFile();
        return turinFileContext;
    }

}
