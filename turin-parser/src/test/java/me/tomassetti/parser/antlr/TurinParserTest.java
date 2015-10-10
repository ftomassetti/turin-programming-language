package me.tomassetti.parser.antlr;

import me.tomassetti.turin.parser.InternalParser;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TurinParserTest {

    private TurinParser.TurinFileContext parse(String exampleName) throws IOException {
        InternalParser internalParser = new InternalParser();
        InputStream inputStream = this.getClass().getResourceAsStream("/me/tomassetti/turin/" + exampleName + ".to");
        if (inputStream == null) {
            throw new RuntimeException("Example not found: " + exampleName);
        }
        TurinParser.TurinFileContext turinFileContext = internalParser.produceParseTree(inputStream);
        return  turinFileContext;
    }

    @Test
    public void parseImports() throws IOException {
        TurinParser.TurinFileContext root = parse("imports_example");
        assertEquals(5, root.importDeclaration().size());
    }

    @Test
    public void parseMethodDeclarationWithExpressionBody() throws IOException {
        TurinParser.TurinFileContext root = parse("method_definitions_expression");
    }

    @Test
    public void parseMethodDeclarationWithBlockBody() throws IOException {
        TurinParser.TurinFileContext root = parse("method_definitions_block");
    }

    @Test
    public void parseFunctionDeclarationWithExpressionBody() throws IOException {
        TurinParser.TurinFileContext root = parse("function_definitions_expression");
    }

    @Test
    public void parseFunctionDeclarationWithBlockBody() throws IOException {
        TurinParser.TurinFileContext root = parse("function_definitions_block");
    }

    @Test
    public void parseArgsLengthInRelExpr() throws IOException {
        TurinParser.TurinFileContext root = parse("args_length_different_from_one");
        assertEquals(1, root.fileMember().size());
        TurinParser.TopLevelFunctionDeclarationContext function = root.members.get(0).topLevelFunctionDeclaration();
        TurinParser.ExpressionContext funcBody = function.methodBody().expression();
        assertEquals("!=", funcBody.relOp.getText());
    }

    @Test
    public void parseFieldAccess() throws IOException {
        TurinParser.TurinFileContext root = parse("field_access");
        assertEquals(1, root.fileMember().size());
        TurinParser.TopLevelFunctionDeclarationContext function = root.members.get(0).topLevelFunctionDeclaration();
        TurinParser.ExpressionContext funcBody = function.methodBody().expression();
        assertEquals("length", funcBody.fieldName.getText());
    }

    @Test
    public void parseMethodInvokation() throws IOException {
        TurinParser.TurinFileContext root = parse("method_invokation");
        assertEquals(1, root.fileMember().size());
        TurinParser.TopLevelFunctionDeclarationContext function = root.members.get(0).topLevelFunctionDeclaration();
        TurinParser.ExpressionContext funcBody = function.methodBody().expression();
        assertEquals("length", funcBody.methodName.getText());
    }

    @Test
    public void parseStringInterpolation1() throws IOException {
        TurinParser.TurinFileContext root = parse("string_interp1");
        assertEquals(1, root.fileMember().size());
        TurinParser.TopLevelFunctionDeclarationContext function = root.members.get(0).topLevelFunctionDeclaration();
        TurinParser.ExpressionContext funcBody = function.methodBody().expression();
    }

    @Test
    public void parseFieldConstraint() throws IOException {
        TurinParser.TurinFileContext root = parse("field_constraint");
        assertEquals(1, root.fileMember().size());
        TurinParser.TypeDeclarationContext type = root.members.get(0).typeDeclaration();
        assertEquals(1, type.typeMembers.size());
        TurinParser.TypeMemberContext propMember = type.typeMembers.get(0);
        assertNotNull(propMember.inTypePropertyDeclaration());
        TurinParser.InTypePropertyDeclarationContext prop = propMember.inTypePropertyDeclaration();
        assertEquals(1, prop.constraintDeclaration().size());
        assertNotNull(prop.constraintDeclaration().get(0).message);
        TurinParser.ExpressionContext msg = prop.constraintDeclaration().get(0).message;
    }

    @Test
    public void parseExplicitConstructor() throws IOException {
        TurinParser.TurinFileContext root = parse("explicit_constructor");
        assertEquals(1, root.fileMember().size());
        TurinParser.TypeDeclarationContext type = root.members.get(0).typeDeclaration();
        assertEquals(2, type.typeMembers.size());
        TurinParser.TypeMemberContext propMember = type.typeMembers.get(0);
        assertNotNull(propMember.inTypePropertyDeclaration());
        TurinParser.InTypePropertyDeclarationContext prop = propMember.inTypePropertyDeclaration();
        assertEquals(0, prop.constraintDeclaration().size());
        TurinParser.ConstructorDefinitionContext constructorDefinition = type.typeMembers.get(1).constructorDefinition();
        assertNotNull(constructorDefinition);
        assertEquals(1, constructorDefinition.statement().size());
        assertNotNull(constructorDefinition.statement().get(0).expressionStmt());
        TurinParser.ExpressionStmtContext expressionStmt = constructorDefinition.statement().get(0).expressionStmt();
        assertNotNull(expressionStmt.expression().isAssignment);
    }

    @Test
    public void parseLocalVar() throws IOException {
        TurinParser.TurinFileContext root = parse("local_var");
        assertEquals(1, root.fileMember().size());
        TurinParser.ProgramContext program = root.members.get(0).program();
        assertEquals(1, program.statements.size());
        TurinParser.StatementContext stmt = program.statement().get(0);
        assertNotNull(stmt.varDecl());
        TurinParser.VarDeclContext varDecl = stmt.varDecl();
        assertNotNull(varDecl.value);
        assertNotNull(varDecl.value.creation());
        TurinParser.CreationContext creation = varDecl.value.creation();
        assertEquals(1, creation.actualParam().size());
        TurinParser.ActualParamContext actualParam = creation.actualParam().get(0);
        assertNotNull(actualParam.name);
        assertEquals("newLinesAfterLBracket", actualParam.name.getText());
        assertNotNull(actualParam.expression().basicExpression());
        assertNotNull(actualParam.expression().basicExpression().booleanLiteral());
    }

    @Test
    public void parseRelationWithoutErrors() throws IOException {
        TurinParser.TurinFileContext root = parse("simple_relation");
    }

    @Test
    public void parseRelationSubsetWithoutErrors() throws IOException {
        TurinParser.TurinFileContext root = parse("relation_subset");
    }

}
