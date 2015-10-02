package me.tomassetti.turin.compiler;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.TurinClassLoader;
import me.tomassetti.turin.parser.Parser;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.TurinFile;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ReferencesInOtherSrcFileTest extends AbstractCompilerTest {

    @Test
    public void referenceFunctionTypeFromOtherSrcFile() throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        TurinFile turinFileSrc = new Parser().parse(this.getClass().getResourceAsStream("/scenarios/referencetypefromothersrcfile/foo.to"));
        TurinFile turinFileTest = new Parser().parse(this.getClass().getResourceAsStream("/scenarios/referencetypefromothersrcfile/foo_test.to"));

        SymbolResolver resolver = getResolverFor(ImmutableList.of(turinFileSrc, turinFileTest),
                Collections.emptyList(),
                Collections.emptyList());
        Compiler instance = new Compiler(resolver, new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitionsSrc = instance.compile(turinFileSrc, new MyErrorCollector());
        assertEquals(1, classFileDefinitionsSrc.size());
        saveClassFile(classFileDefinitionsSrc.get(0), "tmp");
        List<ClassFileDefinition> classFileDefinitionsTest = instance.compile(turinFileTest, new MyErrorCollector());
        assertEquals(1, classFileDefinitionsTest.size());
        saveClassFile(classFileDefinitionsTest.get(0), "tmp");

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        turinClassLoader.addClass(classFileDefinitionsSrc.get(0));
        Class testClass = turinClassLoader.addClass(classFileDefinitionsTest.get(0));

        assertEquals(9876, testClass.getMethod("invoke").invoke(null));
    }

    @Test
    public void referenceFunctionFromOtherSrcFile() throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        TurinFile turinFileSrc = new Parser().parse(this.getClass().getResourceAsStream("/scenarios/referencefunctionfromothersrcfile/foo.to"));
        TurinFile turinFileTest = new Parser().parse(this.getClass().getResourceAsStream("/scenarios/referencefunctionfromothersrcfile/foo_test.to"));

        SymbolResolver resolver = getResolverFor(ImmutableList.of(turinFileSrc, turinFileTest),
                ImmutableList.of("src/test/resources/jars/junit-4.12.jar"),
                Collections.emptyList());
        Compiler instance = new Compiler(resolver, new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitionsSrc = instance.compile(turinFileSrc, new MyErrorCollector());
        assertEquals(1, classFileDefinitionsSrc.size());
        List<ClassFileDefinition> classFileDefinitionsTest = instance.compile(turinFileTest, new MyErrorCollector());
        assertEquals(1, classFileDefinitionsTest.size());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        turinClassLoader.addClass(classFileDefinitionsSrc.get(0));
        Class testClass = turinClassLoader.addClass(classFileDefinitionsTest.get(0));

        testClass.getMethod("invoke").invoke(null);
    }

    @Test
    public void resolveToRightVersionOfAssertEquals() throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        TurinFile turinFileSrc = new Parser().parse(this.getClass().getResourceAsStream("/scenarios/overloadresolution/foo.to"));
        TurinFile turinFileTest = new Parser().parse(this.getClass().getResourceAsStream("/scenarios/overloadresolution/foo_test.to"));

        SymbolResolver resolver = getResolverFor(ImmutableList.of(turinFileSrc, turinFileTest),
                ImmutableList.of("src/test/resources/jars/junit-4.12.jar"),
                Collections.emptyList());
        Compiler instance = new Compiler(resolver, new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitionsSrc = instance.compile(turinFileSrc, new MyErrorCollector());
        assertEquals(1, classFileDefinitionsSrc.size());
        List<ClassFileDefinition> classFileDefinitionsTest = instance.compile(turinFileTest, new MyErrorCollector());
        assertEquals(1, classFileDefinitionsTest.size());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        turinClassLoader.addClass(classFileDefinitionsSrc.get(0));
        Class testClass = turinClassLoader.addClass(classFileDefinitionsTest.get(0));

        testClass.getMethod("invoke").invoke(null);
    }

    @Test
    public void referenceFunctionTypeFromOtherClassesDir() throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        TurinFile turinFileSrc = new Parser().parse(this.getClass().getResourceAsStream("/scenarios/referencetoseparateclassdir/classdira/foo.to"));

        SymbolResolver resolver1 = getResolverFor(ImmutableList.of(turinFileSrc),
                Collections.emptyList(),
                Collections.emptyList());
        File tmpDir = Files.createTempDirectory("classes").toFile();
        tmpDir.deleteOnExit();
        // generate bytecode
        Compiler instance1 = new Compiler(resolver1, new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitionsSrc = instance1.compile(turinFileSrc, new MyErrorCollector());
        assertEquals(1, classFileDefinitionsSrc.size());
        saveClassFile(classFileDefinitionsSrc.get(0), tmpDir.getAbsolutePath());

        TurinFile turinFileTest = new Parser().parse(this.getClass().getResourceAsStream("/scenarios/referencetoseparateclassdir/classdirb/foo_test.to"));
        SymbolResolver resolver2 = getResolverFor(ImmutableList.of(turinFileTest),
                Collections.emptyList(),
                ImmutableList.of(tmpDir.getAbsolutePath()));
        Compiler instance2 = new Compiler(resolver2, new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitionsTest = instance2.compile(turinFileTest, new MyErrorCollector());
        assertEquals(1, classFileDefinitionsTest.size());
        // if it compiles that is enough
    }

    @Test
    public void referenceConstructorFromOtherClassesDir() throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        TurinFile turinFileSrc = new Parser().parse(this.getClass().getResourceAsStream("/scenarios/constructorinseparateclassdir/classdira/foo.to"));

        SymbolResolver resolver1 = getResolverFor(ImmutableList.of(turinFileSrc),
                Collections.emptyList(),
                Collections.emptyList());
        File tmpDir = Files.createTempDirectory("classes").toFile();
        tmpDir.deleteOnExit();
        // generate bytecode
        Compiler instance1 = new Compiler(resolver1, new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitionsSrc = instance1.compile(turinFileSrc, new MyErrorCollector());
        assertEquals(1, classFileDefinitionsSrc.size());
        saveClassFile(classFileDefinitionsSrc.get(0), tmpDir.getAbsolutePath());

        TurinFile turinFileTest = new Parser().parse(this.getClass().getResourceAsStream("/scenarios/constructorinseparateclassdir/classdirb/foo_test.to"));
        SymbolResolver resolver2 = getResolverFor(ImmutableList.of(turinFileTest),
                Collections.emptyList(),
                ImmutableList.of(tmpDir.getAbsolutePath()));
        Compiler instance2 = new Compiler(resolver2, new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitionsTest = instance2.compile(turinFileTest, new MyErrorCollector());
        assertEquals(3, classFileDefinitionsTest.size());
        // if it compiles that is enough

        // we need to add it, otherwise we cannot invoke the methods by reflection
        TurinClassLoader classLoader = new TurinClassLoader();
        Class abc = classLoader.addClass(classFileDefinitionsSrc.get(0));
        Class foo1 = classLoader.addClass(classFileDefinitionsTest.get(0));
        Object res1 = foo1.getMethod("invoke").invoke(null);
        assertEquals(27, res1.getClass().getMethod("getA").invoke(res1));
        assertEquals(5, res1.getClass().getMethod("getB").invoke(res1));
        Class foo2 = classLoader.addClass(classFileDefinitionsTest.get(1));
        Object res2 = foo2.getMethod("invoke").invoke(null);
        assertEquals(27, res2.getClass().getMethod("getA").invoke(res2));
        assertEquals(28, res2.getClass().getMethod("getB").invoke(res2));
        Class foo3 = classLoader.addClass(classFileDefinitionsTest.get(2));
        Object res3 = foo3.getMethod("invoke").invoke(null);
        assertEquals(28, res3.getClass().getMethod("getA").invoke(res3));
        assertEquals(27, res3.getClass().getMethod("getB").invoke(res3));
    }

}
