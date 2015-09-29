package me.tomassetti.turin.compiler;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.TurinClassLoader;
import me.tomassetti.turin.parser.Parser;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.TurinFile;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ResolutionOfOverloadedMethodsTest extends AbstractCompilerTest {

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

}
