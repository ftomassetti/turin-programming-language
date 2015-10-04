package me.tomassetti.turin.compiler;

import me.tomassetti.turin.classloading.TurinClassLoader;
import me.tomassetti.turin.classloading.ClassFileDefinition;
import me.tomassetti.turin.parser.Parser;
import me.tomassetti.turin.parser.ast.TurinFile;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.Assert.*;

public class InitialValuesCompilationTest extends AbstractCompilerTest {

    @Test
    public void theConstructorDoNotConsiderThePropertiesWithInitialValues() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/initial_values.to"));

        // generate bytecode
        Compiler instance = new Compiler(getResolverFor(turinFile), new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new MyErrorCollector());
        assertEquals(2, classFileDefinitions.size());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class typeClass = turinClassLoader.addClass(classFileDefinitions.get(0).getName(),
                classFileDefinitions.get(0).getBytecode());
        saveClassFile(classFileDefinitions.get(0), "tmp");
        assertEquals(1, typeClass.getConstructors().length);
        assertEquals(2, typeClass.getConstructors()[0].getParameterCount());
        assertEquals(int.class, typeClass.getConstructors()[0].getParameterTypes()[0]);
        assertEquals(int.class, typeClass.getConstructors()[0].getParameterTypes()[1]);
    }

    @Test
    public void initialValuesAreSetCorrectly() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/initial_values.to"));

        // generate bytecode
        Compiler instance = new Compiler(getResolverFor(turinFile), new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new MyErrorCollector());
        assertEquals(2, classFileDefinitions.size());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class typeClass = turinClassLoader.addClass(classFileDefinitions.get(0).getName(),
                classFileDefinitions.get(0).getBytecode());
        Class functionClass = turinClassLoader.addClass(classFileDefinitions.get(1).getName(),
                classFileDefinitions.get(1).getBytecode());

        Method invoke = functionClass.getMethod("invoke");
        Object result = invoke.invoke(null);
        assertEquals(1, typeClass.getMethod("getA").invoke(result));
        assertEquals(10, typeClass.getMethod("getB").invoke(result));
        assertEquals(12, typeClass.getMethod("getC").invoke(result));
        assertEquals(2, typeClass.getMethod("getD").invoke(result));
    }

}

