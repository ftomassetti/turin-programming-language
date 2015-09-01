package me.tomassetti.parser.antlr;

import me.tomassetti.turin.parser.InternalParser;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TurinParserTest {

    private TurinParser.TurinFileContext parse(String exampleName) throws IOException {
        InternalParser internalParser = new InternalParser();
        InputStream inputStream = this.getClass().getResourceAsStream("/me/tomassetti/turin/" + exampleName + ".to");
        TurinParser.TurinFileContext turinFileContext = internalParser.produceParseTree(inputStream);
        return  turinFileContext;
    }

    @Test
    public void parseImports() throws IOException {
        TurinParser.TurinFileContext root = parse("imports_example");
        assertEquals(5, root.importDeclaration().size());
    }

}
