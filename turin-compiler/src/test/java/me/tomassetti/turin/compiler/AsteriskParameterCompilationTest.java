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
import java.util.Map;

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

}

