package me.tomassetti.turin.compiler;

import me.tomassetti.turin.TurinClassLoader;
import me.tomassetti.turin.parser.Parser;
import me.tomassetti.turin.parser.ast.TurinFile;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ArrayAccessCompilationTest extends AbstractCompilerTest {

    @Test
    public void compileArrayAccess() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/array_access.to"));

        // generate bytecode
        Compiler instance = new Compiler(getResolverFor(turinFile), new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new MyErrorCollector());
        assertEquals(1, classFileDefinitions.size());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class functionClass = turinClassLoader.addClass(classFileDefinitions.get(0).getName(),
                classFileDefinitions.get(0).getBytecode());

        Method getByIndex = functionClass.getMethod("invoke", int[].class, int.class);
        assertEquals(27, getByIndex.invoke(null, new int[]{27, 29, 4}, 0));
        assertEquals(29, getByIndex.invoke(null, new int[]{27, 29, 4}, 1));
        assertEquals(4, getByIndex.invoke(null, new int[]{27, 29, 4}, 2));
    }

}

