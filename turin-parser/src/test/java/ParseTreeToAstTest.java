import com.google.common.collect.ImmutableList;
import me.tomassetti.parser.antlr.TurinParser;
import me.tomassetti.turin.parser.ParseTreeToAst;
import me.tomassetti.turin.parser.Parser;
import me.tomassetti.turin.parser.ast.*;
import me.tomassetti.turin.parser.ast.expressions.*;
import me.tomassetti.turin.parser.ast.statements.ExpressionStatement;
import me.tomassetti.turin.parser.ast.statements.VariableDeclaration;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;

/**
 * Created by federico on 29/08/15.
 */
public class ParseTreeToAstTest {

    private TurinFile basicMangaAst() {
        TurinFile turinFile = new TurinFile();

        NamespaceDefinition namespaceDefinition = new NamespaceDefinition("manga");

        turinFile.setNameSpace(namespaceDefinition);

        ReferenceTypeUsage stringType = new ReferenceTypeUsage("String");
        ReferenceTypeUsage intType = new ReferenceTypeUsage("UInt");

        PropertyDefinition nameProperty = new PropertyDefinition("name", stringType);

        turinFile.add(nameProperty);

        TypeDefinition mangaCharacter = new TypeDefinition("MangaCharacter");
        PropertyDefinition ageProperty = new PropertyDefinition("age", intType);
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
        ReferenceTypeUsage intType = new ReferenceTypeUsage("UInt");

        PropertyDefinition nameProperty = new PropertyDefinition("name", stringType);

        turinFile.add(nameProperty);

        TypeDefinition mangaCharacter = new TypeDefinition("MangaCharacter");
        PropertyDefinition ageProperty = new PropertyDefinition("age", intType);
        PropertyReference nameRef = new PropertyReference("name");
        mangaCharacter.add(nameRef);
        mangaCharacter.add(ageProperty);

        turinFile.add(mangaCharacter);

        Program program = new Program("MangaExample");
        // val ranma = MangaCharacter("Ranma", 16)
        Creation value = new Creation("MangaCharacter", ImmutableList.of(new ActualParam(new StringLiteral("Ranma")), new ActualParam(new IntLiteral(16))));
        VariableDeclaration varDecl = new VariableDeclaration("ranma", value);
        program.add(varDecl);
        // print("The protagonist is #{ranma}")
        FunctionCall functionCall = new FunctionCall("print", ImmutableList.of(new ActualParam(new StringLiteral("The protagonist is #{ranma}"))));
        program.add(new ExpressionStatement(functionCall));
        turinFile.add(program);

        return turinFile;
    }

    @Test
    public void convertBasicMangaExample() throws IOException {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("basicManga.to");
        TurinParser.TurinFileContext root = new Parser().produceParseTree(inputStream);
        assertEquals(basicMangaAst(), new ParseTreeToAst().toAst(root));
    }

    @Test
    public void convertMangaExample() throws IOException {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("manga.to");
        TurinParser.TurinFileContext root = new Parser().produceParseTree(inputStream);
        assertEquals(mangaAst(), new ParseTreeToAst().toAst(root));
    }

}
