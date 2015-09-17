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

public class TopLevelFunctionInvokationCompilationTest extends AbstractCompilerTest {

    @Test
    public void referenceToCaughtExceptionCompilation() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/function_reference.to"));

        // generate bytecode
        Compiler.Options options = new Compiler.Options();
        Compiler instance = new Compiler(getResolverFor(turinFile), options);
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new MyErrorCollector());
        assertEquals(2, classFileDefinitions.size());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class foo1Class = turinClassLoader.addClass(classFileDefinitions.get(0).getName(),
                classFileDefinitions.get(0).getBytecode());
        Class foo2Class = turinClassLoader.addClass(classFileDefinitions.get(1).getName(),
                classFileDefinitions.get(1).getBytecode());

        Method invoke1 = foo1Class.getMethod("invoke");
        Method invoke2 = foo2Class.getMethod("invoke");

        assertEquals("foo1", invoke1.invoke(null));
        assertEquals("foo2", invoke2.invoke(null));
    }

}

