package me.tomassetti.turin.compiler;

import me.tomassetti.turin.TurinClassLoader;
import me.tomassetti.turin.parser.Parser;
import me.tomassetti.turin.parser.ast.TurinFile;
import org.junit.Test;
import org.objectweb.asm.signature.SignatureWriter;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class DefaultValuesCompilationTest extends AbstractCompilerTest {

    @Test
    public void theConstructorDoNotConsiderThePropertiesWithInitialValues() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/default_values.to"));

        // generate bytecode
        Compiler instance = new Compiler(getResolverFor(turinFile), new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new MyErrorCollector());
        assertEquals(5, classFileDefinitions.size());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class typeClass = turinClassLoader.addClass(classFileDefinitions.get(0).getName(),
                classFileDefinitions.get(0).getBytecode());
        assertEquals(1, typeClass.getConstructors().length);
        assertEquals(2, typeClass.getConstructors()[0].getParameterCount());
        assertEquals(int.class, typeClass.getConstructors()[0].getParameterTypes()[0]);
        assertEquals(Map.class, typeClass.getConstructors()[0].getParameterTypes()[1]);
    }

    @Test
    public void paramsAreAssignedCorrectly() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/default_values.to"));

        // generate bytecode
        Compiler instance = new Compiler(getResolverFor(turinFile), new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new MyErrorCollector());
        assertEquals(5, classFileDefinitions.size());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class typeClass = turinClassLoader.addClass(classFileDefinitions.get(0).getName(),
                classFileDefinitions.get(0).getBytecode());
        Class foo1 = turinClassLoader.addClass(classFileDefinitions.get(1).getName(),
                classFileDefinitions.get(1).getBytecode());
        saveClassFile(classFileDefinitions.get(1), "tmp");
        Class foo2 = turinClassLoader.addClass(classFileDefinitions.get(2).getName(),
                classFileDefinitions.get(2).getBytecode());
        Class foo3 = turinClassLoader.addClass(classFileDefinitions.get(3).getName(),
                classFileDefinitions.get(3).getBytecode());
        Class foo4 = turinClassLoader.addClass(classFileDefinitions.get(4).getName(),
                classFileDefinitions.get(4).getBytecode());

        Object result1 = foo1.getMethod("invoke").invoke(null);
        Object result2 = foo2.getMethod("invoke").invoke(null);
        Object result3 = foo3.getMethod("invoke").invoke(null);
        Object result4 = foo4.getMethod("invoke").invoke(null);

        assertEquals(1, typeClass.getMethod("getA").invoke(result1));
        assertEquals(10, typeClass.getMethod("getB").invoke(result1));
        assertEquals(2, typeClass.getMethod("getC").invoke(result1));
        assertEquals(3, typeClass.getMethod("getD").invoke(result1));

        assertEquals(1, typeClass.getMethod("getA").invoke(result2));
        assertEquals(10, typeClass.getMethod("getB").invoke(result2));
        assertEquals(2, typeClass.getMethod("getC").invoke(result2));
        assertEquals(7, typeClass.getMethod("getD").invoke(result2));

        assertEquals(1, typeClass.getMethod("getA").invoke(result3));
        assertEquals(10, typeClass.getMethod("getB").invoke(result3));
        assertEquals(5, typeClass.getMethod("getC").invoke(result3));
        assertEquals(3, typeClass.getMethod("getD").invoke(result3));

        assertEquals(1, typeClass.getMethod("getA").invoke(result4));
        assertEquals(10, typeClass.getMethod("getB").invoke(result4));
        assertEquals(2, typeClass.getMethod("getC").invoke(result4));
        assertEquals(3, typeClass.getMethod("getD").invoke(result4));
    }

}

