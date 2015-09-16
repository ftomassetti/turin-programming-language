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

    private Method compileFunction(String exampleName, Class[] paramTypes) throws NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/" + exampleName + ".to"));

        // generate bytecode
        Compiler instance = new Compiler(getResolverFor(turinFile), new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new MyErrorCollector());
        assertEquals(1, classFileDefinitions.size());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class functionClass = turinClassLoader.addClass(classFileDefinitions.get(0).getName(),
                classFileDefinitions.get(0).getBytecode());
        assertEquals(0, functionClass.getConstructors().length);

        Method invoke = functionClass.getMethod("invoke", paramTypes);
        return invoke;
    }

    @Test
    public void compileArrayLength() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        Method invoke = compileFunction("array_length", new Class[]{int[].class});
        assertEquals(0, invoke.invoke(null, new int[]{}));
        assertEquals(1, invoke.invoke(null, new int[]{1}));
        assertEquals(3, invoke.invoke(null, new int[]{1, 2, 3}));
    }


}
