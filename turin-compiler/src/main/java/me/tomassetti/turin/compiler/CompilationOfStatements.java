package me.tomassetti.turin.compiler;

import com.google.common.collect.ImmutableList;
import me.tomassetti.bytecode_generation.*;
import me.tomassetti.bytecode_generation.pushop.PushStaticField;
import me.tomassetti.bytecode_generation.returnop.ReturnValueBS;
import me.tomassetti.bytecode_generation.returnop.ReturnVoidBS;
import me.tomassetti.jvm.*;
import me.tomassetti.turin.definitions.ContextDefinition;
import me.tomassetti.turin.parser.ast.expressions.Expression;
import me.tomassetti.turin.parser.ast.statements.*;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsageNode;
import me.tomassetti.turin.symbols.Symbol;
import me.tomassetti.turin.typesystem.TypeUsage;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import turin.context.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CompilationOfStatements {
    private final Compilation compilation;

    public CompilationOfStatements(Compilation compilation) {
        this.compilation = compilation;
    }

    private BytecodeSequence codeToExecuteBeforeReturning;

    BytecodeSequence compile(Statement statement) {
        if (statement instanceof VariableDeclaration) {
            VariableDeclaration variableDeclaration = (VariableDeclaration) statement;
            int pos = compilation.getLocalVarsSymbolTable().add(variableDeclaration.getName(), variableDeclaration);
            JvmTypeCategory typeCategory = variableDeclaration.varType(compilation.getResolver()).jvmType().typeCategory();
            return new ComposedBytecodeSequence(ImmutableList.of(
                    compilation.getPushUtils().pushExpression(variableDeclaration.getValue()),
                    new LocalVarAssignmentBS(pos, typeCategory)));
        } else if (statement instanceof ExpressionStatement) {
            Expression expression = ((ExpressionStatement) statement).getExpression();
            return new CompilationOfPush(compilation).pushExpression(expression);
        } else if (statement instanceof BlockStatement) {
            BlockStatement blockStatement = (BlockStatement) statement;
            List<BytecodeSequence> elements = blockStatement.getStatements().stream().map((s) -> compile(s)).collect(Collectors.toList());
            return new ComposedBytecodeSequence(elements);
        } else if (statement instanceof ReturnStatement) {
            ReturnStatement returnStatement = (ReturnStatement) statement;
            if (returnStatement.hasValue()) {
                Expression returnedValue = returnStatement.getValue();
                TypeUsage returnedValueType = returnedValue.calcType();
                int returnType = returnedValueType.jvmType().returnOpcode();
                if (codeToExecuteBeforeReturning == null) {
                    return new ReturnValueBS(returnType, compilation.getPushUtils().pushExpression(returnStatement.getValue()));
                } else {
                    // So we first calculate the value and then we clean the context
                    return new ReturnValueBS(returnType, new ComposedBytecodeSequence(
                            compilation.getPushUtils().pushExpression(returnStatement.getValue()),
                            codeToExecuteBeforeReturning));
                }
            } else {
                if (codeToExecuteBeforeReturning == null) {
                    return new ReturnVoidBS();
                } else {
                    return new ComposedBytecodeSequence(codeToExecuteBeforeReturning, new ReturnVoidBS());
                }
            }
        } else if (statement instanceof IfStatement) {
            IfStatement ifStatement = (IfStatement) statement;
            BytecodeSequence ifCondition = compilation.getPushUtils().pushExpression(ifStatement.getCondition());
            BytecodeSequence ifBody = compile(ifStatement.getIfBody());
            List<BytecodeSequence> elifConditions = ifStatement.getElifStatements().stream().map((ec) -> compilation.getPushUtils().pushExpression(ec.getCondition())).collect(Collectors.toList());
            List<BytecodeSequence> elifBodys = ifStatement.getElifStatements().stream().map((ec) -> compile(ec.getBody())).collect(Collectors.toList());
            if (ifStatement.hasElse()) {
                return new IfBS(ifCondition, ifBody, elifConditions, elifBodys, compile(ifStatement.getElseBody()));
            } else {
                return new IfBS(ifCondition, ifBody, elifConditions, elifBodys);
            }
        } else if (statement instanceof ThrowStatement) {
            ThrowStatement throwStatement = (ThrowStatement) statement;
            return new ThrowBS(compilation.getPushUtils().pushExpression(throwStatement.getException()));
        } else if (statement instanceof TryCatchStatement) {
            TryCatchStatement tryCatchStatement = (TryCatchStatement) statement;
            return compile(tryCatchStatement);
        } else if (statement instanceof ContextScope) {
            return compile((ContextScope)statement);
        } else {
            throw new UnsupportedOperationException(statement.toString());
        }
    }

    BytecodeSequence codeOnLeavingContextScope(ContextScope contextScope) {
        return new BytecodeSequence() {
            @Override
            public void operate(MethodVisitor mv) {
                for (ContextAssignment assignment : contextScope.getAssignments()) {
                    ContextDefinition contextSymbol = assignment.contextSymbol().get();
                    // We need to get the INSTANCE field
                    JvmFieldDefinition fieldDefinition = new JvmFieldDefinition(
                            JvmNameUtils.canonicalToInternal(contextSymbol.getClassQualifiedName()),
                            "INSTANCE",
                            "L" + JvmNameUtils.canonicalToInternal(contextSymbol.getClassQualifiedName()) + ";",
                            true);
                    new PushStaticField(fieldDefinition).operate(mv);
                    // and then call enterContext
                    JvmMethodDefinition enterContext = new JvmMethodDefinition(
                            JvmNameUtils.internalName(Context.class),
                            "exitContext",
                            "()V",
                            false, false
                    );
                    new MethodInvocationBS(enterContext).operate(mv);
                }
            }
        };
    }

    BytecodeSequence compile(ContextScope contextScope) {
        // TODO catch exceptions just to execute the "leave context" module
        // TODO before returning execute the leave context instructions
        return new BytecodeSequence() {
            @Override
            public void operate(MethodVisitor mv) {
                Label start = new Label();
                Label end = new Label();

                mv.visitLabel(start);

                for (ContextAssignment assignment : contextScope.getAssignments()) {
                    ContextDefinition contextSymbol = assignment.contextSymbol().get();
                    // We need to get the INSTANCE field
                    JvmFieldDefinition fieldDefinition = new JvmFieldDefinition(
                            JvmNameUtils.canonicalToInternal(contextSymbol.getClassQualifiedName()),
                            "INSTANCE",
                            "L" + JvmNameUtils.canonicalToInternal(contextSymbol.getClassQualifiedName()) + ";",
                            true);
                    new PushStaticField(fieldDefinition).operate(mv);
                    // and then call enterContext
                    // push the parameter
                    CompilationOfStatements.this.compilation.getPushUtils().pushExpression(assignment.getContextValue()).operate(mv);
                    JvmMethodDefinition enterContext = new JvmMethodDefinition(
                            JvmNameUtils.internalName(Context.class),
                            "enterContext",
                            "(Ljava/lang/Object;)V",
                            false, false
                    );
                    new MethodInvocationBS(enterContext).operate(mv);
                }

                CompilationOfStatements.this.codeToExecuteBeforeReturning = codeOnLeavingContextScope(contextScope);
                contextScope.getStatements().forEach((s)->compile(s).operate(mv));
                CompilationOfStatements.this.codeToExecuteBeforeReturning = null;
                codeOnLeavingContextScope(contextScope).operate(mv);

                mv.visitLabel(end);


            }
        };
    }

    BytecodeSequence compile(TryCatchStatement tryCatchStatement) {
        return new BytecodeSequence() {
            @Override
            public void operate(MethodVisitor mv) {
                Label tryStart = new Label();
                Label tryEnd = new Label();
                Label afterTryCatch = new Label();
                List<Label> catchSpecificLabels = new ArrayList<Label>();

                for (CatchClause catchClause : tryCatchStatement.getCatchClauses()) {
                    Label catchSpecificLabel = new Label();
                    mv.visitTryCatchBlock(tryStart, tryEnd, catchSpecificLabel, JvmNameUtils.canonicalToInternal(catchClause.getExceptionType().resolve(compilation.getResolver()).getQualifiedName()));
                    catchSpecificLabels.add(catchSpecificLabel);
                }

                mv.visitLabel(tryStart);
                compile(tryCatchStatement.getBody()).operate(mv);
                mv.visitLabel(tryEnd);
                mv.visitJumpInsn(Opcodes.GOTO, afterTryCatch);

                int i = 0;
                for (CatchClause catchClause : tryCatchStatement.getCatchClauses()) {
                    Label catchSpecificLabel = catchSpecificLabels.get(i);
                    mv.visitLabel(catchSpecificLabel);
                    compilation.getLocalVarsSymbolTable().enterBlock();
                    int catchedExcIndex = compilation.getLocalVarsSymbolTable().add(catchClause.getVariableName(), catchClause);
                    new LocalVarAssignmentBS(catchedExcIndex, JvmTypeCategory.REFERENCE).operate(mv);
                    compile(catchClause.getBody()).operate(mv);
                    compilation.getLocalVarsSymbolTable().exitBlock();
                    mv.visitJumpInsn(Opcodes.GOTO, afterTryCatch);
                    i++;
                }

                mv.visitLabel(afterTryCatch);
            }
        };
    }
}