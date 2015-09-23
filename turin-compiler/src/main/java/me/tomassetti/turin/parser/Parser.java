package me.tomassetti.turin.parser;

import com.google.common.collect.ImmutableList;
import me.tomassetti.parser.antlr.TurinLexer;
import me.tomassetti.turin.parser.ast.TurinFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Produce ASTs from the source code.
 */
public class Parser {

    private InternalParser internalParser = new InternalParser();

    public TurinFile parse(InputStream inputStream) throws IOException {
        return new ParseTreeToAst().toAst(internalParser.produceParseTree(inputStream));
    }

    /**
     * Accept a file or a directory. If a directory is given all the children are recursively parsed.
     * All files are parsed, irrespectively of their extension.
     */
    public List<TurinFileWithSource> parseAllIn(File file) throws IOException {
        if (file.isFile()) {
            return ImmutableList.of(new TurinFileWithSource(file, parse(new FileInputStream(file))));
        } else if (file.isDirectory()) {
            List<TurinFileWithSource> result = new ArrayList<>();
            for (File child : file.listFiles()) {
                result.addAll(parseAllIn(child));
            }
            return result;
        } else {
            throw new IllegalArgumentException("Neither a file or a directory: " + file.getPath());
        }
    }

}
