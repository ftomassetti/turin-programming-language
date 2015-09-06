package me.tomassetti.turin.compiler;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.TurinClassLoader;
import me.tomassetti.turin.implicit.BasicTypeUsage;
import me.tomassetti.turin.parser.Parser;
import me.tomassetti.turin.parser.analysis.resolvers.ComposedResolver;
import me.tomassetti.turin.parser.analysis.resolvers.InFileResolver;
import me.tomassetti.turin.parser.analysis.resolvers.Resolver;
import me.tomassetti.turin.parser.analysis.resolvers.SrcResolver;
import me.tomassetti.turin.parser.ast.*;
import me.tomassetti.turin.parser.ast.typeusage.ReferenceTypeUsage;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class CompilerOnFileTest {

    private Resolver getResolverFor(TurinFile turinFile) {
        return new ComposedResolver(ImmutableList.of(new InFileResolver(), new SrcResolver(ImmutableList.of(turinFile))));
    }

    @Test
    public void compileRanma() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/ranma.to"));

        // generate bytecode
        Compiler instance = new Compiler(getResolverFor(turinFile), new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile);
        assertEquals(2, classFileDefinitions.size());

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
    public void compileMangaWithMethods() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/manga_with_methods.to"));

        // generate bytecode
        Compiler instance = new Compiler(getResolverFor(turinFile), new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile);
        assertEquals(2, classFileDefinitions.size());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class mangaCharacterClass = turinClassLoader.addClass(classFileDefinitions.get(0).getName(),
                classFileDefinitions.get(0).getBytecode());
        assertEquals(1, mangaCharacterClass.getConstructors().length);
        Object ranma = mangaCharacterClass.getConstructors()[0].newInstance("Ranma", 16);

        Method toString = mangaCharacterClass.getMethod("toString");
        assertEquals("Ranma, 16", toString.invoke(ranma));
    }

    @Test
    public void compileInstantiationOfDate() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/instantiation_of_date.to"));

        // generate bytecode
        Compiler instance = new Compiler(getResolverFor(turinFile), new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile);
        assertEquals(1, classFileDefinitions.size());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class mangaCharacterClass = turinClassLoader.addClass(classFileDefinitions.get(0).getName(),
                classFileDefinitions.get(0).getBytecode());
    }

    @Test
    public void compileImportDateWithAsterisk() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/import_date_with_asterisk.to"));

        // generate bytecode
        Compiler instance = new Compiler(getResolverFor(turinFile), new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile);
        assertEquals(1, classFileDefinitions.size());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class mangaCharacterClass = turinClassLoader.addClass(classFileDefinitions.get(0).getName(),
                classFileDefinitions.get(0).getBytecode());
    }

    @Test
    public void compileImportDateWithoutAsterisk() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/import_date_without_asterisk.to"));

        // generate bytecode
        Compiler instance = new Compiler(getResolverFor(turinFile), new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile);
        assertEquals(1, classFileDefinitions.size());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class mangaCharacterClass = turinClassLoader.addClass(classFileDefinitions.get(0).getName(),
                classFileDefinitions.get(0).getBytecode());
    }

    @Test
    public void compileImportDateWithAlias() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/import_date_with_alias.to"));

        // generate bytecode
        Compiler instance = new Compiler(getResolverFor(turinFile), new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile);
        assertEquals(1, classFileDefinitions.size());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class mangaCharacterClass = turinClassLoader.addClass(classFileDefinitions.get(0).getName(),
                classFileDefinitions.get(0).getBytecode());
    }

    @Test
    public void compilePrimitiveData() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/primitive_data.to"));

        // generate bytecode
        Compiler compiler = new Compiler(getResolverFor(turinFile), new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitions = compiler.compile(turinFile);
        assertEquals(8, classFileDefinitions.size());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Map<String, Class> clazzes = new HashMap<>();
        for (ClassFileDefinition classFileDefinition : classFileDefinitions) {
            Class clazz = turinClassLoader.addClass(classFileDefinition.getName(),
                    classFileDefinition.getBytecode());
            clazzes.put(clazz.getSimpleName(), clazz);
        }

        Class hasPrimitiveType = null;
        Method toString = null;
        Object instance = null;

        hasPrimitiveType = clazzes.get("UseBoolean");
        assertEquals(1, hasPrimitiveType.getConstructors().length);
        instance = hasPrimitiveType.getConstructors()[0].newInstance(true);
        toString = hasPrimitiveType.getMethod("toString");
        assertEquals("true", toString.invoke(instance));

        hasPrimitiveType = clazzes.get("UseChar");
        assertEquals(1, hasPrimitiveType.getConstructors().length);
        instance = hasPrimitiveType.getConstructors()[0].newInstance('Z');
        toString = hasPrimitiveType.getMethod("toString");
        assertEquals("Z", toString.invoke(instance));

        hasPrimitiveType = clazzes.get("UseByte");
        assertEquals(1, hasPrimitiveType.getConstructors().length);
        instance = hasPrimitiveType.getConstructors()[0].newInstance((byte)42);
        toString = hasPrimitiveType.getMethod("toString");
        assertEquals("42", toString.invoke(instance));

        hasPrimitiveType = clazzes.get("UseShort");
        assertEquals(1, hasPrimitiveType.getConstructors().length);
        instance = hasPrimitiveType.getConstructors()[0].newInstance((short)272);
        toString = hasPrimitiveType.getMethod("toString");
        assertEquals("272", toString.invoke(instance));

        hasPrimitiveType = clazzes.get("UseInt");
        assertEquals(1, hasPrimitiveType.getConstructors().length);
        instance = hasPrimitiveType.getConstructors()[0].newInstance(165000);
        toString = hasPrimitiveType.getMethod("toString");
        assertEquals("165000", toString.invoke(instance));

        hasPrimitiveType = clazzes.get("UseLong");
        assertEquals(1, hasPrimitiveType.getConstructors().length);
        instance = hasPrimitiveType.getConstructors()[0].newInstance(1000000000);
        toString = hasPrimitiveType.getMethod("toString");
        assertEquals("1000000000", toString.invoke(instance));

        hasPrimitiveType = clazzes.get("UseFloat");
        assertEquals(1, hasPrimitiveType.getConstructors().length);
        instance = hasPrimitiveType.getConstructors()[0].newInstance(42.0f);
        toString = hasPrimitiveType.getMethod("toString");
        assertEquals("42.0", toString.invoke(instance));

        hasPrimitiveType = clazzes.get("UseDouble");
        assertEquals(1, hasPrimitiveType.getConstructors().length);
        instance = hasPrimitiveType.getConstructors()[0].newInstance(42.0);
        toString = hasPrimitiveType.getMethod("toString");
        assertEquals("42.0", toString.invoke(instance));
    }

    @Test
    public void compileMath() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/math.to"));

        // generate bytecode
        Compiler instance = new Compiler(getResolverFor(turinFile), new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile);
        assertEquals(1, classFileDefinitions.size());
        saveClassFile(classFileDefinitions.get(0), "tmp");

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class math = turinClassLoader.addClass(classFileDefinitions.get(0).getName(),
                classFileDefinitions.get(0).getBytecode());
        assertEquals(1, math.getConstructors().length);
        Object mathInstance = math.getConstructors()[0].newInstance(3, 5);

        Method calc = math.getMethod("calc", int.class);
        assertEquals(15, calc.invoke(mathInstance, 0));
        assertEquals(20, calc.invoke(mathInstance, 5));
        assertEquals(25, calc.invoke(mathInstance, 10));
    }

    // Used for debugging
    private static void saveClassFile(ClassFileDefinition classFileDefinition, String dir) {
        File output = null;
        try {
            output = new File(dir + "/" + classFileDefinition.getName().replaceAll("\\.", "/") + ".class");
            output.getParentFile().mkdirs();
            FileOutputStream fos = new FileOutputStream(output);
            fos.write(classFileDefinition.getBytecode());
        } catch (IOException e) {
            System.err.println("Problem writing file "+output+": "+ e.getMessage());
            System.exit(3);
        }
    }

}

