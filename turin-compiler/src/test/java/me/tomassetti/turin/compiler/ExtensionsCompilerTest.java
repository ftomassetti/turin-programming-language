package me.tomassetti.turin.compiler;

import me.tomassetti.turin.classloading.TurinClassLoader;
import me.tomassetti.turin.classloading.ClassFileDefinition;
import me.tomassetti.turin.parser.Parser;
import me.tomassetti.turin.parser.ast.TurinFile;
import org.junit.Test;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import static org.junit.Assert.*;

public class ExtensionsCompilerTest extends AbstractCompilerTest {

    @Test
    public void theBaseTypeIsSetCorrectly() throws NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/type_extending.to"));

        // generate bytecode
        Compiler.Options options = new Compiler.Options();
        Compiler instance = new Compiler(getResolverFor(turinFile), options);
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, getErrorCollector());
        assertEquals(2, classFileDefinitions.size());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class typeB = turinClassLoader.addClass(classFileDefinitions.get(0));
        Class typeA = turinClassLoader.addClass(classFileDefinitions.get(1));
        assertEquals(typeB, typeA.getSuperclass());
        assertEquals(Object.class, typeB.getSuperclass());
    }

    @Test
    public void theImplementedInterfacesAreSetCorrectly() throws IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/type_implementing.to"));

        // generate bytecode
        Compiler.Options options = new Compiler.Options();
        Compiler instance = new Compiler(getResolverFor(turinFile), options);
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, getErrorCollector());
        assertEquals(1, classFileDefinitions.size());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class typeA = turinClassLoader.addClass(classFileDefinitions.get(0));
        assertEquals(2, typeA.getInterfaces().length);
        assertTrue(Serializable.class.isAssignableFrom(typeA));
        assertTrue(Cloneable.class.isAssignableFrom(typeA));
    }

}
