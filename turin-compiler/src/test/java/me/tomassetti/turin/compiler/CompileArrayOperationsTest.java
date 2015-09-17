package me.tomassetti.turin.compiler;

import me.tomassetti.turin.TurinClassLoader;
import me.tomassetti.turin.parser.Parser;
import me.tomassetti.turin.parser.ast.TurinFile;
import me.tomassetti.turin.parser.ast.expressions.ValueReference;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CompileArrayOperationsTest extends AbstractCompilerTest {

    @Test
    public void compileArrayLength() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        Method invoke = compileFunction("array_length", new Class[]{int[].class});
        assertEquals(0, invoke.invoke(null, new int[]{}));
        assertEquals(1, invoke.invoke(null, new int[]{1}));
        assertEquals(3, invoke.invoke(null, new int[]{1, 2, 3}));
    }


}
