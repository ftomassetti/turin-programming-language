package me.tomassetti.turin.compiler;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.TurinClassLoader;
import me.tomassetti.turin.parser.Parser;
import me.tomassetti.turin.parser.ast.TurinFile;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class AdvancedExamplesCompilationTest extends AbstractCompilerTest {

    @Test
    public void compileFormatter1() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/examples/formatter1.to"));

        // generate bytecode
        Compiler.Options options = new Compiler.Options();
        List<String> jars = ImmutableList.of("src/test/resources/jars/javaparser-core-2.2.1.jar");
        options.setClassPathElements(jars);
        Compiler instance = new Compiler(getResolverFor(turinFile, jars), options);
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new MyErrorCollector());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        for (ClassFileDefinition classFileDefinition : classFileDefinitions) {
            Class clazz = turinClassLoader.addClass(classFileDefinition.getName(), classFileDefinition.getBytecode());
            clazz.getName();
        }
    }

    @Test
    public void compileFormatter2() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/examples/formatter2.to"));

        // generate bytecode
        Compiler.Options options = new Compiler.Options();
        List<String> jars = ImmutableList.of("src/test/resources/jars/javaparser-core-2.2.1.jar");
        options.setClassPathElements(jars);
        Compiler instance = new Compiler(getResolverFor(turinFile, jars), options);
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new MyErrorCollector());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        for (ClassFileDefinition classFileDefinition : classFileDefinitions) {
            Class clazz = turinClassLoader.addClass(classFileDefinition.getName(), classFileDefinition.getBytecode());
            clazz.getName();
        }
    }

}
