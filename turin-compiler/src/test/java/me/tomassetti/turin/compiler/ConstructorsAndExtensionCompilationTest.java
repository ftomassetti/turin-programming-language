package me.tomassetti.turin.compiler;

import com.github.javaparser.ast.expr.ObjectCreationExpr;
import me.tomassetti.turin.classloading.ClassFileDefinition;
import me.tomassetti.turin.classloading.TurinClassLoader;
import me.tomassetti.turin.parser.Parser;
import me.tomassetti.turin.parser.ast.TurinFile;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ConstructorsAndExtensionCompilationTest extends AbstractCompilerTest {

    @Test
    public void theGeneratedConstructorOfTheDerivedTypeTakesTheExpectedParameters() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/scenarios/constructor_extends1/points.to"));

        // generate bytecode
        Compiler instance = new Compiler(getResolverFor(turinFile), new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new MyErrorCollector());
        assertEquals(6, classFileDefinitions.size());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class pointClass = turinClassLoader.addClass(classFileDefinitions.get(0));
        Class labelledPointClass = turinClassLoader.addClass(classFileDefinitions.get(1));

        assertEquals(1, labelledPointClass.getConstructors().length);
        Constructor constructor = labelledPointClass.getConstructors()[0];
        assertEquals(4, constructor.getParameterCount());
        assertEquals(int.class, constructor.getParameterTypes()[0]);
        assertEquals(int.class, constructor.getParameterTypes()[1]);
        assertEquals(String.class, constructor.getParameterTypes()[2]);
        assertEquals(Map.class, constructor.getParameterTypes()[3]);
    }

    @Test
    public void theGeneratedConstructorOfTheDerivedTypeWorksAsExpected() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/scenarios/constructor_extends1/points.to"));

        // generate bytecode
        Compiler instance = new Compiler(getResolverFor(turinFile), new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new MyErrorCollector());
        assertEquals(6, classFileDefinitions.size());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class pointClass = turinClassLoader.addClass(classFileDefinitions.get(0));
        Class labelledPointClass = turinClassLoader.addClass(classFileDefinitions.get(1));
        Class foo1Class = turinClassLoader.addClass(classFileDefinitions.get(2));
        Class foo2Class = turinClassLoader.addClass(classFileDefinitions.get(3));
        Class foo3Class = turinClassLoader.addClass(classFileDefinitions.get(4));
        Class foo4Class = turinClassLoader.addClass(classFileDefinitions.get(5));

        Object res1 = foo1Class.getMethod("invoke", new Class[]{}).invoke(null);
        assertEquals(1, res1.getClass().getMethod("getX").invoke(res1));
        assertEquals(2, res1.getClass().getMethod("getY").invoke(res1));
        assertEquals(0, res1.getClass().getMethod("getZ").invoke(res1));
        assertEquals("hi", res1.getClass().getMethod("getLabel").invoke(res1));

        Object res2 = foo2Class.getMethod("invoke", new Class[]{}).invoke(null);
        assertEquals(1, res2.getClass().getMethod("getX").invoke(res2));
        assertEquals(2, res2.getClass().getMethod("getY").invoke(res2));
        assertEquals(3, res2.getClass().getMethod("getZ").invoke(res2));
        assertEquals("hi", res2.getClass().getMethod("getLabel").invoke(res2));

        Object res3 = foo3Class.getMethod("invoke", new Class[]{}).invoke(null);
        assertEquals(1, res3.getClass().getMethod("getX").invoke(res3));
        assertEquals(2, res3.getClass().getMethod("getY").invoke(res3));
        assertEquals(3, res3.getClass().getMethod("getZ").invoke(res3));
        assertEquals("hi", res3.getClass().getMethod("getLabel").invoke(res3));

        Object res4 = foo4Class.getMethod("invoke", new Class[]{}).invoke(null);
        assertEquals(1, res4.getClass().getMethod("getX").invoke(res4));
        assertEquals(2, res4.getClass().getMethod("getY").invoke(res4));
        assertEquals(0, res4.getClass().getMethod("getZ").invoke(res4));
        assertEquals("hi", res4.getClass().getMethod("getLabel").invoke(res4));
    }


}

