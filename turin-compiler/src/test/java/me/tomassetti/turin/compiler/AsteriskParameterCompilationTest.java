package me.tomassetti.turin.compiler;

import me.tomassetti.turin.TurinClassLoader;
import me.tomassetti.turin.compiler.errorhandling.ErrorCollector;
import me.tomassetti.turin.parser.Parser;
import me.tomassetti.turin.parser.ast.Position;
import me.tomassetti.turin.parser.ast.TurinFile;
import org.easymock.EasyMock;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class AsteriskParameterCompilationTest extends AbstractCompilerTest {

    @Test
    public void asteriskParamCannotBeUsedWithOtherParams1() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/bad_usage_of_asterisk_on_creation1.to"));

        // generate bytecode
        Compiler.Options options = new Compiler.Options();
        Compiler instance = new Compiler(getResolverFor(turinFile), options);

        ErrorCollector errorCollector = EasyMock.createMock(ErrorCollector.class);
        errorCollector.recordSemanticError(EasyMock.eq(Position.create(10, 20, 10, 28)), EasyMock.anyString());
        EasyMock.replay(errorCollector);

        instance.compile(turinFile, errorCollector);

        EasyMock.verify(errorCollector);
    }

    @Test
    public void asteriskParamCannotBeUsedWithOtherParams2() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/bad_usage_of_asterisk_on_creation2.to"));

        // generate bytecode
        Compiler.Options options = new Compiler.Options();
        Compiler instance = new Compiler(getResolverFor(turinFile), options);

        ErrorCollector errorCollector = EasyMock.createMock(ErrorCollector.class);
        errorCollector.recordSemanticError(EasyMock.eq(Position.create(10, 22, 10, 30)), EasyMock.anyString());
        EasyMock.replay(errorCollector);

        instance.compile(turinFile, errorCollector);

        EasyMock.verify(errorCollector);
    }

    @Test
    public void asteriskParamCannotBeUsedWithOtherParams3() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/bad_usage_of_asterisk_on_creation3.to"));

        // generate bytecode
        Compiler.Options options = new Compiler.Options();
        Compiler instance = new Compiler(getResolverFor(turinFile), options);

        ErrorCollector errorCollector = EasyMock.createMock(ErrorCollector.class);
        errorCollector.recordSemanticError(EasyMock.eq(Position.create(10, 37, 10, 44)), EasyMock.anyString());
        EasyMock.replay(errorCollector);

        instance.compile(turinFile, errorCollector);

        EasyMock.verify(errorCollector);
    }

    @Test
    public void asteriskParamCannotBeUsedWithOverloadedMethods() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/bad_usage_of_asterisk_on_method1.to"));

        // generate bytecode
        Compiler.Options options = new Compiler.Options();
        Compiler instance = new Compiler(getResolverFor(turinFile), options);

        ErrorCollector errorCollector = EasyMock.createMock(ErrorCollector.class);
        errorCollector.recordSemanticError(EasyMock.eq(Position.create(10, 36, 10, 43)), EasyMock.anyString());
        EasyMock.replay(errorCollector);

        instance.compile(turinFile, errorCollector);

        EasyMock.verify(errorCollector);
    }


    @Test
    public void correctUsageOfAsteriskOnCreationWithObject() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/ok_usage_of_asterisk_in_creation_with_object.to"));

        // generate bytecode
        Compiler.Options options = new Compiler.Options();
        Compiler instance = new Compiler(getResolverFor(turinFile), options);

        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new MyErrorCollector());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class typeClass = turinClassLoader.addClass(classFileDefinitions.get(0).getName(),
                classFileDefinitions.get(0).getBytecode());
        Class foo1 = turinClassLoader.addClass(classFileDefinitions.get(1).getName(),
                classFileDefinitions.get(1).getBytecode());
        Class foo2 = turinClassLoader.addClass(classFileDefinitions.get(2).getName(),
                classFileDefinitions.get(2).getBytecode());
        Class foo3 = turinClassLoader.addClass(classFileDefinitions.get(3).getName(),
                classFileDefinitions.get(3).getBytecode());

        Object result1 = foo1.getMethod("invoke").invoke(null);
        Object result2 = foo2.getMethod("invoke").invoke(null);
        Object result3 = foo3.getMethod("invoke").invoke(null);

        assertEquals(1, typeClass.getMethod("getA").invoke(result1));
        assertEquals(10, typeClass.getMethod("getB").invoke(result1));
        assertEquals(5, typeClass.getMethod("getC").invoke(result1));
        assertEquals(7, typeClass.getMethod("getD").invoke(result1));

        assertEquals(1, typeClass.getMethod("getA").invoke(result2));
        assertEquals(10, typeClass.getMethod("getB").invoke(result2));
        assertEquals(15, typeClass.getMethod("getC").invoke(result2));
        assertEquals(7, typeClass.getMethod("getD").invoke(result2));

        assertEquals(1, typeClass.getMethod("getA").invoke(result3));
        assertEquals(10, typeClass.getMethod("getB").invoke(result3));
        assertEquals(5, typeClass.getMethod("getC").invoke(result3));
        assertEquals(15, typeClass.getMethod("getD").invoke(result3));
    }

    @Test
    public void correctUsageOfAsteriskOnMethodWithObject() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/ok_usage_of_asterisk_in_method_with_object.to"));

        // generate bytecode
        Compiler.Options options = new Compiler.Options();
        Compiler instance = new Compiler(getResolverFor(turinFile), options);

        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new MyErrorCollector());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class typeClass = turinClassLoader.addClass(classFileDefinitions.get(0).getName(),
                classFileDefinitions.get(0).getBytecode());
        Class callMe = turinClassLoader.addClass(classFileDefinitions.get(1).getName(),
                classFileDefinitions.get(1).getBytecode());
        Class foo1 = turinClassLoader.addClass(classFileDefinitions.get(2).getName(),
                classFileDefinitions.get(2).getBytecode());
        Class foo2 = turinClassLoader.addClass(classFileDefinitions.get(3).getName(),
                classFileDefinitions.get(3).getBytecode());
        Class foo3 = turinClassLoader.addClass(classFileDefinitions.get(4).getName(),
                classFileDefinitions.get(4).getBytecode());

        Object result1 = foo1.getMethod("invoke").invoke(null);
        Object result2 = foo2.getMethod("invoke").invoke(null);
        Object result3 = foo3.getMethod("invoke").invoke(null);

        assertEquals(23, typeClass.getMethod("getA").invoke(result1));
        assertEquals(10, typeClass.getMethod("getB").invoke(result1));
        assertEquals(5, typeClass.getMethod("getC").invoke(result1));
        assertEquals(7, typeClass.getMethod("getD").invoke(result1));

        assertEquals(1, typeClass.getMethod("getA").invoke(result2));
        assertEquals(10, typeClass.getMethod("getB").invoke(result2));
        assertEquals(15, typeClass.getMethod("getC").invoke(result2));
        assertEquals(7, typeClass.getMethod("getD").invoke(result2));

        assertEquals(1, typeClass.getMethod("getA").invoke(result3));
        assertEquals(10, typeClass.getMethod("getB").invoke(result3));
        assertEquals(5, typeClass.getMethod("getC").invoke(result3));
        assertEquals(7, typeClass.getMethod("getD").invoke(result3));
    }


}

