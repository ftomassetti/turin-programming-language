package me.tomassetti.turin.parser;

import com.google.common.collect.ImmutableList;
import me.tomassetti.parser.antlr.TurinParser;
import me.tomassetti.turin.implicit.BasicTypeUsage;
import me.tomassetti.turin.parser.ast.*;
import me.tomassetti.turin.parser.ast.annotations.AnnotationUsage;
import me.tomassetti.turin.parser.ast.expressions.*;
import me.tomassetti.turin.parser.ast.expressions.literals.*;
import me.tomassetti.turin.parser.ast.imports.*;
import me.tomassetti.turin.parser.ast.relations.RelationDefinition;
import me.tomassetti.turin.parser.ast.relations.RelationFieldDefinition;
import me.tomassetti.turin.parser.ast.statements.*;
import me.tomassetti.turin.parser.ast.typeusage.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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

    public TurinFile toAst(TurinParser.TurinFileContext ctx){
        TurinFile turinFile = new TurinFile();
        getPositionFrom(turinFile, ctx);
        turinFile.setNameSpace(toAst(ctx.namespace));
        for (TurinParser.FileMemberContext memberCtx : ctx.fileMember()) {
            Node memberNode = toAst(memberCtx);
            if (memberNode instanceof TurinTypeDefinition) {
                turinFile.add((TurinTypeDefinition)memberNode);
            } else if (memberNode instanceof PropertyDefinition) {
                turinFile.add((PropertyDefinition) memberNode);
            } else if (memberNode instanceof Program) {
                turinFile.add((Program) memberNode);
            } else if (memberNode instanceof FunctionDefinition) {
                turinFile.add((FunctionDefinition)memberNode);
            } else if (memberNode instanceof RelationDefinition) {
                turinFile.add((RelationDefinition)memberNode);
            } else {
                throw new UnsupportedOperationException(memberNode.getClass().getCanonicalName());
            }
        }
        for (TurinParser.ImportDeclarationContext importDeclarationContext : ctx.importDeclaration()) {
            turinFile.add(toAst(importDeclarationContext));
        }
        return turinFile;
    }

    private ImportDeclaration toAst(TurinParser.ImportDeclarationContext ctx) {
        if (ctx.allFieldsImportDeclaration() != null) {
            return toAst(ctx.allFieldsImportDeclaration());
        } else if (ctx.singleFieldImportDeclaration() != null) {
            return toAst(ctx.singleFieldImportDeclaration());
        } else if (ctx.typeImportDeclaration() != null) {
            return toAst(ctx.typeImportDeclaration());
        } else if (ctx.allPackageImportDeclaration() != null) {
            AllPackageImportDeclaration node = new AllPackageImportDeclaration(toAst(ctx.allPackageImportDeclaration().packagePart));
            getPositionFrom(node, ctx);
            return node;
        } else {
            throw new UnsupportedOperationException(ctx.toString());
        }
    }

    private AllFieldsImportDeclaration toAst(TurinParser.AllFieldsImportDeclarationContext ctx) {
        AllFieldsImportDeclaration node = new AllFieldsImportDeclaration(toAst(ctx.packagePart), idText(ctx.typeName));
        getPositionFrom(node, ctx);
        return node;
    }

    private ImportDeclaration toAst(TurinParser.TypeImportDeclarationContext ctx) {
        ImportDeclaration importDeclaration;
        if (ctx.alternativeName == null) {
             importDeclaration = new TypeImportDeclaration(toAst(ctx.packagePart), idText(ctx.typeName));
        } else {
            importDeclaration = new TypeImportDeclaration(toAst(ctx.packagePart), idText(ctx.typeName), idText(ctx.alternativeName));
        }
        getPositionFrom(importDeclaration, ctx);
        return importDeclaration;
    }

    private ImportDeclaration toAst(TurinParser.SingleFieldImportDeclarationContext ctx) {
        ImportDeclaration importDeclaration;
        if (ctx.alternativeName == null) {
            importDeclaration = new SingleFieldImportDeclaration(toAst(ctx.packagePart), idText(ctx.typeName), toAst(ctx.fieldName));
        } else {
            importDeclaration = new SingleFieldImportDeclaration(toAst(ctx.packagePart), idText(ctx.typeName), toAst(ctx.fieldName), idText(ctx.alternativeName));
        }
        getPositionFrom(importDeclaration, ctx);
        return importDeclaration;
    }

    private QualifiedName toAst(TurinParser.QualifiedIdContext ctx) {
        QualifiedName qualifiedName = QualifiedName.create(ctx.parts.stream().map((p) -> p.getText()).collect(Collectors.toList()));
        getPositionFrom(qualifiedName, ctx);
        return qualifiedName;
    }

    private Node toAst(TurinParser.FileMemberContext ctx) {
        if (ctx.typeDeclaration() != null) {
            return toAst(ctx.typeDeclaration());
        } else if (ctx.topLevelPropertyDeclaration() != null) {
            return toAst(ctx.topLevelPropertyDeclaration());
        } else if (ctx.program() != null) {
            return toAst(ctx.program());
        } else if (ctx.topLevelFunctionDeclaration() != null) {
            return toAst(ctx.topLevelFunctionDeclaration());
        } else if (ctx.relation() != null) {
            return toAst(ctx.relation());
        } else {
            throw new UnsupportedOperationException(ctx.getClass().getCanonicalName());
        }
    }

    private RelationDefinition toAst(TurinParser.RelationContext ctx) {
        List<RelationFieldDefinition> fields = ctx.relationField().stream().map((fCtx)->toAst(fCtx)).collect(Collectors.toList());
        RelationDefinition relationDefinition = new RelationDefinition(ctx.name.getText(), fields);
        getPositionFrom(relationDefinition, ctx);
        return relationDefinition;
    }

    private RelationFieldDefinition toAst(TurinParser.RelationFieldContext ctx) {
        RelationFieldDefinition.Cardinality cardinality = null;
        if (ctx.one != null) {
            cardinality = RelationFieldDefinition.Cardinality.SINGLE;
        } else if (ctx.many != null) {
            cardinality = RelationFieldDefinition.Cardinality.MANY;
        } else {
            throw new UnsupportedOperationException();
        }
        RelationFieldDefinition relationFieldDefinition = new RelationFieldDefinition(cardinality, ctx.name.getText(), toAst(ctx.type));
        getPositionFrom(relationFieldDefinition, ctx);
        return relationFieldDefinition;
    }

    private FunctionDefinition toAst(TurinParser.TopLevelFunctionDeclarationContext ctx) {
        List<FormalParameter> params = ctx.params.stream().map((p) -> toAst(p)).collect(Collectors.toList());
        FunctionDefinition functionDefinition = new FunctionDefinition(idText(ctx.name), toAst(ctx.type), params, toAst(ctx.methodBody()));
        getPositionFrom(functionDefinition, ctx);
        ctx.annotations.forEach((anCtx)->{
            AnnotationUsage annotationUsage = toAst(anCtx);
            functionDefinition.addAnnotation(annotationUsage);
        });
        return functionDefinition;
    }

    private AnnotationUsage toAst(TurinParser.AnnotationUsageContext ctx) {
        // we skip the @ character
        AnnotationUsage annotationUsage = new AnnotationUsage(ctx.annotation.getText().substring(1));
        getPositionFrom(annotationUsage, ctx);
        return annotationUsage;
    }

    private PropertyDefinition toAst(TurinParser.TopLevelPropertyDeclarationContext ctx) {
        Optional<Expression> initialValue = ctx.initialValue == null ? Optional.empty() : Optional.of(toAst(ctx.initialValue));
        Optional<Expression> defaultValue = ctx.defaultValue == null ? Optional.empty() : Optional.of(toAst(ctx.defaultValue));
        List<PropertyConstraint> constraints = Collections.emptyList();
        PropertyDefinition propertyDefinition = new PropertyDefinition(idText(ctx.name), toAst(ctx.type), initialValue, defaultValue, constraints);
        getPositionFrom(propertyDefinition, ctx);
        return propertyDefinition;
    }

    private TurinTypeDefinition toAst(TurinParser.TypeDeclarationContext ctx) {
        TurinTypeDefinition typeDefinition = new TurinTypeDefinition(idText(ctx.name));
        getPositionFrom(typeDefinition, ctx);
        for (TurinParser.TypeMemberContext memberCtx : ctx.typeMember()) {
            Node memberNode = toAst(memberCtx);
            if (memberNode instanceof PropertyReference) {
                typeDefinition.add((PropertyReference)memberNode);
            } else if (memberNode instanceof PropertyDefinition) {
                typeDefinition.add((PropertyDefinition) memberNode);
            } else if (memberNode instanceof TurinTypeMethodDefinition) {
                typeDefinition.add((TurinTypeMethodDefinition) memberNode);
            } else if (memberNode instanceof TurinTypeContructorDefinition) {
                typeDefinition.add((TurinTypeContructorDefinition) memberNode);
            } else {
                throw new UnsupportedOperationException();
            }
        }
        ctx.annotations.forEach((anCtx)->{
            AnnotationUsage annotationUsage = toAst(anCtx);
            typeDefinition.addAnnotation(annotationUsage);
        });
        if (ctx.baseType != null) {
            typeDefinition.setBaseType(toAst(ctx.baseType));
        }
        ctx.interfaze.forEach((iCtx)->typeDefinition.addInterface(toAst(iCtx)));
        return typeDefinition;
    }

    private Node toAst(TurinParser.TypeMemberContext ctx) {
        if (ctx.inTypePropertyDeclaration() != null) {
            return toAst(ctx.inTypePropertyDeclaration());
        } else if (ctx.propertyReference() != null) {
            return toAst(ctx.propertyReference());
        } else if (ctx.methodDefinition() != null) {
            return toAst(ctx.methodDefinition());
        } else if (ctx.constructorDefinition() != null) {
            return toAst(ctx.constructorDefinition());
        } else {
            throw new UnsupportedOperationException(ctx.getClass().getCanonicalName());
        }
    }

    private Node toAst(TurinParser.MethodDefinitionContext ctx) {
        List<FormalParameter> params = ctx.params.stream().map((p) -> toAst(p)).collect(Collectors.toList());
        TurinTypeMethodDefinition methodDefinition = new TurinTypeMethodDefinition(idText(ctx.name), toAst(ctx.type), params, toAst(ctx.methodBody()));
        getPositionFrom(methodDefinition, ctx);
        return methodDefinition;
    }

    private Node toAst(TurinParser.ConstructorDefinitionContext ctx) {
        List<FormalParameter> params = ctx.params.stream().map((p) -> toAst(p)).collect(Collectors.toList());
        List<ActualParam> superParams = ctx.superParams.stream().map((p) -> toAst(p)).collect(Collectors.toList());
        List<Statement> bodyStatements = ctx.statements.stream().map((s) -> toAst(s)).collect(Collectors.toList());
        bodyStatements.add(new ExpressionStatement(new SuperInvokation(superParams)));
        BlockStatement body = new BlockStatement(bodyStatements);
        TurinTypeContructorDefinition constructorDefinition = new TurinTypeContructorDefinition(params, body);
        getPositionFrom(constructorDefinition, ctx);
        return constructorDefinition;
    }

    private Statement toAst(TurinParser.MethodBodyContext ctx) {
        if (ctx.expression() != null) {
            ReturnStatement returnStatement = new ReturnStatement(toAst(ctx.expression()));
            return new BlockStatement(ImmutableList.of(returnStatement));
        } else if (ctx.statements != null) {
            return new BlockStatement(ctx.statements.stream().map((s)->toAst(s)).collect(Collectors.toList()));
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private FormalParameter toAst(TurinParser.FormalParamContext ctx) {
        FormalParameter formalParameter = new FormalParameter(toAst(ctx.type), idText(ctx.name));
        getPositionFrom(formalParameter, ctx);
        return formalParameter;
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

    private Node toAst(TurinParser.InTypePropertyDeclarationContext ctx) {
        Optional<Expression> initialValue = ctx.initialValue == null ? Optional.empty() : Optional.of(toAst(ctx.initialValue));
        Optional<Expression> defaultValue = ctx.defaultValue == null ? Optional.empty() : Optional.of(toAst(ctx.defaultValue));
        List<PropertyConstraint> constraints = ctx.constraint.stream().map((cctx)->toAst(cctx)).collect(Collectors.toList());
        PropertyDefinition propertyDefinition = new PropertyDefinition(
                idText(ctx.name), toAst(ctx.type),
                initialValue, defaultValue, constraints);
        getPositionFrom(propertyDefinition, ctx);
        return propertyDefinition;
    }

    private PropertyConstraint toAst(TurinParser.ConstraintDeclarationContext ctx) {
        Expression condition = toAst(ctx.condition);
        Expression message = ctx.message == null ? new StringLiteral("Condition violated: " + ctx.condition.getText()) : toAst(ctx.message);
        PropertyConstraint propertyConstraint = new PropertyConstraint(condition, message);
        getPositionFrom(propertyConstraint, ctx);
        return propertyConstraint;
    }

    private PropertyReference toAst(TurinParser.PropertyReferenceContext propertyReferenceContext) {
        return new PropertyReference(idText(propertyReferenceContext.name));
    }

    private TypeUsage toAst(TurinParser.TypeUsageContext type) {
        if (type.ref != null) {
            ReferenceTypeUsage referenceTypeUsage = new ReferenceTypeUsage(type.ref.getText());
            getPositionFrom(referenceTypeUsage, type);
            return referenceTypeUsage;
        } else if (type.primitiveType != null) {
            return PrimitiveTypeUsage.getByName(type.primitiveType.getText());
        } else if (type.basicType != null) {
            return BasicTypeUsage.getByName(type.basicType.getText());
        } else if (type.arrayBase != null) {
            return new ArrayTypeUsage(toAst(type.arrayBase));
        } else {
            throw new UnsupportedOperationException(type.getText());
        }
    }

    private Node toAst(TurinParser.ProgramContext programCtx) {
        List<Statement> statements = new ArrayList<>();
        for (TurinParser.StatementContext stmtCtx : programCtx.statements) {
            statements.add(toAst(stmtCtx));
        }
        Program program = new Program(idText(programCtx.name), new BlockStatement(statements), idText(programCtx.formalParam.name));
        getPositionFrom(program, programCtx);
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
        } else if (stmtCtx.tryCatchStmt() != null) {
            return toAst(stmtCtx.tryCatchStmt());
        } else {
            throw new UnsupportedOperationException(stmtCtx.getText());
        }
    }

    private Expression toAst(TurinParser.AssignmentContext ctx) {
        AssignmentExpression node = new AssignmentExpression(toAst(ctx.target), toAst(ctx.value));
        getPositionFrom(node, ctx);
        return node;
    }

    private TryCatchStatement toAst(TurinParser.TryCatchStmtContext ctx) {
        BlockStatement body = new BlockStatement(ctx.body.stream().map((s)->toAst(s)).collect(Collectors.toList()));
        List<CatchClause> catches = ctx.catches.stream().map((cc) -> toAst(cc)).collect(Collectors.toList());
        TryCatchStatement tryCatchStatement = new TryCatchStatement(body, catches);
        getPositionFrom(tryCatchStatement, ctx);
        return tryCatchStatement;
    }

    private CatchClause toAst(TurinParser.CatchClauseContext ctx) {
        BlockStatement body = new BlockStatement(ctx.body.stream().map((s)->toAst(s)).collect(Collectors.toList()));
        TypeIdentifier type = toAst(ctx.type);
        CatchClause catchCause = new CatchClause(type, idText(ctx.varName), body);
        getPositionFrom(catchCause, ctx);
        return catchCause;
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
            return new VariableDeclaration(idText(varDeclContext.name), toAst(varDeclContext.value), toAst(varDeclContext.type));
        } else {
            return new VariableDeclaration(idText(varDeclContext.name), toAst(varDeclContext.value));
        }
    }

    private Expression toAst(TurinParser.ExpressionContext exprCtx) {
        if (exprCtx.basicExpression() != null) {
            return toAst(exprCtx.basicExpression());
        } else if (exprCtx.creation() != null) {
            return toAst(exprCtx.creation());
        } else if (exprCtx.function != null) {
            return toAstFunctionCall(exprCtx);
        } else if (exprCtx.mathOperator != null) {
            return mathOperationToAst(exprCtx.mathOperator.getText(), exprCtx.left, exprCtx.right);
        } else if (exprCtx.logicOperator != null) {
            return logicOperationToAst(exprCtx.logicOperator.getText(), exprCtx.left, exprCtx.right);
        } else if (exprCtx.not != null) {
            return new NotOperation(toAst(exprCtx.value));
        } else if (exprCtx.relOp != null) {
            return relationalOperationToAst(exprCtx.relOp.getText(), exprCtx.left, exprCtx.right, getPosition(exprCtx));
        } else if (exprCtx.array != null) {
            return new ArrayAccess(toAst(exprCtx.array), toAst(exprCtx.index));
        } else if (exprCtx.fieldName != null) {
            return toInstanceFieldAccessAst(exprCtx);
        } else if (exprCtx.methodName != null) {
            return toInstanceMethodAccessAst(exprCtx);
        } else if (exprCtx.isAssignment !=null) {
            AssignmentExpression assignmentStatement = new AssignmentExpression(toAst(exprCtx.left), toAst(exprCtx.right));
            getPositionFrom(assignmentStatement, exprCtx);
            return assignmentStatement;
        } else if (exprCtx.relationSubset() != null) {
            return toAst(exprCtx.relationSubset());
        } else {
            throw new UnsupportedOperationException("Enable to produce ast for " + exprCtx.getText());
        }
    }

    private Expression toAst(TurinParser.RelationSubsetContext ctx) {
        RelationSubset relationSubset = new RelationSubset(ctx.relationName.getText(), ctx.field.getText(),
                ctx.actualParam().stream()
                        .map((apCtx) -> toAst(apCtx))
                        .collect(Collectors.toList()));
        getPositionFrom(relationSubset, ctx);
        return relationSubset;
    }

    private InstanceMethodInvokation toInstanceMethodAccessAst(TurinParser.ExpressionContext ctx) {
        List<ActualParam> params = ctx.actualParam().stream().map((apCtx)->toAst(apCtx)).collect(Collectors.toList());
        InstanceMethodInvokation instanceMethodInvokation = new InstanceMethodInvokation(toAst(ctx.container), idText(ctx.methodName), params);
        getPositionFrom(instanceMethodInvokation, ctx);
        return instanceMethodInvokation;
    }

    private InstanceFieldAccess toInstanceFieldAccessAst(TurinParser.ExpressionContext ctx) {
        InstanceFieldAccess instanceFieldAccess = new InstanceFieldAccess(toAst(ctx.container), idText(ctx.fieldName));
        getPositionFrom(instanceFieldAccess, ctx);
        return instanceFieldAccess;
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

    private Expression relationalOperationToAst(String operatorStr, TurinParser.ExpressionContext left, TurinParser.ExpressionContext right, Position position) {
        Expression leftExpr = toAst(left);
        Expression rightExpr = toAst(right);
        RelationalOperation.Operator operator = RelationalOperation.Operator.fromSymbol(operatorStr);
        RelationalOperation relationalOperation = new RelationalOperation(operator, leftExpr, rightExpr);
        relationalOperation.setPosition(position);
        return relationalOperation;
    }

    private Expression toAst(TurinParser.BasicExpressionContext exprCtx) {
        if (exprCtx.valueReference() != null) {
            return toAst(exprCtx.valueReference());
        } else if (exprCtx.interpolatedStringLiteral() != null) {
            return toAst(exprCtx.interpolatedStringLiteral());
        } else if (exprCtx.byteLiteral() != null) {
            return toAst(exprCtx.byteLiteral());
        } else if (exprCtx.shortLiteral() != null) {
            return toAst(exprCtx.shortLiteral());
        } else if (exprCtx.intLiteral() != null) {
            return toAst(exprCtx.intLiteral());
        } else if (exprCtx.longLiteral() != null) {
            return toAst(exprCtx.longLiteral());
        } else if (exprCtx.floatLiteral() != null) {
            return toAst(exprCtx.floatLiteral());
        } else if (exprCtx.doubleLiteral() != null) {
            return toAst(exprCtx.doubleLiteral());
        } else if (exprCtx.parenExpression() != null) {
            return toAst(exprCtx.parenExpression().internal);
        } else if (exprCtx.stringLiteral() != null) {
            return toAst(exprCtx.stringLiteral());
        } else if (exprCtx.booleanLiteral() != null) {
            return new BooleanLiteral(exprCtx.booleanLiteral().positive != null);
        } else if (exprCtx.staticFieldReference() != null) {
            return toAst(exprCtx.staticFieldReference());
        } else if (exprCtx.placeholderUsage() != null) {
            return toAst(exprCtx.placeholderUsage());
        } else if (exprCtx.placeholderNameUsage() != null) {
            return toAst(exprCtx.placeholderNameUsage());
        } else if (exprCtx.thisReference() != null) {
            ThisExpression thisExpression = new ThisExpression();
            getPositionFrom(thisExpression, exprCtx.thisReference());
            return thisExpression;
        } else {
            throw new UnsupportedOperationException(exprCtx.getText());
        }
    }

    private Expression toAst(TurinParser.PlaceholderUsageContext ctx) {
        ParserRuleContext parent = ctx.getParent();
        String fieldName = null;
        while (parent != null && fieldName == null) {
            if (parent instanceof TurinParser.InTypePropertyDeclarationContext) {
                fieldName = idText(((TurinParser.InTypePropertyDeclarationContext)parent).name);
            } else {
                parent = parent.getParent();
            }
        }
        if (fieldName == null) {
            return new SemanticError("A placeholder should be used only inside in-type property declarations", getPosition(ctx));
        } else {
            Placeholder placeholder = new Placeholder();
            getPositionFrom(placeholder, ctx);
            return placeholder;
        }
    }

    private Expression toAst(TurinParser.PlaceholderNameUsageContext ctx) {
        ParserRuleContext parent = ctx.getParent();
        String fieldName = null;
        while (parent != null && fieldName == null) {
            if (parent instanceof TurinParser.InTypePropertyDeclarationContext) {
                fieldName = idText(((TurinParser.InTypePropertyDeclarationContext)parent).name);
            } else {
                parent = parent.getParent();
            }
        }
        if (fieldName == null) {
            return new SemanticError("A placeholder should be used only inside in-type property declarations", getPosition(ctx));
        } else {
            StringLiteral stringLiteral = new StringLiteral(fieldName);
            getPositionFrom(stringLiteral, ctx);
            return stringLiteral;
        }
    }

    private Expression toAst(TurinParser.StaticFieldReferenceContext ctx) {
        StaticFieldAccess staticFieldAccess = new StaticFieldAccess(toAst(ctx.typeReference()), idText(ctx.name));
        getPositionFrom(staticFieldAccess, ctx);
        return staticFieldAccess;
    }

    private TypeIdentifier toAst(TurinParser.TypeReferenceContext ctx) {
        if (ctx.packag != null) {
            return new TypeIdentifier(toAst(ctx.packag), idText(ctx.name));
        } else {
            return new TypeIdentifier(idText(ctx.name));
        }
    }

    private String idText(Token token) {
        if (token.getText().startsWith("v#") || token.getText().startsWith("T#")) {
            return token.getText().substring(2);
        } else {
            return token.getText();
        }
    }

    private ValueReference toAst(TurinParser.ValueReferenceContext valueReferenceContext) {
        ValueReference expression = new ValueReference(idText(valueReferenceContext.name));
        getPositionFrom(expression, valueReferenceContext);
        return expression;
    }

    private String unescape(String s) {
        switch (s) {
            case "\\r":
                return "\r";
            case "\\n":
                return "\n";
            case "\\b":
                return "\b";
            case "\\f":
                return "\f";
            case "\\t":
                return "\t";
            case "\\\\":
                return "\\";
            case "\\\"":
                return "\"";
            default:
                throw new UnsupportedOperationException(s);
        }
    }

    private Expression toAst(TurinParser.InterpolatedStringLiteralContext interpolatedStringLiteralContext) {
        StringInterpolation stringInterpolation = new StringInterpolation();
        for (TurinParser.StringElementContext element :interpolatedStringLiteralContext.elements){
            if (element.stringInterpolationElement() != null) {
                stringInterpolation.add(toAst(element.stringInterpolationElement()));
            } else if (element.STRING_CONTENT() != null) {
                stringInterpolation.add(new StringLiteral(element.STRING_CONTENT().getText()));
            } else if (element.ESCAPE_SEQUENCE() != null) {
                stringInterpolation.add(new StringLiteral(unescape(element.ESCAPE_SEQUENCE().getText())));
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

    private Creation toAst(TurinParser.CreationContext ctx) {
        Creation creation = new Creation(toAst(ctx.ref), ctx.actualParam().stream().map((apCtx)->toAst(apCtx)).collect(Collectors.toList()));
        getPositionFrom(creation, ctx);
        return creation;
    }

    private ActualParam toAst(TurinParser.ActualParamContext apCtx) {
        ActualParam actualParam;
        if (apCtx.name != null) {
            actualParam = new ActualParam(idText(apCtx.name), toAst(apCtx.expression()));
        } else {
            actualParam = new ActualParam(toAst(apCtx.expression()), apCtx.asterisk != null);
        }
        getPositionFrom(actualParam, apCtx);
        return actualParam;
    }

    private FunctionCall toAstFunctionCall(TurinParser.ExpressionContext functionCallContext) {
        Expression function = toAst(functionCallContext.function);
        return new FunctionCall(function, functionCallContext.actualParam().stream().map((apCtx)->toAst(apCtx)).collect(Collectors.toList()));
    }

    private Expression toAst(TurinParser.ByteLiteralContext ctx) {
        try {
            // ignore last character
            String text = ctx.getText().substring(0, ctx.getText().length() - 1);
            BigInteger bigInteger = new BigInteger(text);
            if (bigInteger.compareTo(new BigInteger(Byte.toString(Byte.MAX_VALUE))) == 1
                || bigInteger.compareTo(new BigInteger(Byte.toString(Byte.MIN_VALUE))) == -1) {
                return new SemanticError("Value cannot be contained in the given type", getPosition(ctx));
            }
            ByteLiteral literal = new ByteLiteral(Byte.parseByte(text));
            getPositionFrom(literal, ctx);
            return literal;
        } catch (NumberFormatException e){
            return new SemanticError("Invalid number literal", getPosition(ctx));
        }
    }

    private Expression toAst(TurinParser.ShortLiteralContext ctx) {
        try {
            // ignore last character
            String text = ctx.getText().substring(0, ctx.getText().length() - 1);
            BigInteger bigInteger = new BigInteger(text);
            if (bigInteger.compareTo(new BigInteger(Short.toString(Short.MAX_VALUE))) == 1
                    || bigInteger.compareTo(new BigInteger(Short.toString(Short.MIN_VALUE))) == -1) {
                return new SemanticError("Value cannot be contained in the given type", getPosition(ctx));
            }
            ShortLiteral literal = new ShortLiteral(Short.parseShort(text));
            getPositionFrom(literal, ctx);
            return literal;
        } catch (NumberFormatException e){
            return new SemanticError("Invalid number literal", getPosition(ctx));
        }
    }

    private Expression toAst(TurinParser.IntLiteralContext ctx) {
        try {
            BigInteger bigInteger = new BigInteger(ctx.getText());
            if (bigInteger.compareTo(new BigInteger(Integer.toString(Integer.MAX_VALUE))) == 1
                    || bigInteger.compareTo(new BigInteger(Integer.toString(Integer.MIN_VALUE))) == -1) {
                return new SemanticError("Value cannot be contained in the given type", getPosition(ctx));
            }
            IntLiteral intLiteral = new IntLiteral(Integer.parseInt(ctx.getText()));
            getPositionFrom(intLiteral, ctx);
            return intLiteral;
        } catch (NumberFormatException e){
            return new SemanticError("Invalid number literal", getPosition(ctx));
        }
    }

    private Expression toAst(TurinParser.LongLiteralContext ctx) {
        try {
            // ignore last character
            String text = ctx.getText().substring(0, ctx.getText().length() - 1);
            BigInteger bigInteger = new BigInteger(text);
            if (bigInteger.compareTo(new BigInteger(Long.toString(Long.MAX_VALUE))) == 1
                    || bigInteger.compareTo(new BigInteger(Long.toString(Long.MIN_VALUE))) == -1) {
                return new SemanticError("Value cannot be contained in the given type", getPosition(ctx));
            }
            LongLiteral literal = new LongLiteral(Long.parseLong(text));
            getPositionFrom(literal, ctx);
            return literal;
        } catch (NumberFormatException e){
            return new SemanticError("Invalid number literal", getPosition(ctx));
        }
    }

    private Expression toAst(TurinParser.FloatLiteralContext ctx) {
        try {
            // ignore last character
            String text = ctx.getText().substring(0, ctx.getText().length() - 1);
            FloatLiteral literal = new FloatLiteral(Float.parseFloat(text));
            getPositionFrom(literal, ctx);
            return literal;
        } catch (NumberFormatException e){
            return new SemanticError("Invalid number literal", getPosition(ctx));
        }
    }

    private Expression toAst(TurinParser.DoubleLiteralContext ctx) {
        try {
            DoubleLiteral literal = new DoubleLiteral(Double.parseDouble(ctx.getText()));
            getPositionFrom(literal, ctx);
            return literal;
        } catch (NumberFormatException e){
            return new SemanticError("Invalid number literal", getPosition(ctx));
        }
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
        // in this way we take care of the escaped IDs
        return new NamespaceDefinition(toAst(namespaceContext.name).qualifiedName());
    }
}
