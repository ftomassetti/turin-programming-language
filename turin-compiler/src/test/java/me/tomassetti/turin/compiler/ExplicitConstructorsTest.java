package me.tomassetti.turin.compiler;

import me.tomassetti.turin.classloading.ClassFileDefinition;
import me.tomassetti.turin.classloading.TurinClassLoader;
import me.tomassetti.turin.compiler.errorhandling.ErrorCollector;
import me.tomassetti.turin.parser.Parser;
import me.tomassetti.turin.parser.ast.Position;
import me.tomassetti.turin.parser.ast.TurinFile;
import org.easymock.EasyMock;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static org.junit.Assert.*;

public class ExplicitConstructorsTest extends AbstractCompilerTest {

    @Test
    public void multipleExplicitConstructorsCauseAnError() throws IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/explicit_constructors.to"));

        // generate bytecode
        Compiler.Options options = new Compiler.Options();
        Compiler instance = new Compiler(getResolverFor(turinFile), options);

        ErrorCollector errorCollector = EasyMock.createMock(ErrorCollector.class);
        errorCollector.recordSemanticError(Position.create(5, 4, 7, 1), "At most one explicit constructor can be defined");
        errorCollector.recordSemanticError(Position.create(8, 4, 10, 1), "At most one explicit constructor can be defined");
        EasyMock.replay(errorCollector);

        instance.compile(turinFile, errorCollector);

        EasyMock.verify(errorCollector);
    }

    @Test
    public void explicitConstructorNoExtend() throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/explicit_constructor_no_extend.to"));

        // generate bytecode
        Compiler.Options options = new Compiler.Options();
        Compiler compiler = new Compiler(getResolverFor(turinFile), options);

        List<ClassFileDefinition> classFileDefinitions = compiler.compile(turinFile, new MyErrorCollector());
        assertEquals(1, classFileDefinitions.size());

        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class typeClass = turinClassLoader.addClass(classFileDefinitions.get(0));

        Object instance = typeClass.getConstructor(int.class).newInstance(123);
        assertEquals("123", typeClass.getMethod("getA").invoke(instance));
    }

}
