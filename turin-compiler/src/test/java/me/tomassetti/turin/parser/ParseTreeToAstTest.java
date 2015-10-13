package me.tomassetti.turin.parser;

import com.google.common.collect.ImmutableList;
import me.tomassetti.parser.antlr.TurinParser;
import me.tomassetti.turin.implicit.BasicTypeUsage;
import me.tomassetti.turin.parser.ast.*;
import me.tomassetti.turin.parser.ast.expressions.*;
import me.tomassetti.turin.parser.ast.expressions.literals.IntLiteral;
import me.tomassetti.turin.parser.ast.expressions.literals.StringLiteral;
import me.tomassetti.turin.parser.ast.properties.PropertyDefinition;
import me.tomassetti.turin.parser.ast.properties.PropertyReference;
import me.tomassetti.turin.parser.ast.statements.BlockStatement;
import me.tomassetti.turin.parser.ast.statements.ExpressionStatement;
import me.tomassetti.turin.parser.ast.statements.VariableDeclaration;
import me.tomassetti.turin.parser.ast.typeusage.ReferenceTypeUsage;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ParseTreeToAstTest {

    private TurinFile basicMangaAst() {
        TurinFile turinFile = new TurinFile();

        NamespaceDefinition namespaceDefinition = new NamespaceDefinition("manga");

        turinFile.setNameSpace(namespaceDefinition);

        ReferenceTypeUsage stringType = new ReferenceTypeUsage("String");
        BasicTypeUsage intType = BasicTypeUsage.UINT;

        PropertyDefinition nameProperty = new PropertyDefinition("name", stringType, Optional.empty(), Optional.empty(), Collections.emptyList());

        turinFile.add(nameProperty);

        TurinTypeDefinition mangaCharacter = new TurinTypeDefinition("MangaCharacter");
        PropertyDefinition ageProperty = new PropertyDefinition("age", intType, Optional.empty(), Optional.empty(), Collections.emptyList());
        PropertyReference nameRef = new PropertyReference("name");
        mangaCharacter.add(nameRef);
        mangaCharacter.add(ageProperty);

        turinFile.add(mangaCharacter);

        return turinFile;
    }

    private TurinFile mangaAst() {
        TurinFile turinFile = new TurinFile();

        NamespaceDefinition namespaceDefinition = new NamespaceDefinition("manga");

        turinFile.setNameSpace(namespaceDefinition);

        ReferenceTypeUsage stringType = new ReferenceTypeUsage("String");
        BasicTypeUsage intType = BasicTypeUsage.UINT;

        PropertyDefinition nameProperty = new PropertyDefinition("name", stringType, Optional.empty(), Optional.empty(), Collections.emptyList());

        turinFile.add(nameProperty);

        TurinTypeDefinition mangaCharacter = new TurinTypeDefinition("MangaCharacter");
        PropertyDefinition ageProperty = new PropertyDefinition("age", intType, Optional.empty(), Optional.empty(), Collections.emptyList());
        PropertyReference nameRef = new PropertyReference("name");
        mangaCharacter.add(nameRef);
        mangaCharacter.add(ageProperty);

        turinFile.add(mangaCharacter);


        // val ranma = MangaCharacter("Ranma", 16)
        Creation value = new Creation("MangaCharacter", ImmutableList.of(new ActualParam(new StringLiteral("Ranma")), new ActualParam(new IntLiteral(16))));
        VariableDeclaration varDecl = new VariableDeclaration("ranma", value);
        // print("The protagonist is #{ranma}")
        StringInterpolation string = new StringInterpolation();
        string.add(new StringLiteral("The protagonist is "));
        string.add(new ValueReference("ranma"));
        FunctionCall functionCall = new FunctionCall(new ValueReference("print"), ImmutableList.of(new ActualParam(string)));
        Program program = new Program("MangaExample", new BlockStatement(ImmutableList.of(varDecl, new ExpressionStatement(functionCall))), "args");
        turinFile.add(program);

        return turinFile;
    }

    @Test
    public void convertBasicMangaExample() throws IOException {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("basicManga.to");
        TurinParser.TurinFileContext root = new InternalParser().produceParseTree(inputStream);
        assertEquals(basicMangaAst(), new ParseTreeToAst().toAst(root));
    }

    @Test
    public void convertMangaExample() throws IOException {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("manga.to");
        TurinParser.TurinFileContext root = new InternalParser().produceParseTree(inputStream);
        assertEquals(mangaAst(), new ParseTreeToAst().toAst(root));
    }

    @Test
    public void typeExtendingAndImplementin() throws IOException {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("parser_examples/type_extending_and_implementing.to");
        TurinParser.TurinFileContext root = new InternalParser().produceParseTree(inputStream);
        TurinFile turinFile = new ParseTreeToAst().toAst(root);
        assertEquals(1, turinFile.getTopLevelTypeDefinitions().size());
        TurinTypeDefinition typeDefinition = turinFile.getTopLevelTypeDefinitions().get(0);
        assertTrue(typeDefinition.getBaseType().isPresent());
        assertEquals(2, typeDefinition.getInterfaces().size());
    }

}
