package me.tomassetti.turin.compiler;

import me.tomassetti.turin.classloading.TurinClassLoader;
import me.tomassetti.turin.classloading.ClassFileDefinition;
import me.tomassetti.turin.parser.Parser;
import me.tomassetti.turin.parser.ast.*;
import me.tomassetti.turin.parser.ast.typeusage.PrimitiveTypeUsage;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class CompilerOnFileTest extends AbstractCompilerTest {

    @Test
    public void compileRanma() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/ranma.to"));

        // generate bytecode
        Compiler instance = new Compiler(getResolverFor(turinFile), new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new MyErrorCollector());
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
    public void toStringIsGenerated() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/ranma.to"));

        // generate bytecode
        Compiler instance = new Compiler(getResolverFor(turinFile), new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new MyErrorCollector());
        assertEquals(2, classFileDefinitions.size());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class mangaCharacterClass = turinClassLoader.addClass(classFileDefinitions.get(0).getName(),
                classFileDefinitions.get(0).getBytecode());
        assertEquals(1, mangaCharacterClass.getConstructors().length);
        Object ranma = mangaCharacterClass.getConstructors()[0].newInstance("Ranma", 16);

        Method toString = mangaCharacterClass.getMethod("toString");
        assertEquals("MangaCharacter{name=Ranma, age=16}", toString.invoke(ranma));
    }

    @Test
    public void compileMangaWithMethods() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/manga_with_methods.to"));

        // generate bytecode
        Compiler instance = new Compiler(getResolverFor(turinFile), new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new MyErrorCollector());
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
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new MyErrorCollector());
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
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new MyErrorCollector());
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
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new MyErrorCollector());
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
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new MyErrorCollector());
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
        List<ClassFileDefinition> classFileDefinitions = compiler.compile(turinFile, new MyErrorCollector());
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
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new MyErrorCollector());
        assertEquals(1, classFileDefinitions.size());

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

    @Test
    public void pimitiveTypesHavePrecedence() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/boolean_literals.to"));

        assertEquals(PrimitiveTypeUsage.BOOLEAN, turinFile.getTopTypeDefinition("A").get().getDirectMethods().get(0).getReturnType());
    }

    @Test
    public void compileBooleanLiterals() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/boolean_literals.to"));

        // generate bytecode
        Compiler instance = new Compiler(getResolverFor(turinFile), new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new MyErrorCollector());
        assertEquals(1, classFileDefinitions.size());
        
        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class aClass = turinClassLoader.addClass(classFileDefinitions.get(0).getName(),
                classFileDefinitions.get(0).getBytecode());
        assertEquals(1, aClass.getConstructors().length);
        Object aInstance = aClass.getConstructors()[0].newInstance();

        Method foo1 = aClass.getMethod("foo1");
        Method foo2 = aClass.getMethod("foo2");
        assertEquals(false, foo1.invoke(aInstance));
        assertEquals(true, foo2.invoke(aInstance));
    }

    @Test
    public void compileAndOperator() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/logical_operators.to"));

        // generate bytecode
        Compiler instance = new Compiler(getResolverFor(turinFile), new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new MyErrorCollector());
        assertEquals(1, classFileDefinitions.size());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class aClass = turinClassLoader.addClass(classFileDefinitions.get(0).getName(),
                classFileDefinitions.get(0).getBytecode());
        assertEquals(1, aClass.getConstructors().length);
        Object aInstance = aClass.getConstructors()[0].newInstance();

        Method foo1 = aClass.getMethod("foo1");
        Method foo2 = aClass.getMethod("foo2");
        Method foo3 = aClass.getMethod("foo3");
        Method foo4 = aClass.getMethod("foo4");
        assertEquals(false, foo1.invoke(aInstance));
        assertEquals(false, foo2.invoke(aInstance));
        assertEquals(false, foo3.invoke(aInstance));
        assertEquals(true, foo4.invoke(aInstance));
    }

    @Test
    public void compileOrOperator() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/logical_operators.to"));

        // generate bytecode
        Compiler instance = new Compiler(getResolverFor(turinFile), new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new MyErrorCollector());
        assertEquals(1, classFileDefinitions.size());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class aClass = turinClassLoader.addClass(classFileDefinitions.get(0).getName(),
                classFileDefinitions.get(0).getBytecode());
        assertEquals(1, aClass.getConstructors().length);
        Object aInstance = aClass.getConstructors()[0].newInstance();

        Method foo5 = aClass.getMethod("foo5");
        Method foo6 = aClass.getMethod("foo6");
        Method foo7 = aClass.getMethod("foo7");
        Method foo8 = aClass.getMethod("foo8");
        assertEquals(false, foo5.invoke(aInstance));
        assertEquals(true, foo6.invoke(aInstance));
        assertEquals(true, foo7.invoke(aInstance));
        assertEquals(true, foo8.invoke(aInstance));
    }

    @Test
    public void compileNotOperator() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/logical_operators.to"));

        // generate bytecode
        Compiler instance = new Compiler(getResolverFor(turinFile), new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new MyErrorCollector());
        assertEquals(1, classFileDefinitions.size());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class aClass = turinClassLoader.addClass(classFileDefinitions.get(0).getName(),
                classFileDefinitions.get(0).getBytecode());
        assertEquals(1, aClass.getConstructors().length);
        Object aInstance = aClass.getConstructors()[0].newInstance();

        Method foo9 = aClass.getMethod("foo9");
        Method foo10 = aClass.getMethod("foo10");
        assertEquals(true, foo9.invoke(aInstance));
        assertEquals(false, foo10.invoke(aInstance));
    }

    @Test
    public void compileLogicalExpressions() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/logical_operators.to"));

        // generate bytecode
        Compiler instance = new Compiler(getResolverFor(turinFile), new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new MyErrorCollector());
        assertEquals(1, classFileDefinitions.size());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class aClass = turinClassLoader.addClass(classFileDefinitions.get(0).getName(),
                classFileDefinitions.get(0).getBytecode());
        assertEquals(1, aClass.getConstructors().length);
        Object aInstance = aClass.getConstructors()[0].newInstance();

        Method foo11 = aClass.getMethod("foo11");
        Method foo12 = aClass.getMethod("foo12");
        assertEquals(true, foo11.invoke(aInstance));
        assertEquals(false, foo12.invoke(aInstance));
    }

    @Test
    public void compileIntegerEqualExpresion() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/relational_operators.to"));

        // generate bytecode
        Compiler instance = new Compiler(getResolverFor(turinFile), new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new MyErrorCollector());
        assertEquals(1, classFileDefinitions.size());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class aClass = turinClassLoader.addClass(classFileDefinitions.get(0).getName(),
                classFileDefinitions.get(0).getBytecode());
        assertEquals(1, aClass.getConstructors().length);
        Object aInstance = aClass.getConstructors()[0].newInstance();

        Method foo1 = aClass.getMethod("foo1");
        Method foo2 = aClass.getMethod("foo2");
        Method foo3 = aClass.getMethod("foo3");
        Method foo4 = aClass.getMethod("foo4");
        assertEquals(true,  foo1.invoke(aInstance));
        assertEquals(true, foo2.invoke(aInstance));
        assertEquals(false, foo3.invoke(aInstance));
        assertEquals(false, foo4.invoke(aInstance));
    }

    @Test
    public void compileIntegerNotEqualExpresion() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/relational_operators.to"));

        // generate bytecode
        Compiler instance = new Compiler(getResolverFor(turinFile), new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new MyErrorCollector());
        assertEquals(1, classFileDefinitions.size());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class aClass = turinClassLoader.addClass(classFileDefinitions.get(0).getName(),
                classFileDefinitions.get(0).getBytecode());
        assertEquals(1, aClass.getConstructors().length);
        Object aInstance = aClass.getConstructors()[0].newInstance();

        Method foo5 = aClass.getMethod("foo5");
        Method foo6 = aClass.getMethod("foo6");
        Method foo7 = aClass.getMethod("foo7");
        Method foo8 = aClass.getMethod("foo8");
        assertEquals(false,  foo5.invoke(aInstance));
        assertEquals(false, foo6.invoke(aInstance));
        assertEquals(true, foo7.invoke(aInstance));
        assertEquals(true, foo8.invoke(aInstance));
    }

    @Test
    public void compileLessExpresion() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/relational_operators.to"));

        // generate bytecode
        Compiler instance = new Compiler(getResolverFor(turinFile), new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new MyErrorCollector());
        assertEquals(1, classFileDefinitions.size());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class aClass = turinClassLoader.addClass(classFileDefinitions.get(0).getName(),
                classFileDefinitions.get(0).getBytecode());
        assertEquals(1, aClass.getConstructors().length);
        Object aInstance = aClass.getConstructors()[0].newInstance();

        Method foo9 = aClass.getMethod("foo9");
        Method foo10 = aClass.getMethod("foo10");
        Method foo11 = aClass.getMethod("foo11");
        assertEquals(false,  foo9.invoke(aInstance));
        assertEquals(false, foo10.invoke(aInstance));
        assertEquals(true, foo11.invoke(aInstance));
    }

    @Test
    public void compileLessEqualExpresion() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/relational_operators.to"));

        // generate bytecode
        Compiler instance = new Compiler(getResolverFor(turinFile), new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new MyErrorCollector());
        assertEquals(1, classFileDefinitions.size());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class aClass = turinClassLoader.addClass(classFileDefinitions.get(0).getName(),
                classFileDefinitions.get(0).getBytecode());
        assertEquals(1, aClass.getConstructors().length);
        Object aInstance = aClass.getConstructors()[0].newInstance();

        Method foo12 = aClass.getMethod("foo12");
        Method foo13 = aClass.getMethod("foo13");
        Method foo14 = aClass.getMethod("foo14");
        assertEquals(false, foo12.invoke(aInstance));
        assertEquals(true, foo13.invoke(aInstance));
        assertEquals(true, foo14.invoke(aInstance));
    }

    @Test
    public void compileMoreExpresion() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/relational_operators.to"));

        // generate bytecode
        Compiler instance = new Compiler(getResolverFor(turinFile), new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new MyErrorCollector());
        assertEquals(1, classFileDefinitions.size());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class aClass = turinClassLoader.addClass(classFileDefinitions.get(0).getName(),
                classFileDefinitions.get(0).getBytecode());
        assertEquals(1, aClass.getConstructors().length);
        Object aInstance = aClass.getConstructors()[0].newInstance();

        Method foo15 = aClass.getMethod("foo15");
        Method foo16 = aClass.getMethod("foo16");
        Method foo17 = aClass.getMethod("foo17");
        assertEquals(true,  foo15.invoke(aInstance));
        assertEquals(false, foo16.invoke(aInstance));
        assertEquals(false, foo17.invoke(aInstance));
    }

    @Test
    public void compileMoreEqualExpresion() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/relational_operators.to"));

        // generate bytecode
        Compiler instance = new Compiler(getResolverFor(turinFile), new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new MyErrorCollector());
        assertEquals(1, classFileDefinitions.size());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class aClass = turinClassLoader.addClass(classFileDefinitions.get(0).getName(),
                classFileDefinitions.get(0).getBytecode());
        assertEquals(1, aClass.getConstructors().length);
        Object aInstance = aClass.getConstructors()[0].newInstance();

        Method foo18 = aClass.getMethod("foo18");
        Method foo19 = aClass.getMethod("foo19");
        Method foo20 = aClass.getMethod("foo20");
        assertEquals(true, foo18.invoke(aInstance));
        assertEquals(true, foo19.invoke(aInstance));
        assertEquals(false, foo20.invoke(aInstance));
    }

    @Test
    public void compileSimpleIf() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/if.to"));

        // generate bytecode
        Compiler instance = new Compiler(getResolverFor(turinFile), new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new MyErrorCollector());
        assertEquals(1, classFileDefinitions.size());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class aClass = turinClassLoader.addClass(classFileDefinitions.get(0).getName(),
                classFileDefinitions.get(0).getBytecode());
        assertEquals(1, aClass.getConstructors().length);
        Object aInstance = aClass.getConstructors()[0].newInstance();

        Method method = aClass.getMethod("foo1", boolean.class);
        assertEquals("B", method.invoke(aInstance, false));
        assertEquals("A", method.invoke(aInstance, true));
    }

    @Test
    public void compileSimpleIfElse() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/if.to"));

        // generate bytecode
        Compiler instance = new Compiler(getResolverFor(turinFile), new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new MyErrorCollector());
        assertEquals(1, classFileDefinitions.size());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class aClass = turinClassLoader.addClass(classFileDefinitions.get(0).getName(),
                classFileDefinitions.get(0).getBytecode());
        assertEquals(1, aClass.getConstructors().length);
        Object aInstance = aClass.getConstructors()[0].newInstance();

        Method method = aClass.getMethod("foo2", boolean.class);
        assertEquals("B", method.invoke(aInstance, false));
        assertEquals("A", method.invoke(aInstance, true));
    }

    @Test
    public void compileSimpleIfElifsElse() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/if.to"));

        // generate bytecode
        Compiler instance = new Compiler(getResolverFor(turinFile), new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new MyErrorCollector());
        assertEquals(1, classFileDefinitions.size());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class aClass = turinClassLoader.addClass(classFileDefinitions.get(0).getName(),
                classFileDefinitions.get(0).getBytecode());
        assertEquals(1, aClass.getConstructors().length);
        Object aInstance = aClass.getConstructors()[0].newInstance();

        Method method = aClass.getMethod("foo3", boolean.class, boolean.class, boolean.class);
        assertEquals("D", method.invoke(aInstance, false, false, false));
        assertEquals("C", method.invoke(aInstance, false, false, true));
        assertEquals("B", method.invoke(aInstance, false, true,  false));
        assertEquals("B", method.invoke(aInstance, false, true,  true));
        assertEquals("A", method.invoke(aInstance, true, false, false));
        assertEquals("A", method.invoke(aInstance, true, false, true));
        assertEquals("A", method.invoke(aInstance, true, true,  false));
        assertEquals("A", method.invoke(aInstance, true, true,  true));
    }

    @Test
    public void compileSimpleIfElifsNoElse() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/if.to"));

        // generate bytecode
        Compiler instance = new Compiler(getResolverFor(turinFile), new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new MyErrorCollector());
        assertEquals(1, classFileDefinitions.size());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class aClass = turinClassLoader.addClass(classFileDefinitions.get(0).getName(),
                classFileDefinitions.get(0).getBytecode());
        assertEquals(1, aClass.getConstructors().length);
        Object aInstance = aClass.getConstructors()[0].newInstance();

        Method method = aClass.getMethod("foo4", boolean.class, boolean.class, boolean.class);
        assertEquals("D", method.invoke(aInstance, false, false, false));
        assertEquals("C", method.invoke(aInstance, false, false, true));
        assertEquals("B", method.invoke(aInstance, false, true,  false));
        assertEquals("B", method.invoke(aInstance, false, true,  true));
        assertEquals("A", method.invoke(aInstance, true, false, false));
        assertEquals("A", method.invoke(aInstance, true, false, true));
        assertEquals("A", method.invoke(aInstance, true, true,  false));
        assertEquals("A", method.invoke(aInstance, true, true,  true));
    }

    @Test
    public void compileMathAndLogicalOperations() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/relational_operators.to"));

        // generate bytecode
        Compiler instance = new Compiler(getResolverFor(turinFile), new Compiler.Options());
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new MyErrorCollector());
        assertEquals(1, classFileDefinitions.size());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class aClass = turinClassLoader.addClass(classFileDefinitions.get(0).getName(),
                classFileDefinitions.get(0).getBytecode());
        assertEquals(1, aClass.getConstructors().length);
        Object aInstance = aClass.getConstructors()[0].newInstance();

        Method foo22 = aClass.getMethod("foo22", int.class);
        assertEquals(false,  foo22.invoke(aInstance, -10));
        assertEquals(false,  foo22.invoke(aInstance, 0));
        assertEquals(true,  foo22.invoke(aInstance, 1));
        assertEquals(true,  foo22.invoke(aInstance, 5));
        assertEquals(true,  foo22.invoke(aInstance, 20));
        assertEquals(true,  foo22.invoke(aInstance, 50));

        Method foo23 = aClass.getMethod("foo23", int.class);
        assertEquals(true,  foo23.invoke(aInstance, -10));
        assertEquals(true,  foo23.invoke(aInstance, 0));
        assertEquals(true,  foo23.invoke(aInstance, 1));
        assertEquals(true,  foo23.invoke(aInstance, 5));
        assertEquals(true,  foo23.invoke(aInstance, 20));
        assertEquals(false,  foo23.invoke(aInstance, 50));

        Method foo21 = aClass.getMethod("foo21", int.class);
        assertEquals(false,  foo21.invoke(aInstance, -10));
        assertEquals(false,  foo21.invoke(aInstance, 0));
        assertEquals(true,  foo21.invoke(aInstance, 1));
        assertEquals(true,  foo21.invoke(aInstance, 5));
        assertEquals(true,  foo21.invoke(aInstance, 20));
        assertEquals(false,  foo21.invoke(aInstance, 50));

        Method foo24 = aClass.getMethod("foo24", int.class);
        assertEquals(true,  foo24.invoke(aInstance, -10));
        assertEquals(true,  foo24.invoke(aInstance, 0));
        assertEquals(true,  foo24.invoke(aInstance, 1));
        assertEquals(true,  foo24.invoke(aInstance, 5));
        assertEquals(true,  foo24.invoke(aInstance, 20));
        assertEquals(true,  foo24.invoke(aInstance, 50));
    }

}

