package me.tomassetti.turin.compiler;

import com.google.common.collect.ImmutableList;
import me.tomassetti.bytecode_generation.*;
import me.tomassetti.bytecode_generation.logicalop.CastBS;
import me.tomassetti.bytecode_generation.logicalop.LogicalAndBS;
import me.tomassetti.bytecode_generation.logicalop.LogicalNotBS;
import me.tomassetti.bytecode_generation.logicalop.LogicalOrBS;
import me.tomassetti.bytecode_generation.pushop.*;
import me.tomassetti.jvm.*;
import me.tomassetti.turin.definitions.TypeDefinition;
import me.tomassetti.turin.parser.analysis.Property;
import me.tomassetti.turin.parser.analysis.exceptions.UnsolvedMethodException;
import me.tomassetti.turin.parser.analysis.resolvers.jdk.ReflectionBasedField;
import me.tomassetti.turin.parser.analysis.resolvers.jdk.ReflectionBasedSetOfOverloadedMethods;
import me.tomassetti.turin.parser.ast.expressions.relations.AccessEndpoint;
import me.tomassetti.turin.parser.ast.invokables.FunctionDefinitionNode;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.Placeholder;
import me.tomassetti.turin.parser.ast.expressions.*;
import me.tomassetti.turin.parser.ast.expressions.literals.*;
import me.tomassetti.turin.parser.ast.statements.SuperInvokation;
import me.tomassetti.turin.symbols.Symbol;
import me.tomassetti.turin.typesystem.PrimitiveTypeUsage;
import me.tomassetti.turin.typesystem.TypeUsage;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static me.tomassetti.turin.compiler.BoxUnboxing.box;
import static me.tomassetti.bytecode_generation.OpcodesUtils.loadTypeFor;

public class CompilationOfPush {
    private final Compilation compilation;

    public CompilationOfPush(Compilation compilation) {
        this.compilation = compilation;
    }

    BytecodeSequence pushInstance(FunctionCall functionCall) {
        Expression function = functionCall.getFunction();
        if (function instanceof FieldAccess) {
            return pushExpression(((FieldAccess) function).getSubject());
        } else if (function instanceof ValueReference) {
            ValueReference valueReference = (ValueReference) function;
            Symbol declaration = valueReference.resolve(compilation.getResolver());
            if (declaration instanceof ReflectionBasedSetOfOverloadedMethods) {
                ReflectionBasedSetOfOverloadedMethods methods = (ReflectionBasedSetOfOverloadedMethods) declaration;
                if (methods.isStatic()) {
                    return NoOp.getInstance();
                } else {
                    return push(methods.getInstance());
                }
            } else if (declaration instanceof FunctionDefinitionNode) {
                return NoOp.getInstance();
            } else {
                throw new UnsupportedOperationException(declaration.getClass().getCanonicalName());
            }
        } else if (function instanceof StaticFieldAccess) {
            return NoOp.getInstance();
        } else {
            throw new UnsupportedOperationException(function.getClass().getCanonicalName());
        }
    }

    BytecodeSequence push(Symbol symbol) {
        if (symbol.isNode()) {
            return push(symbol.asNode());
        } else {
            throw new UnsupportedOperationException();
        }
    }

    BytecodeSequence push(Node node) {
        if (node instanceof ReflectionBasedField) {
            ReflectionBasedField reflectionBaseField = (ReflectionBasedField) node;
            if (reflectionBaseField.isStatic()) {
                return new PushStaticField(reflectionBaseField.toJvmField(compilation.getResolver()));
            } else {
                throw new UnsupportedOperationException();
            }
        } else if (node instanceof Property) {
            Property property = (Property) node;
            JvmFieldDefinition field = new JvmFieldDefinition(compilation.getInternalClassName(), property.fieldName(), property.getTypeUsage().jvmType().getDescriptor(), false);
            return new PushInstanceField(field);
        } else if (node instanceof AccessEndpoint) {
            AccessEndpoint accessEndpoint = (AccessEndpoint) node;
            // we should call a static method created in the Relation class passing the instance
            // as the only parameter
            String relationClassInternalName = JvmNameUtils.canonicalToInternal(accessEndpoint.getRelationDefinition().getGeneratedClassQualifiedName());
            String descriptor = accessEndpoint.getRelationField().methodDescriptor(compilation.getResolver());
            JvmMethodDefinition methodDefinition = new JvmMethodDefinition(
                    relationClassInternalName,
                    accessEndpoint.getRelationField().methodName(),
                    descriptor,
                    true,
                    false);
            return new ComposedBytecodeSequence(
                    push(accessEndpoint.getInstance()),
                    new MethodInvocationBS(methodDefinition)
            );
        } else if (node instanceof Expression) {
            return pushExpression((Expression)node);
        } else {
            throw new UnsupportedOperationException(node.getClass().getCanonicalName());
        }
    }

    BytecodeSequence pushExpression(Expression expr) {
        if (expr instanceof ByteLiteral) {
            return new PushIntConst(((ByteLiteral) expr).getValue());
        } else if (expr instanceof ShortLiteral) {
            return new PushIntConst(((ShortLiteral) expr).getValue());
        } else if (expr instanceof IntLiteral) {
            return new PushIntConst(((IntLiteral) expr).getValue());
        } else if (expr instanceof LongLiteral) {
            return new PushLongConst(((LongLiteral) expr).getValue());
        } else if (expr instanceof FloatLiteral) {
            return new PushFloatConst(((FloatLiteral) expr).getValue());
        } else if (expr instanceof DoubleLiteral) {
            return new PushDoubleConst(((DoubleLiteral) expr).getValue());
        } else if (expr instanceof StringLiteral) {
            return new PushStringConst(((StringLiteral) expr).getValue());
        } else if (expr instanceof StaticFieldAccess) {
            StaticFieldAccess staticFieldAccess = (StaticFieldAccess) expr;
            return new PushStaticField(staticFieldAccess.toJvmField(compilation.getResolver()));
        } else if (expr instanceof StringInterpolation) {
            StringInterpolation stringInterpolation = (StringInterpolation) expr;

            List<BytecodeSequence> elements = new ArrayList<BytecodeSequence>();
            elements.add(new NewInvocationBS(new JvmConstructorDefinition("java/lang/StringBuilder", "()V"), NoOp.getInstance()));

            for (Expression piece : stringInterpolation.getElements()) {
                compilation.appendToStringBuilder(piece, elements);
            }

            elements.add(new MethodInvocationBS(new JvmMethodDefinition("java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false, false)));
            return new ComposedBytecodeSequence(elements);
        } else if (expr instanceof ValueReference) {
            ValueReference valueReference = (ValueReference) expr;
            Optional<Integer> index = compilation.getLocalVarsSymbolTable().findIndex(valueReference.getName());
            if (index.isPresent()) {
                TypeUsage type = compilation.getLocalVarsSymbolTable().findDeclaration(valueReference.getName()).get().calcType();
                return new PushLocalVar(loadTypeForTypeUsage(type), index.get());
            } else if (compilation.getLocalVarsSymbolTable().hasAlias(valueReference.getName())) {
                return compilation.getLocalVarsSymbolTable().getAlias(valueReference.getName());
            } else {
                return push(valueReference.resolve(compilation.getResolver()));
            }
        } else if (expr instanceof MathOperation) {
            MathOperation mathOperation = (MathOperation) expr;
            // TODO do proper conversions
            if (!mathOperation.getLeft().calcType().sameType(PrimitiveTypeUsage.INT)) {
                throw new UnsupportedOperationException();
            }
            if (!mathOperation.getRight().calcType().sameType(PrimitiveTypeUsage.INT)) {
                throw new UnsupportedOperationException();
            }
            JvmTypeCategory leftTypeCategory = mathOperation.getLeft().calcType().jvmType().typeCategory();
            return new ComposedBytecodeSequence(ImmutableList.of(
                    pushExpression(mathOperation.getLeft()),
                    pushExpression(mathOperation.getRight()),
                    BytecodeUtils.createMathOperation(leftTypeCategory, mathOperation.getOperator())));
        } else if (expr instanceof BooleanLiteral) {
            return new PushBoolean(((BooleanLiteral) expr).getValue());
        } else if (expr instanceof LogicOperation) {
            LogicOperation logicOperation = (LogicOperation) expr;
            switch (logicOperation.getOperator()) {
                case AND:
                    return new ComposedBytecodeSequence(ImmutableList.of(
                            pushExpression(logicOperation.getLeft()),
                            pushExpression(logicOperation.getRight()),
                            new LogicalAndBS()
                    ));
                case OR:
                    return new ComposedBytecodeSequence(ImmutableList.of(
                            pushExpression(logicOperation.getLeft()),
                            pushExpression(logicOperation.getRight()),
                            new LogicalOrBS()
                    ));
                default:
                    throw new UnsupportedOperationException(logicOperation.getOperator().name());
            }
        } else if (expr instanceof NotOperation) {
            NotOperation notOperation = (NotOperation) expr;
            return new ComposedBytecodeSequence(ImmutableList.of(pushExpression(notOperation.getValue()), new LogicalNotBS()));
        } else if (expr instanceof RelationalOperation) {
            RelationalOperation relationalOperation = (RelationalOperation) expr;
            return new ComposedBytecodeSequence(ImmutableList.of(
                    pushExpression(relationalOperation.getLeft()),
                    pushExpression(relationalOperation.getRight()),
                    BytecodeUtils.createRelationOperation(relationalOperation.getOperator())
            ));
        } else if (expr instanceof FunctionCall) {
            FunctionCall functionCall = (FunctionCall) expr;
            functionCall.desugarize(compilation.getResolver());
            BytecodeSequence instancePush = pushInstance(functionCall);
            Optional<JvmMethodDefinition> methodDefinition = compilation.getResolver().findJvmDefinition(functionCall);
            if (!methodDefinition.isPresent()) {
                throw new UnsolvedMethodException(functionCall);
            }
            BytecodeSequence argumentsPush = adaptAndPushAllParameters(
                    functionCall.getActualParamValuesInOrder(), methodDefinition.get()
            );
            return new ComposedBytecodeSequence(ImmutableList.<BytecodeSequence>builder()
                    .add(instancePush)
                    .add(argumentsPush)
                    .add(new MethodInvocationBS(methodDefinition.get())).build());
        } else if (expr instanceof Creation) {
            Creation creation = (Creation) expr;
            creation.desugarize(compilation.getResolver());
            JvmConstructorDefinition constructorDefinition = creation.jvmDefinition(compilation.getResolver());
            BytecodeSequence argumentsPush = adaptAndPushAllParameters(creation.getActualParamValuesInOrder(), constructorDefinition);
            return new NewInvocationBS(constructorDefinition, argumentsPush);
        } else if (expr instanceof ArrayAccess) {
            ArrayAccess arrayAccess = (ArrayAccess) expr;
            return new ComposedBytecodeSequence(ImmutableList.of(
                    pushExpression(arrayAccess.getArray()),
                    pushExpression(arrayAccess.getIndex()),
                    new ArrayAccessBS(arrayAccess.calcType().jvmType().typeCategory())));
        } else if (expr instanceof InstanceFieldAccess) {
            InstanceFieldAccess instanceFieldAccess = (InstanceFieldAccess) expr;
            // Ideally it should be desugarized before
            if (instanceFieldAccess.isArrayLength(compilation.getResolver())) {
                return new ComposedBytecodeSequence(pushExpression(instanceFieldAccess.getSubject()), new ArrayLengthBS());
            } else {
                TypeUsage instanceType = instanceFieldAccess.getSubject().calcType();
                Node value = instanceType.getFieldOnInstance(instanceFieldAccess.getField(), instanceFieldAccess.getSubject(), compilation.getResolver());
                return push(value);
            }
        } else if (expr instanceof InstanceMethodInvokation) {
            InstanceMethodInvokation instanceMethodInvokation = (InstanceMethodInvokation) expr;

            // TODO cast result when it involves generics

            instanceMethodInvokation.desugarize(compilation.getResolver());
            BytecodeSequence instancePush = pushExpression(instanceMethodInvokation.getSubject());
            JvmMethodDefinition methodDefinition = instanceMethodInvokation.findJvmDefinition(compilation.getResolver());
            TypeUsage returnType = instanceMethodInvokation.calcType();
            String typeReturnedFromMethod = methodDefinition.getReturnTypeDescriptor();
            // This could happen because of generics: in this case a cast is needed
            BytecodeSequence invokationBS = new ComposedBytecodeSequence(ImmutableList.<BytecodeSequence>builder()
                    .add(instancePush)
                    .add(adaptAndPushAllParameters(instanceMethodInvokation.getActualParamValuesInOrder(), methodDefinition))
                    .add(new MethodInvocationBS(methodDefinition)).build());
            if (!returnType.jvmType().getDescriptor().equals(typeReturnedFromMethod)){
                return new ComposedBytecodeSequence(invokationBS, new CastBS(returnType.jvmType().getInternalName()));
            } else {
                return invokationBS;
            }
        } else if (expr instanceof Placeholder) {
            return compilation.getLocalVarsSymbolTable().getAlias("placeholder");
        } else if (expr instanceof ThisExpression) {
            return PushThis.getInstance();
        } else if (expr instanceof AssignmentExpression) {
            return pushAssignment((AssignmentExpression)expr);
        } else if (expr instanceof SuperInvokation) {
            SuperInvokation superInvokation = (SuperInvokation) expr;
            return compile(superInvokation);
        } else if (expr instanceof RelationSubset) {
            throw new UnsupportedOperationException();
        } else {
            throw new UnsupportedOperationException(expr.getClass().getCanonicalName());
        }
    }

    BytecodeSequence compile(SuperInvokation superInvokation) {
        superInvokation.desugarize(compilation.getResolver());
        BytecodeSequence instancePush = PushThis.getInstance();
        Optional<JvmConstructorDefinition> constructor = superInvokation.findJvmDefinition(compilation.getResolver());
        if (!constructor.isPresent()) {
            throw new UnsolvedMethodException(superInvokation);
        }
        BytecodeSequence argumentsPush = compilation.getPushUtils().adaptAndPushAllParameters(
                superInvokation.getActualParamValuesInOrder(), constructor.get()
        );
        BytecodeSequence invokation = new BytecodeSequence() {
            @Override
            public void operate(MethodVisitor mv) {
                mv.visitMethodInsn(Opcodes.INVOKESPECIAL, constructor.get().getOwnerInternalName(), "<init>",
                        constructor.get().getDescriptor(), false);
            }
        };
        return new ComposedBytecodeSequence(ImmutableList.<BytecodeSequence>builder()
                .add(instancePush)
                .add(argumentsPush)
                .add(invokation)
                .build());
    }

    private BytecodeSequence pushAssignment(AssignmentExpression assignmentStatement) {
        if (assignmentStatement.getTarget() instanceof InstanceFieldAccess) {
            InstanceFieldAccess instanceFieldAccess = (InstanceFieldAccess)assignmentStatement.getTarget();
            BytecodeSequence pushInstance = compilation.getPushUtils().pushExpression(instanceFieldAccess.getSubject());
            BytecodeSequence pushValue = compilation.getPushUtils().pushExpression(assignmentStatement.getValue());
            BytecodeSequence putField = new BytecodeSequence() {
                @Override
                public void operate(MethodVisitor mv) {
                    TypeDefinition typeDefinition = instanceFieldAccess.getSubject().calcType()
                            .asReferenceTypeUsage()
                            .getTypeDefinition();
                    String internalClassName = JvmNameUtils.canonicalToInternal(typeDefinition.getQualifiedName());
                    String descriptor = typeDefinition.getFieldType(instanceFieldAccess.getField(), false, compilation.getResolver())
                            .jvmType()
                            .getDescriptor();
                    mv.visitFieldInsn(Opcodes.PUTFIELD, internalClassName, instanceFieldAccess.getField(), descriptor);
                }
            };
            return new ComposedBytecodeSequence(
                    pushInstance,
                    pushValue,
                    putField
            );
        }
        throw new UnsupportedOperationException(assignmentStatement.getTarget().getClass().getCanonicalName());
    }

    BytecodeSequence adaptAndPushAllParameters(List<Expression> actualValues, JvmInvokableDefinition invokableDefinition) {
        List<BytecodeSequence> elements = new LinkedList<>();
        for (int i=0; i<actualValues.size(); i++) {
            Expression value = actualValues.get(i);
            JvmType formalType = invokableDefinition.getParamType(i);
            elements.add(adaptAndPush(value, formalType));
        }
        return new ComposedBytecodeSequence(elements);
    }

    private BytecodeSequence adaptAndPush(Expression value, JvmType formalType) {
        JvmType actualType = value.calcType().jvmType();
        boolean isPrimitive = actualType.isPrimitive();
        if (isPrimitive && !formalType.isPrimitive()) {
            // need boxing
            return pushExpression(box(value, compilation.getResolver()));
        } if (isPrimitive && formalType.isPrimitive() && !actualType.equals(formalType)) {
            // need primitive conversion
            return convertAndPush(value, formalType);
        } else {
            return pushExpression(value);
        }
    }

    public BytecodeSequence convertAndPush(Expression value, JvmType formalType) {
        JvmType actualType = value.calcType().jvmType();
        if (actualType.equals(formalType)) {
            return pushExpression(value);
        }
        if (!actualType.isPrimitive()) {
            throw new IllegalArgumentException();
        }
        if (!formalType.isPrimitive()) {
            throw new IllegalArgumentException();
        }
        if (actualType.isStoredInInt() && formalType.equals(JvmType.INT)) {
            return pushExpression(value);
        }
        if (actualType.equals(JvmType.INT) && formalType.equals(JvmType.LONG)) {
            return new ComposedBytecodeSequence(
                    pushExpression(value),
                    new IntToLongBS()
            );
        } else {
            throw new UnsupportedOperationException("actual: " + actualType + ", formal: " + formalType);
        }
    }

    int loadTypeForTypeUsage(TypeUsage type) {
        return loadTypeFor(type.jvmType());
    }

}