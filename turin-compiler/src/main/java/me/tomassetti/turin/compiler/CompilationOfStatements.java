package me.tomassetti.turin.compiler;

import com.google.common.collect.ImmutableList;
import me.tomassetti.bytecode_generation.*;
import me.tomassetti.bytecode_generation.pushop.PushLocalVar;
import me.tomassetti.bytecode_generation.returnop.ReturnValueBS;
import me.tomassetti.bytecode_generation.returnop.ReturnVoidBS;
import me.tomassetti.jvm.JvmNameUtils;
import me.tomassetti.jvm.JvmType;
import me.tomassetti.jvm.JvmTypeCategory;
import me.tomassetti.turin.parser.ast.FormalParameter;
import me.tomassetti.turin.parser.ast.TypeDefinition;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;
import me.tomassetti.turin.parser.ast.expressions.AssignmentExpression;
import me.tomassetti.turin.parser.ast.expressions.Expression;
import me.tomassetti.turin.parser.ast.expressions.InstanceFieldAccess;
import me.tomassetti.turin.parser.ast.statements.*;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CompilationOfStatements {
    private final Compilation compilation;

    public CompilationOfStatements(Compilation compilation) {
        this.compilation = compilation;
    }

    BytecodeSequence compile(Statement statement) {
        if (statement instanceof VariableDeclaration) {
            VariableDeclaration variableDeclaration = (VariableDeclaration) statement;
            int pos = compilation.getLocalVarsSymbolTable().add(variableDeclaration.getName(), variableDeclaration);
            JvmTypeCategory typeCategory = variableDeclaration.varType(compilation.getResolver()).toJvmTypeCategory(compilation.getResolver());
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
                int returnType = returnStatement.getValue().calcType(compilation.getResolver()).jvmType(compilation.getResolver()).returnOpcode();
                return new ReturnValueBS(returnType, compilation.getPushUtils().pushExpression(returnStatement.getValue()));
            } else {
                return new ReturnVoidBS();
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
        } else if (statement instanceof SuperInvokation) {
            SuperInvokation superInvokation = (SuperInvokation) statement;
            return compile(superInvokation);
        } else {
            throw new UnsupportedOperationException(statement.toString());
        }
    }

    BytecodeSequence compile(SuperInvokation superInvokation) {
        // push all explicitly passed parameters
        int index = 1;
        for (ActualParam actualParam : superInvokation.getParams()){
            if (!formalParameter.hasDefaultValue()) {
                JvmType jvmType = formalParameter.getType().jvmType(resolver);
                new PushLocalVar(OpcodesUtils.loadTypeFor(jvmType), index).operate(mv);
                index++;
            }
        }
        // push the map if it has default values
        if (superConstructor.hasDefaultParams()) {
            // push the map which is in the parameters after all the formal parameters
            index = formalParametersWithoutDefaults.size() + 1;
            new PushLocalVar(Opcodes.ALOAD, index).operate(mv);
        }


        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, superConstructor.getJvmConstructorDefinition().getOwnerInternalName(), "<init>",
                superConstructor.getJvmConstructorDefinition().getDescriptor(), false);
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