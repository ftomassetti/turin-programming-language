package me.tomassetti.turin.parser;

import me.tomassetti.parser.antlr.TurinLexer;
import me.tomassetti.parser.antlr.TurinParser;
import org.antlr.v4.runtime.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Parser {

    public TurinParser.TurinFileContext produceParseTree(InputStream inputStream) throws IOException {
        CharStream charStream = new ANTLRInputStream(inputStream);
        TurinLexer l = new TurinLexer(charStream);
        TurinParser p = new TurinParser(new CommonTokenStream(l));
        p.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                throw new IllegalStateException("failed to parse at line " + line + " due to " + msg, e);
            }
        });
        TurinParser.TurinFileContext turinFileContext = p.turinFile();
        return turinFileContext;
    }

    public static void main(String[] args) throws IOException {
        File file = new File("/home/federico/repos/turin-programming-language//samples/ranma.to");
        TurinParser.TurinFileContext turinFileContext = new Parser().produceParseTree(new FileInputStream(file));
        System.out.println(turinFileContext.toStringTree());
    }

}
