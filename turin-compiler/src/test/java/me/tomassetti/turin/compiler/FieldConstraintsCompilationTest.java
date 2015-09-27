package me.tomassetti.turin.compiler;

import me.tomassetti.turin.TurinClassLoader;
import me.tomassetti.turin.parser.Parser;
import me.tomassetti.turin.parser.ast.TurinFile;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class FieldConstraintsCompilationTest extends AbstractCompilerTest {

    @Test
    public void fieldConstraintsCompilation() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/field_constraints.to"));

        // generate bytecode
        Compiler.Options options = new Compiler.Options();
        Compiler instance = new Compiler(getResolverFor(turinFile), options);
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile, new MyErrorCollector());
        assertEquals(7, classFileDefinitions.size());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class optionsClass = turinClassLoader.addClass(classFileDefinitions.get(0).getName(),
                classFileDefinitions.get(0).getBytecode());
        Class foo1Class = turinClassLoader.addClass(classFileDefinitions.get(1).getName(),
                classFileDefinitions.get(1).getBytecode());
        Class foo2Class = turinClassLoader.addClass(classFileDefinitions.get(2).getName(),
                classFileDefinitions.get(2).getBytecode());
        Class foo3Class = turinClassLoader.addClass(classFileDefinitions.get(3).getName(),
                classFileDefinitions.get(3).getBytecode());
        Class foo4Class = turinClassLoader.addClass(classFileDefinitions.get(4).getName(),
                classFileDefinitions.get(4).getBytecode());
        Class foo5Class = turinClassLoader.addClass(classFileDefinitions.get(5).getName(),
                classFileDefinitions.get(5).getBytecode());
        Class foo6Class = turinClassLoader.addClass(classFileDefinitions.get(6).getName(),
                classFileDefinitions.get(6).getBytecode());

        Optional<Throwable> exc1 = getException(foo1Class.getMethod("invoke"));
        Optional<Throwable> exc2 = getException(foo2Class.getMethod("invoke"));
        Optional<Throwable> exc3 = getException(foo3Class.getMethod("invoke"));
        Optional<Throwable> exc4 = getException(foo4Class.getMethod("invoke"));
        Optional<Throwable> exc5 = getException(foo5Class.getMethod("invoke"));
        Optional<Throwable> exc6 = getException(foo6Class.getMethod("invoke"));

        assertEquals(true, exc1.isPresent());
        assertEquals(true, exc2.isPresent());
        assertEquals(true, exc3.isPresent());
        assertEquals(false, exc4.isPresent());
        assertEquals(true, exc5.isPresent());
        assertEquals(false, exc6.isPresent());
    }

    private Optional<Throwable> getException(Method functionMethod) throws IllegalAccessException {
        try {
            functionMethod.invoke(null);
            return Optional.empty();
        } catch (InvocationTargetException e) {
            return Optional.of(e.getTargetException());
        }
    }

}

