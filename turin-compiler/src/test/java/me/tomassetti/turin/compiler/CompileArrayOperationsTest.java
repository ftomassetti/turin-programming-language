package me.tomassetti.turin.compiler;

import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;

public class CompileArrayOperationsTest extends AbstractCompilerTest {

    @Test
    public void compileArrayLength() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        Method invoke = compileFunction("array_length", new Class[]{int[].class});
        assertEquals(0, invoke.invoke(null, new int[]{}));
        assertEquals(1, invoke.invoke(null, new int[]{1}));
        assertEquals(3, invoke.invoke(null, new int[]{1, 2, 3}));
    }

}
