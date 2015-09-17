package me.tomassetti.turin.compiler;

import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;

public class ReferenceToCaughtExceptionCompilationTest extends AbstractCompilerTest {

    @Test
    public void referenceToCaughtExceptionCompilation() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        Method invoke = compileFunction("reference_to_caught_exception", new Class[]{});
        assertEquals("abcdef", invoke.invoke(null));
    }

}

