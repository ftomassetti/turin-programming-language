package me.tomassetti.turin.compiler;

import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ReferenceToStaticFieldInCompiledClassCompilationTest extends AbstractCompilerTest {
    @Test
    public void compile() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        Method method = compileFunction("reference_to_static_field_compiled", new Class[]{});
        Object result = method.invoke(null);
        assertTrue(result instanceof Charset);
        assertEquals(result, StandardCharsets.UTF_8);
    }

}
