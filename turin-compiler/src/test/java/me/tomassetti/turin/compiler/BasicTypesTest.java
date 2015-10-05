package me.tomassetti.turin.compiler;

import me.tomassetti.turin.classloading.ClassFileDefinition;
import me.tomassetti.turin.classloading.TurinClassLoader;
import me.tomassetti.turin.parser.Parser;
import me.tomassetti.turin.parser.ast.TurinFile;
import org.junit.Test;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class BasicTypesTest extends AbstractCompilerTest {

    @Test
    public void compileUbyteProperty() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/ubyte_property.to"));

        // generate bytecode
        Compiler.Options options = new Compiler.Options();
        Compiler instance = new Compiler(getResolverFor(turinFile), options);
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new MyErrorCollector());
        assertEquals(4, classFileDefinitions.size());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class aClass = turinClassLoader.addClass(classFileDefinitions.get(0));
        Class foo1Class = turinClassLoader.addClass(classFileDefinitions.get(1));
        Class foo2Class = turinClassLoader.addClass(classFileDefinitions.get(2));
        Class foo3Class = turinClassLoader.addClass(classFileDefinitions.get(3));

        Optional<Throwable> exc1 = getException(foo1Class.getMethod("invoke"));
        Optional<Throwable> exc2 = getException(foo2Class.getMethod("invoke"));
        Optional<Throwable> exc3 = getException(foo3Class.getMethod("invoke"));

        assertEquals(true, exc1.isPresent());
        assertEquals(false, exc2.isPresent());
        assertEquals(false, exc3.isPresent());
    }

    @Test
    public void compileUshortProperty() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/ushort_property.to"));

        // generate bytecode
        Compiler.Options options = new Compiler.Options();
        Compiler instance = new Compiler(getResolverFor(turinFile), options);
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new MyErrorCollector());
        assertEquals(4, classFileDefinitions.size());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class aClass = turinClassLoader.addClass(classFileDefinitions.get(0));
        Class foo1Class = turinClassLoader.addClass(classFileDefinitions.get(1));
        Class foo2Class = turinClassLoader.addClass(classFileDefinitions.get(2));
        Class foo3Class = turinClassLoader.addClass(classFileDefinitions.get(3));

        Optional<Throwable> exc1 = getException(foo1Class.getMethod("invoke"));
        Optional<Throwable> exc2 = getException(foo2Class.getMethod("invoke"));
        Optional<Throwable> exc3 = getException(foo3Class.getMethod("invoke"));

        assertEquals(true, exc1.isPresent());
        assertEquals(false, exc2.isPresent());
        assertEquals(false, exc3.isPresent());
    }

    @Test
    public void compileUintProperty() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/uint_property.to"));

        // generate bytecode
        Compiler.Options options = new Compiler.Options();
        Compiler instance = new Compiler(getResolverFor(turinFile), options);
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new MyErrorCollector());
        assertEquals(4, classFileDefinitions.size());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class aClass = turinClassLoader.addClass(classFileDefinitions.get(0));
        Class foo1Class = turinClassLoader.addClass(classFileDefinitions.get(1));
        Class foo2Class = turinClassLoader.addClass(classFileDefinitions.get(2));
        Class foo3Class = turinClassLoader.addClass(classFileDefinitions.get(3));

        Optional<Throwable> exc1 = getException(foo1Class.getMethod("invoke"));
        Optional<Throwable> exc2 = getException(foo2Class.getMethod("invoke"));
        Optional<Throwable> exc3 = getException(foo3Class.getMethod("invoke"));

        assertEquals(true, exc1.isPresent());
        assertEquals(false, exc2.isPresent());
        assertEquals(false, exc3.isPresent());
    }

    @Test
    public void compileUlongProperty() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/ulong_property.to"));

        // generate bytecode
        Compiler.Options options = new Compiler.Options();
        Compiler instance = new Compiler(getResolverFor(turinFile), options);
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new MyErrorCollector());
        assertEquals(4, classFileDefinitions.size());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class aClass = turinClassLoader.addClass(classFileDefinitions.get(0));
        Class foo1Class = turinClassLoader.addClass(classFileDefinitions.get(1));
        Class foo2Class = turinClassLoader.addClass(classFileDefinitions.get(2));
        Class foo3Class = turinClassLoader.addClass(classFileDefinitions.get(3));

        Optional<Throwable> exc1 = getException(foo1Class.getMethod("invoke"));
        Optional<Throwable> exc2 = getException(foo2Class.getMethod("invoke"));
        Optional<Throwable> exc3 = getException(foo3Class.getMethod("invoke"));

        assertEquals(true, exc1.isPresent());
        assertEquals(false, exc2.isPresent());
        assertEquals(false, exc3.isPresent());
    }

    @Test
    public void compileUfloatProperty() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/ufloat_property.to"));

        // generate bytecode
        Compiler.Options options = new Compiler.Options();
        Compiler instance = new Compiler(getResolverFor(turinFile), options);
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new MyErrorCollector());
        assertEquals(4, classFileDefinitions.size());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class aClass = turinClassLoader.addClass(classFileDefinitions.get(0));
        Class foo1Class = turinClassLoader.addClass(classFileDefinitions.get(1));
        Class foo2Class = turinClassLoader.addClass(classFileDefinitions.get(2));
        Class foo3Class = turinClassLoader.addClass(classFileDefinitions.get(3));

        Optional<Throwable> exc1 = getException(foo1Class.getMethod("invoke"));
        Optional<Throwable> exc2 = getException(foo2Class.getMethod("invoke"));
        Optional<Throwable> exc3 = getException(foo3Class.getMethod("invoke"));

        assertEquals(true, exc1.isPresent());
        assertEquals(false, exc2.isPresent());
        assertEquals(false, exc3.isPresent());
    }

    @Test
    public void compileUdoubleProperty() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/udouble_property.to"));

        // generate bytecode
        Compiler.Options options = new Compiler.Options();
        Compiler instance = new Compiler(getResolverFor(turinFile), options);
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new MyErrorCollector());
        assertEquals(4, classFileDefinitions.size());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class aClass = turinClassLoader.addClass(classFileDefinitions.get(0));
        Class foo1Class = turinClassLoader.addClass(classFileDefinitions.get(1));
        Class foo2Class = turinClassLoader.addClass(classFileDefinitions.get(2));
        Class foo3Class = turinClassLoader.addClass(classFileDefinitions.get(3));

        Optional<Throwable> exc1 = getException(foo1Class.getMethod("invoke"));
        Optional<Throwable> exc2 = getException(foo2Class.getMethod("invoke"));
        Optional<Throwable> exc3 = getException(foo3Class.getMethod("invoke"));

        assertEquals(true, exc1.isPresent());
        assertEquals(false, exc2.isPresent());
        assertEquals(false, exc3.isPresent());
    }


}

