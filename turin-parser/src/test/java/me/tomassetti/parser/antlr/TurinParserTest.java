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
        if (inputStream == null) {
            throw new RuntimeException("Example not found: " + exampleName);
        }
        TurinParser.TurinFileContext turinFileContext = internalParser.produceParseTree(inputStream);
        return  turinFileContext;
    }

    @Test
    public void parseImports() throws IOException {
        TurinParser.TurinFileContext root = parse("imports_example");
        assertEquals(5, root.importDeclaration().size());
    }

    @Test
    public void parseMethodDeclarationWithExpressionBody() throws IOException {
        TurinParser.TurinFileContext root = parse("method_definitions_expression");
    }

    @Test
    public void parseMethodDeclarationWithBlockBody() throws IOException {
        TurinParser.TurinFileContext root = parse("method_definitions_block");
    }

    @Test
    public void parseFunctionDeclarationWithExpressionBody() throws IOException {
        TurinParser.TurinFileContext root = parse("function_definitions_expression");
    }

    @Test
    public void parseFunctionDeclarationWithBlockBody() throws IOException {
        TurinParser.TurinFileContext root = parse("function_definitions_block");
    }

    @Test
    public void parseArgsLengthInRelExpr() throws IOException {
        TurinParser.TurinFileContext root = parse("args_length_different_from_one");
        assertEquals(1, root.fileMember().size());
        TurinParser.TopLevelFunctionDeclarationContext function = root.members.get(0).topLevelFunctionDeclaration();
        TurinParser.ExpressionContext funcBody = function.methodBody().expression();
        assertEquals("!=", funcBody.relOp.getText());
    }

    @Test
    public void parseFieldAccess() throws IOException {
        TurinParser.TurinFileContext root = parse("field_access");
        assertEquals(1, root.fileMember().size());
        TurinParser.TopLevelFunctionDeclarationContext function = root.members.get(0).topLevelFunctionDeclaration();
        TurinParser.ExpressionContext funcBody = function.methodBody().expression();
        assertEquals("length", funcBody.fieldName.getText());
    }

    @Test
    public void parseMethodInvokation() throws IOException {
        TurinParser.TurinFileContext root = parse("method_invokation");
        assertEquals(1, root.fileMember().size());
        TurinParser.TopLevelFunctionDeclarationContext function = root.members.get(0).topLevelFunctionDeclaration();
        TurinParser.ExpressionContext funcBody = function.methodBody().expression();
        assertEquals("length", funcBody.methodName.getText());
    }

    @Test
    public void parseStringInterpolation1() throws IOException {
        TurinParser.TurinFileContext root = parse("string_interp1");
        assertEquals(1, root.fileMember().size());
        TurinParser.TopLevelFunctionDeclarationContext function = root.members.get(0).topLevelFunctionDeclaration();
        TurinParser.ExpressionContext funcBody = function.methodBody().expression();
    }
}
