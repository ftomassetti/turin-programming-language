package me.tomassetti.turin.compiler.relations;

import com.google.common.collect.ImmutableList;
import jdk.nashorn.internal.ir.Block;
import me.tomassetti.turin.classloading.ClassFileDefinition;
import me.tomassetti.turin.classloading.TurinClassLoader;
import me.tomassetti.turin.compiler.AbstractCompilerTest;
import me.tomassetti.turin.compiler.Compiler;
import me.tomassetti.turin.parser.Parser;
import me.tomassetti.turin.parser.ast.TurinFile;
import me.tomassetti.turin.parser.ast.expressions.Expression;
import me.tomassetti.turin.parser.ast.invokables.FunctionDefinitionNode;
import me.tomassetti.turin.parser.ast.statements.BlockStatement;
import me.tomassetti.turin.parser.ast.statements.ReturnStatement;
import me.tomassetti.turin.parser.ast.statements.Statement;
import me.tomassetti.turin.resolvers.ResolverRegistry;
import me.tomassetti.turin.resolvers.SymbolResolver;
import me.tomassetti.turin.typesystem.TypeUsage;
import org.junit.Test;
import turin.relations.OneToManyRelation;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class RelationsCompilationTest extends AbstractCompilerTest {

    @Test
    public void aClassWithTheCorrectNameIsGeneratedForARelation() throws IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/relations/simple_relation.to"));

        // generate bytecode
        me.tomassetti.turin.compiler.Compiler.Options options = new Compiler.Options();
        Compiler instance = new Compiler(getResolverFor(turinFile), options);
        List<ClassFileDefinition> classDefinitions = instance.compile(turinFile, new MyErrorCollector());
        assertEquals(2, classDefinitions.size());
        assertEquals("relations.Relation_Ast", classDefinitions.get(1).getName());
    }

    @Test
    public void theGeneratedRelationClassHasStaticFieldNeeded() throws IOException, IllegalAccessException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/relations/simple_relation.to"));

        // generate bytecode
        me.tomassetti.turin.compiler.Compiler.Options options = new Compiler.Options();
        Compiler instance = new Compiler(getResolverFor(turinFile), options);
        List<ClassFileDefinition> classDefinitions = instance.compile(turinFile, new MyErrorCollector());
        assertEquals(2, classDefinitions.size());
        Class astClass = new TurinClassLoader().addClass(classDefinitions.get(1));
        assertEquals(1, astClass.getFields().length);
        Field field = astClass.getFields()[0];
        assertEquals("RELATION", field.getName());
        assertTrue(Modifier.isStatic(field.getModifiers()));
        assertTrue(Modifier.isFinal(field.getModifiers()));
        Object value = field.get(null);
        assertNotNull(value);
        assertEquals(OneToManyRelation.class.getCanonicalName(), value.getClass().getCanonicalName());
    }

    @Test
    public void theGeneratedRelationClassHasMethodsForAccessingEndpoints() throws IOException, NoSuchMethodException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/relations/simple_relation.to"));

        // generate bytecode
        me.tomassetti.turin.compiler.Compiler.Options options = new Compiler.Options();
        Compiler instance = new Compiler(getResolverFor(turinFile), options);
        List<ClassFileDefinition> classDefinitions = instance.compile(turinFile, new MyErrorCollector());
        assertEquals(2, classDefinitions.size());
        TurinClassLoader turinClassLoader = new TurinClassLoader();
        Class nodeClass = turinClassLoader.addClass(classDefinitions.get(0));
        Class astClass = turinClassLoader.addClass(classDefinitions.get(1));
        assertEquals(2, astClass.getDeclaredMethods().length);
        Method parentForChildrenElement = astClass.getDeclaredMethod("parentForChildrenElement", new Class[]{nodeClass});
        Method childrenForParent = astClass.getDeclaredMethod("childrenForParent", new Class[]{nodeClass});
    }

    /*
    @Test
    public void aRelationSubsetIsGeneratedCorrectly() throws IOException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/relations/relation_subset.to"));

        // generate bytecode
        me.tomassetti.turin.compiler.Compiler.Options options = new Compiler.Options();
        Compiler instance = new Compiler(getResolverFor(turinFile), options);
        List<ClassFileDefinition> classDefinitions = instance.compile(turinFile, new MyErrorCollector());
        assertEquals(5, classDefinitions.size());
        assertEquals("relations.Relation_Ast", classDefinitions.get(2).getName());
    }*/

    /**
     * We verify if we can handle correctly the type parameters present in the relation endpoints.
     * @throws IOException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @Test
    public void relationsReturnCorrectType() throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/relations/relation_usage.to"));
        SymbolResolver resolver = getResolverFor(turinFile);
        ResolverRegistry.INSTANCE.record(turinFile, resolver);

        FunctionDefinitionNode foo2 = turinFile.getTopLevelFunctionDefinitions().get(1);
        assertEquals("foo2", foo2.getName());
        BlockStatement body = (BlockStatement) foo2.getBody();
        Statement lastStatement = body.getStatements().get(body.getStatements().size() - 1);
        ReturnStatement returnStatement = (ReturnStatement)lastStatement;
        // Probably JavaAssistTypeDefinition already replaced Type Variables
        Expression returnedValue = returnStatement.getValue();
        TypeUsage returnedValueType = returnedValue.calcType();
        assertTrue(returnedValueType.isReferenceTypeUsage());
        assertEquals("relations.Node", returnedValueType.asReferenceTypeUsage().getQualifiedName());
    }

    @Test
    public void relationUsage() throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        TurinFile turinFile = new Parser().parse(this.getClass().getResourceAsStream("/relations/relation_usage.to"));

        // generate bytecode
        me.tomassetti.turin.compiler.Compiler.Options options = new Compiler.Options();
        Compiler instance = new Compiler(getResolverFor(turinFile), options);
        List<ClassFileDefinition> classDefinitions = instance.compile(turinFile, new MyErrorCollector());
        assertEquals(6, classDefinitions.size());

        TurinClassLoader classLoader = new TurinClassLoader();
        Class nodeClass = classLoader.addClass(classDefinitions.get(0));
        Class astClass = classLoader.addClass(classDefinitions.get(1));
        saveClassFile(classDefinitions.get(1), "relations");
        Class foo1 = classLoader.addClass(classDefinitions.get(2));
        saveClassFile(classDefinitions.get(2), "relations");
        Class foo2 = classLoader.addClass(classDefinitions.get(3));
        saveClassFile(classDefinitions.get(3), "relations");
        Class foo3 = classLoader.addClass(classDefinitions.get(4));
        saveClassFile(classDefinitions.get(4), "relations");
        Class foo4 = classLoader.addClass(classDefinitions.get(5));

        // foo1 should gives true
        Object res1 = foo1.getMethod("invoke", new Class[]{}).invoke(null);
        assertEquals(true, res1);
        // foo2 should gives Node("C")
        Object res2 = foo2.getMethod("invoke", new Class[]{}).invoke(null);
        Object nodeC = nodeClass.getConstructor(String.class).newInstance("C");
        assertEquals(nodeC, res2);
        // foo3 should gives []
        Object res3 = foo3.getMethod("invoke", new Class[]{}).invoke(null);
        assertEquals(Collections.emptyList(), res3);
        // foo4 should gives [Node("A")]
        Object nodeA = nodeClass.getConstructor(String.class).newInstance("A");
        Object res4 = foo4.getMethod("invoke", new Class[]{}).invoke(null);
        assertEquals(ImmutableList.of(nodeA), res4);
    }

}
