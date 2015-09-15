package me.tomassetti.turin.parser;

import com.google.common.collect.ImmutableList;
import me.tomassetti.parser.antlr.TurinParser;
import me.tomassetti.turin.implicit.BasicTypeUsage;
import me.tomassetti.turin.parser.ast.*;
import me.tomassetti.turin.parser.ast.expressions.*;
import me.tomassetti.turin.parser.ast.expressions.literals.BooleanLiteral;
import me.tomassetti.turin.parser.ast.expressions.literals.IntLiteral;
import me.tomassetti.turin.parser.ast.expressions.literals.StringLiteral;
import me.tomassetti.turin.parser.ast.imports.AllPackageImportDeclaration;
import me.tomassetti.turin.parser.ast.imports.ImportDeclaration;
import me.tomassetti.turin.parser.ast.imports.SingleFieldImportDeclaration;
import me.tomassetti.turin.parser.ast.imports.TypeImportDeclaration;
import me.tomassetti.turin.parser.ast.statements.*;
import me.tomassetti.turin.parser.ast.typeusage.PrimitiveTypeUsage;
import me.tomassetti.turin.parser.ast.typeusage.ReferenceTypeUsage;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;
import me.tomassetti.turin.parser.ast.typeusage.VoidTypeUsage;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class ParseTreeToAst {

    private Position getPosition(ParserRuleContext ctx) {
        return new Position(getStartPoint(ctx.start), getEndPoint(ctx.stop));
    }

    private void getPositionFrom(Node node, ParserRuleContext ctx) {
        node.setPosition(getPosition(ctx));
    }

    private Point getStartPoint(Token token) {
        return new Point(token.getLine(), token.getCharPositionInLine());
    }

    private Point getEndPoint(Token token) {
        return new Point(token.getLine(), token.getCharPositionInLine() + token.getText().length());
    }

    public TurinFile toAst(TurinParser.TurinFileContext turinFileContext){
        TurinFile turinFile = new TurinFile();
        getPositionFrom(turinFile, turinFileContext);
        turinFile.setNameSpace(toAst(turinFileContext.namespace));
        for (TurinParser.FileMemberContext memberCtx : turinFileContext.fileMember()) {
            Node memberNode = toAst(memberCtx);
            if (memberNode instanceof TurinTypeDefinition) {
                turinFile.add((TurinTypeDefinition)memberNode);
            } else if (memberNode instanceof PropertyDefinition) {
                turinFile.add((PropertyDefinition) memberNode);
            } else if (memberNode instanceof Program) {
                turinFile.add((Program) memberNode);
            } else if (memberNode instanceof FunctionDefinition) {
                turinFile.add((FunctionDefinition)memberNode);
            } else {
                throw new UnsupportedOperationException(memberNode.getClass().getCanonicalName());
            }
        }
        for (TurinParser.ImportDeclarationContext importDeclarationContext : turinFileContext.importDeclaration()) {
            turinFile.add(toAst(importDeclarationContext));
        }
        return turinFile;
    }

    private ImportDeclaration toAst(TurinParser.ImportDeclarationContext importDeclarationContext) {
        if (importDeclarationContext.allFieldsImportDeclaration() != null) {
            throw new UnsupportedOperationException();
        } else if (importDeclarationContext.singleFieldImportDeclaration() != null) {
            return toAst(importDeclarationContext.singleFieldImportDeclaration());
        } else if (importDeclarationContext.typeImportDeclaration() != null) {
            return toAst(importDeclarationContext.typeImportDeclaration());
        } else if (importDeclarationContext.allPackageImportDeclaration() != null) {
            return new AllPackageImportDeclaration(toAst(importDeclarationContext.allPackageImportDeclaration().packagePart));
        } else {
            throw new UnsupportedOperationException(importDeclarationContext.toString());
        }
    }

    private ImportDeclaration toAst(TurinParser.TypeImportDeclarationContext ctx) {
        if (ctx.alternativeName == null) {
            return new TypeImportDeclaration(toAst(ctx.packagePart), ctx.typeName.getText());
        } else {
            return new TypeImportDeclaration(toAst(ctx.packagePart), ctx.typeName.getText(), ctx.alternativeName.getText());
        }
    }

    private ImportDeclaration toAst(TurinParser.SingleFieldImportDeclarationContext ctx) {
        if (ctx.alternativeName == null) {
            return new SingleFieldImportDeclaration(toAst(ctx.packagePart), ctx.typeName.getText(), toAst(ctx.fieldName));
        } else {
            return new SingleFieldImportDeclaration(toAst(ctx.packagePart), ctx.typeName.getText(), toAst(ctx.fieldName), ctx.alternativeName.getText());
        }
    }

    private QualifiedName toAst(TurinParser.QualifiedIdContext qualifiedIdContext) {
        return QualifiedName.create(qualifiedIdContext.parts.stream().map((p)->p.getText()).collect(Collectors.toList()));
    }

    private Node toAst(TurinParser.FileMemberContext memberCtx) {
        if (memberCtx.typeDeclaration() != null) {
            return toAst(memberCtx.typeDeclaration());
        } else if (memberCtx.topLevelPropertyDeclaration() != null) {
            return toAst(memberCtx.topLevelPropertyDeclaration());
        } else if (memberCtx.program() != null) {
            return toAst(memberCtx.program());
        } else if (memberCtx.topLevelFunctionDeclaration() != null) {
            return toAst(memberCtx.topLevelFunctionDeclaration());
        } else {
            throw new UnsupportedOperationException(memberCtx.toString());
        }
    }

    private Node toAst(TurinParser.TopLevelFunctionDeclarationContext ctx) {
        List<FormalParameter> params = ctx.params.stream().map((p) -> toAst(p)).collect(Collectors.toList());
        return new FunctionDefinition(ctx.name.getText(), toAst(ctx.type), params, toAst(ctx.methodBody()));
    }

    private Node toAst(TurinParser.TopLevelPropertyDeclarationContext topLevelPropertyDeclarationContext) {
        PropertyDefinition propertyDefinition = new PropertyDefinition(topLevelPropertyDeclarationContext.name.getText(), toAst(topLevelPropertyDeclarationContext.type));
        return propertyDefinition;
    }

    private TurinTypeDefinition toAst(TurinParser.TypeDeclarationContext typeDeclarationContext) {
        TurinTypeDefinition typeDefinition = new TurinTypeDefinition(typeDeclarationContext.name.getText());
        getPositionFrom(typeDefinition, typeDeclarationContext);
        for (TurinParser.TypeMemberContext memberCtx : typeDeclarationContext.typeMember()) {
            Node memberNode = toAst(memberCtx);
            if (memberNode instanceof PropertyReference) {
                typeDefinition.add((PropertyReference)memberNode);
            } else if (memberNode instanceof PropertyDefinition) {
                typeDefinition.add((PropertyDefinition) memberNode);
            } else if (memberNode instanceof MethodDefinition) {
                typeDefinition.add((MethodDefinition) memberNode);
            } else {
                throw new UnsupportedOperationException();
            }
        }
        return typeDefinition;
    }

    private Node toAst(TurinParser.TypeMemberContext memberCtx) {
        if (memberCtx.inTypePropertyDeclaration() != null) {
            return toAst(memberCtx.inTypePropertyDeclaration());
        } else if (memberCtx.propertyReference() != null) {
            return toAst(memberCtx.propertyReference());
        } else if (memberCtx.methodDefinition() != null) {
            return toAst(memberCtx.methodDefinition());
        } else {
            throw new UnsupportedOperationException(memberCtx.getClass().getCanonicalName());
        }
    }

    private Node toAst(TurinParser.MethodDefinitionContext methodDefinitionContext) {
        List<FormalParameter> params = methodDefinitionContext.params.stream().map((p) -> toAst(p)).collect(Collectors.toList());
        return new MethodDefinition(methodDefinitionContext.name.getText(), toAst(methodDefinitionContext.type), params, toAst(methodDefinitionContext.methodBody()));
    }

    private Statement toAst(TurinParser.MethodBodyContext methodBodyContext) {
        if (methodBodyContext.expression() != null) {
            ReturnStatement returnStatement = new ReturnStatement(toAst(methodBodyContext.expression()));
            return new BlockStatement(ImmutableList.of(returnStatement));
        } else if (methodBodyContext.statements != null) {
            return new BlockStatement(methodBodyContext.statements.stream().map((s)->toAst(s)).collect(Collectors.toList()));
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private FormalParameter toAst(TurinParser.FormalParamContext formalParamContext) {
        return new FormalParameter(toAst(formalParamContext.type), formalParamContext.name.getText());
    }

    private TypeUsage toAst(TurinParser.ReturnTypeContext type) {
        if (type.isVoid != null) {
            return new VoidTypeUsage();
        } else if (type.type != null) {
            return toAst(type.type);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private Node toAst(TurinParser.InTypePropertyDeclarationContext inTypePropertyDeclarationContext) {
        PropertyDefinition propertyDefinition = new PropertyDefinition(inTypePropertyDeclarationContext.name.getText(), toAst(inTypePropertyDeclarationContext.type));
        return propertyDefinition;
    }

    private PropertyReference toAst(TurinParser.PropertyReferenceContext propertyReferenceContext) {
        return new PropertyReference(propertyReferenceContext.name.getText());
    }

    private TypeUsage toAst(TurinParser.TypeUsageContext type) {
        if (type.ref != null) {
            return new ReferenceTypeUsage(type.ref.getText());
        } else if (type.primitiveType != null) {
            return PrimitiveTypeUsage.getByName(type.primitiveType.getText());
        } else if (type.basicType != null) {
            return BasicTypeUsage.getByName(type.basicType.getText());
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private Node toAst(TurinParser.ProgramContext programCtx) {
        List<Statement> statements = new ArrayList<>();
        for (TurinParser.StatementContext stmtCtx : programCtx.statements) {
            statements.add(toAst(stmtCtx));
        }
        Program program = new Program(programCtx.name.getText(), new BlockStatement(statements), programCtx.formalParam.name.getText());
        return program;
    }

    private Statement toAst(TurinParser.StatementContext stmtCtx) {
        if (stmtCtx.expressionStmt() != null) {
            return toAst(stmtCtx.expressionStmt());
        } else if (stmtCtx.varDecl() != null) {
            return toAst(stmtCtx.varDecl());
        } else if (stmtCtx.ifStmt() != null) {
            return toAst(stmtCtx.ifStmt());
        } else if (stmtCtx.returnStmt() != null) {
            return toAst(stmtCtx.returnStmt());
        } else if (stmtCtx.throwStmt() != null) {
            return toAst(stmtCtx.throwStmt());
        } else {
            throw new UnsupportedOperationException(stmtCtx.getText());
        }
    }

    private ThrowStatement toAst(TurinParser.ThrowStmtContext ctx) {
        ThrowStatement throwStatement = new ThrowStatement(toAst(ctx.expression()));
        getPositionFrom(throwStatement, ctx);
        return throwStatement;
    }

    private Statement toAst(TurinParser.ReturnStmtContext returnStmtContext) {
        if (returnStmtContext.value == null) {
            return new ReturnStatement();
        } else {
            return new ReturnStatement(toAst(returnStmtContext.value));
        }
    }

    private Statement toAst(TurinParser.IfStmtContext ctx) {
        BlockStatement ifBody = new BlockStatement(ctx.ifBody.stream().map((s)->toAst(s)).collect(Collectors.toList()));
        List<ElifClause> elifs = ctx.elifs.stream().map((s) -> toAst(s)).collect(Collectors.toList());
        if (ctx.elseBody == null) {
            return new IfStatement(toAst(ctx.condition), ifBody, elifs);
        } else {
            BlockStatement elseBody = new BlockStatement(ctx.elseBody.stream().map((s)->toAst(s)).collect(Collectors.toList()));
            return new IfStatement(toAst(ctx.condition), ifBody, elifs, elseBody);
        }
    }

    private ElifClause toAst(TurinParser.ElifStmtContext ctx) {
        BlockStatement body = new BlockStatement(ctx.body.stream().map((s)->toAst(s)).collect(Collectors.toList()));
        return new ElifClause(toAst(ctx.condition), body);
    }

    private VariableDeclaration toAst(TurinParser.VarDeclContext varDeclContext) {
        if (varDeclContext.type != null) {
            return new VariableDeclaration(varDeclContext.name.getText(), toAst(varDeclContext.value), toAst(varDeclContext.type));
        } else {
            return new VariableDeclaration(varDeclContext.name.getText(), toAst(varDeclContext.value));
        }
    }

    private Expression toAst(TurinParser.ExpressionContext exprCtx) {
        if (exprCtx.basicExpression() != null) {
            return toAst(exprCtx.basicExpression());
        } else if (exprCtx.creation() != null) {
            return toAst(exprCtx.creation());
        } else if (exprCtx.invokation() != null) {
            return toAst(exprCtx.invokation());
        } else if (exprCtx.mathOperator != null) {
            return mathOperationToAst(exprCtx.mathOperator.getText(), exprCtx.left, exprCtx.right);
        } else if (exprCtx.logicOperator != null) {
            return logicOperationToAst(exprCtx.logicOperator.getText(), exprCtx.left, exprCtx.right);
        } else if (exprCtx.not != null) {
            return new NotOperation(toAst(exprCtx.value));
        } else if (exprCtx.relOp != null) {
            return relationalOperationToAst(exprCtx.relOp.getText(), exprCtx.left, exprCtx.right);
        } else {
            throw new UnsupportedOperationException(exprCtx.getText());
        }
    }

    private Expression mathOperationToAst(String operatorStr, TurinParser.ExpressionContext left, TurinParser.ExpressionContext right) {
        Expression leftExpr = toAst(left);
        Expression rightExpr = toAst(right);
        MathOperation.Operator operator = MathOperation.Operator.fromSymbol(operatorStr);
        return new MathOperation(operator, leftExpr, rightExpr);
    }

    private Expression logicOperationToAst(String operatorStr, TurinParser.ExpressionContext left, TurinParser.ExpressionContext right) {
        Expression leftExpr = toAst(left);
        Expression rightExpr = toAst(right);
        LogicOperation.Operator operator = LogicOperation.Operator.fromSymbol(operatorStr);
        return new LogicOperation(operator, leftExpr, rightExpr);
    }

    private Expression relationalOperationToAst(String operatorStr, TurinParser.ExpressionContext left, TurinParser.ExpressionContext right) {
        Expression leftExpr = toAst(left);
        Expression rightExpr = toAst(right);
        RelationalOperation.Operator operator = RelationalOperation.Operator.fromSymbol(operatorStr);
        return new RelationalOperation(operator, leftExpr, rightExpr);
    }

    private Expression toAst(TurinParser.BasicExpressionContext exprCtx) {
        if (exprCtx.valueReference() != null) {
            return toAst(exprCtx.valueReference());
        } else if (exprCtx.interpolatedStringLiteral() != null) {
            return toAst(exprCtx.interpolatedStringLiteral());
        } else if (exprCtx.intLiteral() != null) {
            return toAst(exprCtx.intLiteral());
        } else if (exprCtx.parenExpression() != null) {
            return toAst(exprCtx.parenExpression().internal);
        } else if (exprCtx.stringLiteral() != null) {
            return toAst(exprCtx.stringLiteral());
        } else if (exprCtx.booleanLiteral() != null) {
            return new BooleanLiteral(exprCtx.booleanLiteral().positive != null);
        } else if (exprCtx.staticFieldReference() != null) {
            return toAst(exprCtx.staticFieldReference());
        } else {
            throw new UnsupportedOperationException(exprCtx.getText());
        }
    }

    private Expression toAst(TurinParser.StaticFieldReferenceContext ctx) {
        return new StaticFieldAccess(toAst(ctx.typeReference()), ctx.name.getText());
    }

    private TypeIdentifier toAst(TurinParser.TypeReferenceContext ctx) {
        if (ctx.packag != null) {
            return new TypeIdentifier(toAst(ctx.packag), ctx.name.getText());
        } else {
            return new TypeIdentifier(ctx.name.getText());
        }
    }

    private ValueReference toAst(TurinParser.ValueReferenceContext valueReferenceContext) {
        ValueReference expression = new ValueReference(valueReferenceContext.getText());
        getPositionFrom(expression, valueReferenceContext);
        return expression;
    }

    private Expression toAst(TurinParser.InterpolatedStringLiteralContext interpolatedStringLiteralContext) {
        StringInterpolation stringInterpolation = new StringInterpolation();
        for (TurinParser.StringElementContext element :interpolatedStringLiteralContext.elements){
            if (element.stringInterpolationElement() != null) {
                stringInterpolation.add(toAst(element.stringInterpolationElement()));
            } else if (element.STRING_CONTENT() != null) {
                stringInterpolation.add(new StringLiteral(element.STRING_CONTENT().getText()));
            } else {
                throw new UnsupportedOperationException();
            }
        }
        getPositionFrom(stringInterpolation, interpolatedStringLiteralContext);
        return stringInterpolation;
    }

    private Expression toAst(TurinParser.StringInterpolationElementContext stringInterpolationElementContext) {
        return toAst(stringInterpolationElementContext.expression());
    }

    private Creation toAst(TurinParser.CreationContext creation) {
        String name;
        if (creation.pakage != null) {
            name = toAst(creation.pakage).qualifiedName() + "." + creation.name.getText();
        } else {
            name = creation.name.getText();
        }
        return new Creation(name, creation.actualParam().stream().map((apCtx)->toAst(apCtx)).collect(Collectors.toList()));
    }

    private ActualParam toAst(TurinParser.ActualParamContext apCtx) {
        if (apCtx.name != null) {
            return new ActualParam(apCtx.name.getText(), toAst(apCtx.expression()));
        } else {
            return new ActualParam(toAst(apCtx.expression()));
        }
    }

    private FunctionCall toAst(TurinParser.InvokationContext functionCallContext) {
        return new FunctionCall(toAst(functionCallContext.basicExpression()), functionCallContext.actualParam().stream().map((apCtx)->toAst(apCtx)).collect(Collectors.toList()));
    }

    private IntLiteral toAst(TurinParser.IntLiteralContext intLiteralContext) {
        return new IntLiteral(Integer.parseInt(intLiteralContext.getText()));
    }

    private StringLiteral toAst(TurinParser.StringLiteralContext stringLiteralContext) {
        String content = stringLiteralContext.getText().substring(1, stringLiteralContext.getText().length() - 1);
        StringLiteral stringLiteral = new StringLiteral(content);
        getPositionFrom(stringLiteral, stringLiteralContext);
        return stringLiteral;
    }

    private ExpressionStatement toAst(TurinParser.ExpressionStmtContext expressionStmtContext) {
        return new ExpressionStatement(toAst(expressionStmtContext.expression()));
    }

    private NamespaceDefinition toAst(TurinParser.NamespaceDeclContext namespaceContext) {
        return new NamespaceDefinition(namespaceContext.name.getText());
    }
}
