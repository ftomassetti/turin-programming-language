package me.tomassetti.turin.compiler;

import me.tomassetti.turin.TurinClassLoader;
import me.tomassetti.turin.compiler.errorhandling.ErrorCollector;
import me.tomassetti.turin.parser.Parser;
import me.tomassetti.turin.parser.ast.Position;
import me.tomassetti.turin.parser.ast.TurinFile;
import me.tomassetti.turin.parser.ast.statements.ThrowStatement;
import org.easymock.EasyMock;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.Assert.*;

public class ThrowCatchCompilationTest extends AbstractCompilerTest {

    @Test
    public void compileThrowStatement() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/throw_statement.to"));

        // generate bytecode
        Compiler instance = new Compiler(getResolverFor(turinFile), new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new MyErrorCollector());
        assertEquals(1, classFileDefinitions.size());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class functionClass = turinClassLoader.addClass(classFileDefinitions.get(0).getName(),
                classFileDefinitions.get(0).getBytecode());
        assertEquals(0, functionClass.getConstructors().length);

        Method invoke = functionClass.getMethod("invoke", String.class);
        try {
            invoke.invoke(null, "foo");
            fail("exception should have been thrown");
        } catch (InvocationTargetException e) {
            assertTrue(e.getTargetException() instanceof UnsupportedOperationException);
            UnsupportedOperationException unsupportedOperationException = (UnsupportedOperationException)e.getTargetException();
            assertEquals("To be implemented", unsupportedOperationException.getMessage());
        }
    }

    @Test
    public void throwStatementDoesNotAcceptSomethingWhichIsNotAnException() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/throw_statement_using_string.to"));

        ErrorCollector errorCollector = EasyMock.createMock(ErrorCollector.class);
        errorCollector.recordSemanticError(Position.create(4, 10, 4, 29), ThrowStatement.ERR_MESSAGE);
        EasyMock.replay(errorCollector);

        Compiler instance = new Compiler(getResolverFor(turinFile), new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, errorCollector);
        assertEquals(0, classFileDefinitions.size());

        EasyMock.verify(errorCollector);
    }

    @Test
    public void compileTryCatchStatement() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/try_catch_statement.to"));

        Compiler instance = new Compiler(getResolverFor(turinFile), new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new MyErrorCollector());
        assertEquals(1, classFileDefinitions.size());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class functionClass = turinClassLoader.addClass(classFileDefinitions.get(0).getName(),
                classFileDefinitions.get(0).getBytecode());
        Method method = functionClass.getMethod("invoke", int.class);
        assertEquals(-1, method.invoke(null, 0));
        assertEquals(-2, method.invoke(null, 1));
        assertEquals(2, method.invoke(null, 2));
    }

}

