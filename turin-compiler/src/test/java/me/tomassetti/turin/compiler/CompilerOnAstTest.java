package me.tomassetti.turin.compiler;

import me.tomassetti.turin.*;
import me.tomassetti.turin.implicit.BasicTypeUsage;
import me.tomassetti.turin.parser.analysis.resolvers.InFileResolver;
import me.tomassetti.turin.parser.analysis.resolvers.jdk.JdkTypeResolver;
import me.tomassetti.turin.parser.ast.NamespaceDefinition;
import me.tomassetti.turin.parser.ast.*;
import me.tomassetti.turin.parser.ast.TurinFile;
import me.tomassetti.turin.parser.ast.typeusage.ReferenceTypeUsage;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.Assert.*;

public class CompilerOnAstTest extends AbstractCompilerTest {

    private TurinFile mangaAst() {
        // define AST
        TurinFile turinFile = new TurinFile();

        NamespaceDefinition namespaceDefinition = new NamespaceDefinition("manga");

        turinFile.setNameSpace(namespaceDefinition);

        ReferenceTypeUsage stringType = new ReferenceTypeUsage("String");
        BasicTypeUsage intType = BasicTypeUsage.UINT;

        PropertyDefinition nameProperty = new PropertyDefinition("name", stringType);

        turinFile.add(nameProperty);

        TurinTypeDefinition mangaCharacter = new TurinTypeDefinition("MangaCharacter");
        mangaCharacter.setPosition(Position.create(0, 0, 0, 0));
        PropertyDefinition ageProperty = new PropertyDefinition("age", intType);
        PropertyReference nameRef = new PropertyReference("name");
        mangaCharacter.add(nameRef);
        mangaCharacter.add(ageProperty);

        turinFile.add(mangaCharacter);
        return turinFile;
    }

    @Test
    public void compileAstManga() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        TurinFile turinFile = mangaAst();

        // generate bytecode
        Compiler instance = new Compiler(new InFileResolver(JdkTypeResolver.getInstance()), new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new AbstractCompilerTest.MyErrorCollector());
        assertEquals(1, classFileDefinitions.size());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class mangaCharacterClass = turinClassLoader.addClass(classFileDefinitions.get(0).getName(),
                classFileDefinitions.get(0).getBytecode());
        assertEquals(1, mangaCharacterClass.getConstructors().length);
        Object ranma = mangaCharacterClass.getConstructors()[0].newInstance("Ranma", 16);

        Method getName = mangaCharacterClass.getMethod("getName");
        assertEquals("Ranma", getName.invoke(ranma));

        Method getAge = mangaCharacterClass.getMethod("getAge");
        assertEquals(16, getAge.invoke(ranma));
    }

    @Test
    public void compileAstRegistryPerson() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        TurinFile turinFile = ExamplesAst.registryAst();

        // generate bytecode
        Compiler instance = new Compiler(new InFileResolver(JdkTypeResolver.getInstance()), new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new AbstractCompilerTest.MyErrorCollector());
        assertEquals(2, classFileDefinitions.size());

        assertEquals("registry.Person", classFileDefinitions.get(0).getName());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class personClass = turinClassLoader.addClass(classFileDefinitions.get(0).getName(),
                classFileDefinitions.get(0).getBytecode());
        assertEquals(1, personClass.getConstructors().length);
        assertEquals(2, personClass.getConstructors()[0].getParameterTypes().length);
        Object federico = personClass.getConstructors()[0].newInstance("Federico", "Tomassetti");

        Method getFirstName = personClass.getMethod("getFirstName");
        assertEquals("Federico", getFirstName.invoke(federico));

        Method getLastName = personClass.getMethod("getLastName");
        assertEquals("Tomassetti", getLastName.invoke(federico));
    }

    @Test
    public void compileAstRegistryAddress() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        TurinFile turinFile = ExamplesAst.registryAst();

        // generate bytecode
        Compiler instance = new Compiler(new InFileResolver(JdkTypeResolver.getInstance()), new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new AbstractCompilerTest.MyErrorCollector());
        assertEquals(2, classFileDefinitions.size());

        assertEquals("registry.Address", classFileDefinitions.get(1).getName());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class addressClass = turinClassLoader.addClass(classFileDefinitions.get(1).getName(),
                classFileDefinitions.get(1).getBytecode());
        assertEquals(1, addressClass.getConstructors().length);
        assertEquals(4, addressClass.getConstructors()[0].getParameterTypes().length);
        Object address = addressClass.getConstructors()[0].newInstance("Rue de Seze", 86, "Lyon", 69006);

        assertEquals("Rue de Seze", addressClass.getMethod("getStreet").invoke(address));
        assertEquals(86, addressClass.getMethod("getNumber").invoke(address));
        assertEquals("Lyon", addressClass.getMethod("getCity").invoke(address));
        assertEquals(69006, addressClass.getMethod("getZip").invoke(address));
    }

    @Test
    public void compileAstRegistryAddressSetters() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        TurinFile turinFile = ExamplesAst.registryAst();

        // generate bytecode
        Compiler instance = new Compiler(new InFileResolver(JdkTypeResolver.getInstance()), new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new AbstractCompilerTest.MyErrorCollector());
        assertEquals(2, classFileDefinitions.size());

        assertEquals("registry.Address", classFileDefinitions.get(1).getName());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class addressClass = turinClassLoader.addClass(classFileDefinitions.get(1).getName(),
                classFileDefinitions.get(1).getBytecode());
        assertEquals(1, addressClass.getConstructors().length);
        assertEquals(4, addressClass.getConstructors()[0].getParameterTypes().length);
        Object address = addressClass.getConstructors()[0].newInstance("Rue de Seze", 86, "Lyon", 69006);

        addressClass.getMethod("setStreet", String.class).invoke(address, "Piazza Emanuele Filiberto");
        addressClass.getMethod("setNumber", int.class).invoke(address, 4);
        addressClass.getMethod("setCity", String.class).invoke(address, "Torino");
        addressClass.getMethod("setZip", int.class).invoke(address, 10136);

        assertEquals("Piazza Emanuele Filiberto", addressClass.getMethod("getStreet").invoke(address));
        assertEquals(4, addressClass.getMethod("getNumber").invoke(address));
        assertEquals("Torino", addressClass.getMethod("getCity").invoke(address));
        assertEquals(10136, addressClass.getMethod("getZip").invoke(address));
    }

    @Test
    public void nullIsNotAcceptedForNameProperty() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        TurinFile turinFile = mangaAst();

        // generate bytecode
        Compiler instance = new Compiler(new InFileResolver(JdkTypeResolver.getInstance()), new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new AbstractCompilerTest.MyErrorCollector());
        assertEquals(1, classFileDefinitions.size());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class mangaCharacterClass = turinClassLoader.addClass(classFileDefinitions.get(0).getName(),
                classFileDefinitions.get(0).getBytecode());
        assertEquals(1, mangaCharacterClass.getConstructors().length);
        try {
            Object ranma = mangaCharacterClass.getConstructors()[0].newInstance(null, 16);
            fail("exception expected");
        } catch (InvocationTargetException e) {
            assertTrue(e.getTargetException() instanceof IllegalArgumentException);
            assertEquals("name cannot be null", e.getTargetException().getMessage());
        }
    }

    @Test
    public void negativeAgeIsNotAcceptedForNameProperty() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        TurinFile turinFile = mangaAst();

        // generate bytecode
        Compiler instance = new Compiler(new InFileResolver(JdkTypeResolver.getInstance()), new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new AbstractCompilerTest.MyErrorCollector());
        assertEquals(1, classFileDefinitions.size());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class mangaCharacterClass = turinClassLoader.addClass(classFileDefinitions.get(0).getName(),
                classFileDefinitions.get(0).getBytecode());
        assertEquals(1, mangaCharacterClass.getConstructors().length);
        try {
            Object ranma = mangaCharacterClass.getConstructors()[0].newInstance("Ranma", -16);
            fail("exception expected");
        } catch (InvocationTargetException e) {
            assertTrue(e.getTargetException() instanceof IllegalArgumentException);
            assertEquals("age should be positive", e.getTargetException().getMessage());
        }
    }

    @Test
    public void equalsIsGeneratedCorrectlyPositiveCase() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        TurinFile turinFile = mangaAst();

        // generate bytecode
        Compiler instance = new Compiler(new InFileResolver(JdkTypeResolver.getInstance()), new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new AbstractCompilerTest.MyErrorCollector());
        assertEquals(1, classFileDefinitions.size());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class mangaCharacterClass = turinClassLoader.addClass(classFileDefinitions.get(0).getName(),
                classFileDefinitions.get(0).getBytecode());
        assertEquals(1, mangaCharacterClass.getConstructors().length);
        Object ranma1 = mangaCharacterClass.getConstructors()[0].newInstance("Ranma", 16);
        Object ranma2 = mangaCharacterClass.getConstructors()[0].newInstance("Ranma", 16);

        assertTrue(ranma1.equals(ranma2));
        assertTrue(ranma2.equals(ranma1));
    }

    @Test
    public void equalsIsGeneratedCorrectlyDifferentName() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        TurinFile turinFile = mangaAst();

        // generate bytecode
        Compiler instance = new Compiler(new InFileResolver(JdkTypeResolver.getInstance()), new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new AbstractCompilerTest.MyErrorCollector());
        assertEquals(1, classFileDefinitions.size());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class mangaCharacterClass = turinClassLoader.addClass(classFileDefinitions.get(0).getName(),
                classFileDefinitions.get(0).getBytecode());
        assertEquals(1, mangaCharacterClass.getConstructors().length);
        Object ranma1 = mangaCharacterClass.getConstructors()[0].newInstance("Ranma", 16);
        Object ranma2 = mangaCharacterClass.getConstructors()[0].newInstance("Ranma Saotome", 16);

        assertFalse(ranma1.equals(ranma2));
        assertFalse(ranma2.equals(ranma1));
    }

    @Test
    public void equalsIsGeneratedCorrectlyDifferentAge() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        TurinFile turinFile = mangaAst();

        // generate bytecode
        Compiler instance = new Compiler(new InFileResolver(JdkTypeResolver.getInstance()), new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new AbstractCompilerTest.MyErrorCollector());
        assertEquals(1, classFileDefinitions.size());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class mangaCharacterClass = turinClassLoader.addClass(classFileDefinitions.get(0).getName(),
                classFileDefinitions.get(0).getBytecode());
        assertEquals(1, mangaCharacterClass.getConstructors().length);
        Object ranma1 = mangaCharacterClass.getConstructors()[0].newInstance("Ranma", 16);
        Object ranma2 = mangaCharacterClass.getConstructors()[0].newInstance("Ranma", 18);

        assertFalse(ranma1.equals(ranma2));
        assertFalse(ranma2.equals(ranma1));
    }

    @Test
    public void equalsIsGeneratedCorrectlyOnOtherClassReturnFalse() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        TurinFile turinFile = mangaAst();

        // generate bytecode
        Compiler instance = new Compiler(new InFileResolver(JdkTypeResolver.getInstance()), new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new AbstractCompilerTest.MyErrorCollector());
        assertEquals(1, classFileDefinitions.size());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class mangaCharacterClass = turinClassLoader.addClass(classFileDefinitions.get(0).getName(),
                classFileDefinitions.get(0).getBytecode());
        assertEquals(1, mangaCharacterClass.getConstructors().length);
        Object ranma1 = mangaCharacterClass.getConstructors()[0].newInstance("Ranma", 16);
        Object ranma2 = new String("Ranma");

        assertFalse(ranma1.equals(ranma2));
        assertFalse(ranma2.equals(ranma1));
    }

    @Test
    public void equalsIsGeneratedCorrectlyOnNullReturnFalse() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        TurinFile turinFile = mangaAst();

        // generate bytecode
        Compiler instance = new Compiler(new InFileResolver(JdkTypeResolver.getInstance()), new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new AbstractCompilerTest.MyErrorCollector());
        assertEquals(1, classFileDefinitions.size());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class mangaCharacterClass = turinClassLoader.addClass(classFileDefinitions.get(0).getName(),
                classFileDefinitions.get(0).getBytecode());
        assertEquals(1, mangaCharacterClass.getConstructors().length);
        Object ranma1 = mangaCharacterClass.getConstructors()[0].newInstance("Ranma", 16);
        Object ranma2 = null;

        assertFalse(ranma1.equals(ranma2));
    }

    @Test
    public void equalsIsGeneratedCorrectlyIsEqualsToItSelf() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        TurinFile turinFile = mangaAst();

        // generate bytecode
        Compiler instance = new Compiler(new InFileResolver(JdkTypeResolver.getInstance()), new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new AbstractCompilerTest.MyErrorCollector());
        assertEquals(1, classFileDefinitions.size());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class mangaCharacterClass = turinClassLoader.addClass(classFileDefinitions.get(0).getName(),
                classFileDefinitions.get(0).getBytecode());
        assertEquals(1, mangaCharacterClass.getConstructors().length);
        Object ranma1 = mangaCharacterClass.getConstructors()[0].newInstance("Ranma", 16);
        Object ranma2 = ranma1;

        assertTrue(ranma1.equals(ranma2));
    }

    @Test
    public void hashcodeIsGeneratedCorrectlyPositiveCase() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        TurinFile turinFile = mangaAst();

        // generate bytecode
        Compiler instance = new Compiler(new InFileResolver(JdkTypeResolver.getInstance()), new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new AbstractCompilerTest.MyErrorCollector());
        assertEquals(1, classFileDefinitions.size());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class mangaCharacterClass = turinClassLoader.addClass(classFileDefinitions.get(0).getName(),
                classFileDefinitions.get(0).getBytecode());
        assertEquals(1, mangaCharacterClass.getConstructors().length);
        Object ranma1 = mangaCharacterClass.getConstructors()[0].newInstance("Ranma", 16);
        Object ranma2 = mangaCharacterClass.getConstructors()[0].newInstance("Ranma", 16);

        assertTrue(ranma1.hashCode() == ranma2.hashCode());
        assertTrue(ranma2.hashCode() == ranma1.hashCode());
    }

    @Test
    public void hashcodeIsGeneratedCorrectlyDifferentName() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        TurinFile turinFile = mangaAst();

        // generate bytecode
        Compiler instance = new Compiler(new InFileResolver(JdkTypeResolver.getInstance()), new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new AbstractCompilerTest.MyErrorCollector());
        assertEquals(1, classFileDefinitions.size());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class mangaCharacterClass = turinClassLoader.addClass(classFileDefinitions.get(0).getName(),
                classFileDefinitions.get(0).getBytecode());
        assertEquals(1, mangaCharacterClass.getConstructors().length);
        Object ranma1 = mangaCharacterClass.getConstructors()[0].newInstance("Ranma", 16);
        Object ranma2 = mangaCharacterClass.getConstructors()[0].newInstance("Ranma Saotome", 16);

        assertFalse(ranma1.hashCode() == ranma2.hashCode());
        assertFalse(ranma2.hashCode() == ranma1.hashCode());
    }

    @Test
    public void hashcodeIsGeneratedCorrectlyDifferentAge() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        TurinFile turinFile = mangaAst();

        // generate bytecode
        Compiler instance = new Compiler(new InFileResolver(JdkTypeResolver.getInstance()), new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new AbstractCompilerTest.MyErrorCollector());
        assertEquals(1, classFileDefinitions.size());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class mangaCharacterClass = turinClassLoader.addClass(classFileDefinitions.get(0).getName(),
                classFileDefinitions.get(0).getBytecode());
        assertEquals(1, mangaCharacterClass.getConstructors().length);
        Object ranma1 = mangaCharacterClass.getConstructors()[0].newInstance("Ranma", 16);
        Object ranma2 = mangaCharacterClass.getConstructors()[0].newInstance("Ranma", 18);

        assertFalse(ranma1.hashCode() == ranma2.hashCode());
        assertFalse(ranma2.hashCode() == ranma1.hashCode());
    }

}
