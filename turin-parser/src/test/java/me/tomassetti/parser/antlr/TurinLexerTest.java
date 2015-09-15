package me.tomassetti.parser.antlr;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TurinLexerTest {

    private void verifyError(String code) throws IOException {
        InputStream stream = new ByteArrayInputStream(code.getBytes(StandardCharsets.UTF_8));

        final List<Integer> errors = new ArrayList<>();

        CharStream charStream = new ANTLRInputStream(stream);
        TurinLexer turinLexer = new TurinLexer(charStream);
        turinLexer.removeErrorListeners();
        turinLexer.addErrorListener(new ANTLRErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object o, int i, int i1, String s, RecognitionException e) {
                errors.add(i);
            }

            @Override
            public void reportAmbiguity(Parser parser, DFA dfa, int i, int i1, boolean b, BitSet bitSet, ATNConfigSet atnConfigSet) {

            }

            @Override
            public void reportAttemptingFullContext(Parser parser, DFA dfa, int i, int i1, BitSet bitSet, ATNConfigSet atnConfigSet) {

            }

            @Override
            public void reportContextSensitivity(Parser parser, DFA dfa, int i, int i1, int i2, ATNConfigSet atnConfigSet) {

            }
        });

        List<Token> tokens = new ArrayList<>();
        Token token;
        while ((token = turinLexer.nextToken()).getType() != -1) {
            tokens.add(token);
        }
        assertTrue(errors.size() > 0);
    }

    private void verifyModeIsNotInitial(String code) throws IOException {
        InputStream stream = new ByteArrayInputStream(code.getBytes(StandardCharsets.UTF_8));

        CharStream charStream = new ANTLRInputStream(stream);
        TurinLexer turinLexer = new TurinLexer(charStream);
        turinLexer.removeErrorListeners();
        turinLexer.addErrorListener(new ANTLRErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object o, int i, int i1, String s, RecognitionException e) {
                throw new RuntimeException("Syntax error found "+ i + " " + i1 + " "+s+" "+e.getMessage());
            }

            @Override
            public void reportAmbiguity(Parser parser, DFA dfa, int i, int i1, boolean b, BitSet bitSet, ATNConfigSet atnConfigSet) {

            }

            @Override
            public void reportAttemptingFullContext(Parser parser, DFA dfa, int i, int i1, BitSet bitSet, ATNConfigSet atnConfigSet) {

            }

            @Override
            public void reportContextSensitivity(Parser parser, DFA dfa, int i, int i1, int i2, ATNConfigSet atnConfigSet) {

            }
        });

        List<Token> tokens = new ArrayList<>();
        Token token;
        while ((token = turinLexer.nextToken()).getType() != -1) {
            tokens.add(token);
        }
        assertTrue(turinLexer._mode != 0);
    }

    private List<Token> parseCode(String code) throws IOException {
        InputStream stream = new ByteArrayInputStream(code.getBytes(StandardCharsets.UTF_8));

        CharStream charStream = new ANTLRInputStream(stream);
        TurinLexer turinLexer = new TurinLexer(charStream);
        turinLexer.removeErrorListeners();
        turinLexer.addErrorListener(new ANTLRErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object o, int i, int i1, String s, RecognitionException e) {
                throw new RuntimeException("Syntax error found "+ i + " " + i1 + " "+s+" "+e.getMessage());
            }

            @Override
            public void reportAmbiguity(Parser parser, DFA dfa, int i, int i1, boolean b, BitSet bitSet, ATNConfigSet atnConfigSet) {

            }

            @Override
            public void reportAttemptingFullContext(Parser parser, DFA dfa, int i, int i1, BitSet bitSet, ATNConfigSet atnConfigSet) {

            }

            @Override
            public void reportContextSensitivity(Parser parser, DFA dfa, int i, int i1, int i2, ATNConfigSet atnConfigSet) {

            }
        });

        List<Token> tokens = new ArrayList<>();
        Token token;
        while ((token = turinLexer.nextToken()).getType() != -1) {
            if (token.getChannel() == 0) {
                tokens.add(token);
            }
        }
        return tokens;
    }



    private List<Integer> getTokenTypes(String code) throws IOException {
        return parseCode(code).stream().map((t)->t.getType()).collect(Collectors.toList());
    }

    private void verify(String code, Integer... tokenTypes) throws IOException {
        assertEquals(Arrays.asList(tokenTypes), getTokenTypes(code));
    }

    @Test
    public void parseLineComment() throws IOException {
        String code = " // hi! \n an_id";
        verify(code, TurinLexer.NL, TurinLexer.VALUE_ID);
    }

    @Test
    public void parseBasicKeywords() throws IOException {
        String code = "namespace property val has type program";
        verify(code, TurinLexer.NAMESPACE_KW, TurinLexer.PROPERTY_KW, TurinLexer.VAL_KW, TurinLexer.HAS_KW, TurinLexer.TYPE_KW, TurinLexer.PROGRAM_KW);
    }

    @Test
    public void parseModifierKeywords() throws IOException {
        String code = "abstract shared";
        verify(code, TurinLexer.ABSTRACT_KW, TurinLexer.SHARED_KW);
    }

    @Test
    public void parseIDs() throws IOException {
        String code = "foo f122___ a___FOO";
        verify(code, TurinLexer.VALUE_ID, TurinLexer.VALUE_ID, TurinLexer.VALUE_ID);
    }

    @Test
    public void parseTIDs() throws IOException {
        String code = "Foo F122___ A___foo";
        verify(code, TurinLexer.TYPE_ID, TurinLexer.TYPE_ID, TurinLexer.TYPE_ID);
    }

    @Test
    public void parseIDsAndTIDsStartingWithUnderscore() throws IOException {
        String code = "_a __B";
        verify(code, TurinLexer.VALUE_ID, TurinLexer.TYPE_ID);
    }

    @Test
    public void parseSimpleString() throws IOException {
        String code = "\"Hello!\"";
        verify(code, TurinLexer.STRING_START, TurinLexer.STRING_CONTENT, TurinLexer.STRING_STOP);
    }

    @Test
    public void parseStringWithStringEndEscape() throws IOException {
        String code = "\"Hel\\\"lo!\"";
        verify(code, TurinLexer.STRING_START, TurinLexer.STRING_CONTENT, TurinLexer.STRING_STOP);
    }

    @Test
    public void parseStringWithUnclosedInterpolationInTheMiddleWithErrors() throws IOException {
        String code = "\"Hel#{lo!\"";
        verifyError(code);
    }

    @Test
    public void parseStringWithUnclosedInterpolationAtTheEndWithErrors() throws IOException {
        String code = "\"Hello!#{\"";
        verifyModeIsNotInitial(code);
    }

    @Test
    public void parseStringWithUnknownEscapeSequenceWithErrors() throws IOException {
        String code = "\"\\z\"";
        verifyError(code);
    }

    @Test
    public void parseStringWithEmptyInterpolation() throws IOException {
        String code = "\"Hel#{}lo!\"";
        verify(code, TurinLexer.STRING_START, TurinLexer.STRING_CONTENT, TurinLexer.INTERPOLATION_START, TurinLexer.INTERPOLATION_END, TurinLexer.STRING_CONTENT, TurinLexer.STRING_STOP);
    }

    @Test
    public void parseStringWithInterpolationContainingID() throws IOException {
        String code = "\"Hel#{foo}lo!\"";
        verify(code, TurinLexer.STRING_START, TurinLexer.STRING_CONTENT, TurinLexer.INTERPOLATION_START,
                TurinLexer.VALUE_ID,
                TurinLexer.INTERPOLATION_END, TurinLexer.STRING_CONTENT, TurinLexer.STRING_STOP);
    }

    @Test
    public void parseStringWithSharpSymbol() throws IOException {
        String code = "\"Hel#lo!\"";
        verify(code, TurinLexer.STRING_START, TurinLexer.STRING_CONTENT, TurinLexer.STRING_STOP);
    }

    @Test
    public void parseMethodDefinitionWithExpressionBody() throws IOException {
        String code = "Void toString() = \"foo\"";
        verify(code, TurinLexer.VOID_KW, TurinLexer.VALUE_ID, TurinLexer.LPAREN, TurinLexer.RPAREN, TurinLexer.ASSIGNMENT, TurinLexer.STRING_START, TurinLexer.STRING_CONTENT, TurinLexer.STRING_STOP);
    }

    @Test
    public void parseZero() throws IOException {
        String code = "0";
        verify(code, TurinLexer.INT);
    }

    @Test
    public void parseMinusZero() throws IOException {
        String code = "-0";
        verify(code, TurinLexer.INT);
    }

    @Test
    public void parsePositive() throws IOException {
        String code = "38";
        verify(code, TurinLexer.INT);
    }

    @Test
    public void parseNegative() throws IOException {
        String code = "-19";
        verify(code, TurinLexer.INT);
    }
}
