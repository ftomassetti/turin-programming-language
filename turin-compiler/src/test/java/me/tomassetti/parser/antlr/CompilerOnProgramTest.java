package me.tomassetti.parser.antlr;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.TurinClassLoader;
import me.tomassetti.turin.compiler.ClassFileDefinition;
import me.tomassetti.turin.compiler.Compiler;
import me.tomassetti.turin.parser.ast.*;
import me.tomassetti.turin.parser.ast.expressions.*;
import me.tomassetti.turin.parser.ast.statements.ExpressionStatement;
import me.tomassetti.turin.parser.ast.statements.Statement;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * We test specifically the compilation of programs AST nodes.
 */
public class CompilerOnProgramTest {

    private TurinFile emptyProgram() {
        // define AST
        TurinFile turinFile = new TurinFile();

        NamespaceDefinition namespaceDefinition = new NamespaceDefinition("myProgram");

        turinFile.setNameSpace(namespaceDefinition);

        Program program = new Program("SuperSimple");
        turinFile.add(program);

        return turinFile;
    }

    private TurinFile simpleProgram() {
        // define AST
        TurinFile turinFile = new TurinFile();

        NamespaceDefinition namespaceDefinition = new NamespaceDefinition("myProgram");

        turinFile.setNameSpace(namespaceDefinition);

        Program program = new Program("SuperSimple");
        StringLiteral stringLiteral = new StringLiteral("Hello Turin!");
        QualifiedName javaLang = new QualifiedName(new QualifiedName("java"), "lang");
        TypeIdentifier system = new TypeIdentifier(javaLang, "System");
        StaticFieldAccess out = new StaticFieldAccess(system, "out");
        FieldAccess println = new FieldAccess(out, "println");
        FunctionCall printInvokation = new FunctionCall(println, ImmutableList.of(new ActualParam(stringLiteral)));
        Statement printStatement = new ExpressionStatement(printInvokation);
        program.add(printStatement);
        turinFile.add(program);

        return turinFile;
    }

    private void invokeProgram(Class<?> programClass) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method main = programClass.getMethod("main", String[].class);
        assertEquals("main", main.getName());
        assertEquals(true, Modifier.isStatic(main.getModifiers()));
        assertEquals(1, main.getParameterTypes().length);
        assertEquals(String[].class, main.getParameterTypes()[0]);
        main.invoke(null, (Object)new String[]{});
    }

    private void loadAndInvoke(ClassFileDefinition classFileDefinition) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class programClass = turinClassLoader.addClass(classFileDefinition.getName(),
                classFileDefinition.getBytecode());
        invokeProgram(programClass);
    }

    @Test
    public void compileAnEmptyProgram() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        TurinFile turinFile = emptyProgram();

        // generate bytecode
        Compiler instance = new Compiler();
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile);
        assertEquals(1, classFileDefinitions.size());

        loadAndInvoke(classFileDefinitions.get(0));
    }

    @Test
    public void compileASimpleProgram() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        TurinFile turinFile = simpleProgram();

        // generate bytecode
        Compiler instance = new Compiler();
        List<ClassFileDefinition> classFileDefinitions = instance.compile(turinFile);
        assertEquals(1, classFileDefinitions.size());
        assertEquals("myProgram.SuperSimple", classFileDefinitions.get(0).getName());

        loadAndInvoke(classFileDefinitions.get(0));
    }


}
