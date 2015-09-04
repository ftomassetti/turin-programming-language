package me.tomassetti.turin.parser;

import me.tomassetti.parser.antlr.TurinParser;
import me.tomassetti.turin.parser.ast.*;
import me.tomassetti.turin.parser.ast.expressions.*;
import me.tomassetti.turin.parser.ast.expressions.literals.IntLiteral;
import me.tomassetti.turin.parser.ast.expressions.literals.StringLiteral;
import me.tomassetti.turin.parser.ast.statements.ExpressionStatement;
import me.tomassetti.turin.parser.ast.statements.Statement;
import me.tomassetti.turin.parser.ast.statements.VariableDeclaration;
import me.tomassetti.turin.parser.ast.typeusage.ReferenceTypeUsage;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

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
            } else {
                throw new UnsupportedOperationException();
            }
        }
        return turinFile;
    }

    private Node toAst(TurinParser.FileMemberContext memberCtx) {
        if (memberCtx.typeDeclaration() != null) {
            return toAst(memberCtx.typeDeclaration());
        } else if (memberCtx.topLevelPropertyDeclaration() != null) {
            return toAst(memberCtx.topLevelPropertyDeclaration());
        } else if (memberCtx.program() != null) {
            return toAst(memberCtx.program());
        } else {
            throw new UnsupportedOperationException();
        }
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
                typeDefinition.add((PropertyDefinition)memberNode);
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
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private Node toAst(TurinParser.ProgramContext programCtx) {
        Program program = new Program(programCtx.name.getText());
        for (TurinParser.StatementContext stmtCtx : programCtx.statements) {
            program.add(toAst(stmtCtx));
        }
        return program;
    }

    private Statement toAst(TurinParser.StatementContext stmtCtx) {
        if (stmtCtx.expressionStmt() != null) {
            return toAst(stmtCtx.expressionStmt());
        } else if (stmtCtx.varDecl() != null) {
            return toAst(stmtCtx.varDecl());
        } else {
            throw new UnsupportedOperationException();
        }
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
        } else {
            throw new UnsupportedOperationException(exprCtx.getText());
        }
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
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private Expression toAst(TurinParser.ValueReferenceContext valueReferenceContext) {
        return new ValueReference(valueReferenceContext.getText());
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
        return stringInterpolation;
    }

    private Expression toAst(TurinParser.StringInterpolationElementContext stringInterpolationElementContext) {
        return toAst(stringInterpolationElementContext.expression());
    }

    private Creation toAst(TurinParser.CreationContext creation) {
        return new Creation(creation.name.getText(), creation.actualParam().stream().map((apCtx)->toAst(apCtx)).collect(Collectors.toList()));
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
        return new StringLiteral(content);
    }

    private ExpressionStatement toAst(TurinParser.ExpressionStmtContext expressionStmtContext) {
        return new ExpressionStatement(toAst(expressionStmtContext.expression()));
    }

    private NamespaceDefinition toAst(TurinParser.NamespaceDeclContext namespaceContext) {
        return new NamespaceDefinition(namespaceContext.name.getText());
    }
}
