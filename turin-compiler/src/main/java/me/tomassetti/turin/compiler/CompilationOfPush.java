package me.tomassetti.turin.compiler;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.compiler.bytecode.*;
import me.tomassetti.turin.compiler.bytecode.logicalop.LogicalAndBS;
import me.tomassetti.turin.compiler.bytecode.logicalop.LogicalNotBS;
import me.tomassetti.turin.compiler.bytecode.logicalop.LogicalOrBS;
import me.tomassetti.turin.compiler.bytecode.pushop.*;
import me.tomassetti.turin.jvm.JvmConstructorDefinition;
import me.tomassetti.turin.jvm.JvmFieldDefinition;
import me.tomassetti.turin.jvm.JvmMethodDefinition;
import me.tomassetti.turin.parser.analysis.Property;
import me.tomassetti.turin.parser.analysis.UnsolvedMethodException;
import me.tomassetti.turin.parser.analysis.resolvers.jdk.ReflectionBaseField;
import me.tomassetti.turin.parser.analysis.resolvers.jdk.ReflectionBasedSetOfOverloadedMethods;
import me.tomassetti.turin.parser.ast.FunctionDefinition;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.expressions.*;
import me.tomassetti.turin.parser.ast.expressions.literals.BooleanLiteral;
import me.tomassetti.turin.parser.ast.expressions.literals.IntLiteral;
import me.tomassetti.turin.parser.ast.expressions.literals.StringLiteral;
import me.tomassetti.turin.parser.ast.typeusage.PrimitiveTypeUsage;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
            Node declaration = valueReference.resolve(compilation.getResolver());
            if (declaration instanceof ReflectionBasedSetOfOverloadedMethods) {
                ReflectionBasedSetOfOverloadedMethods methods = (ReflectionBasedSetOfOverloadedMethods) declaration;
                if (methods.isStatic()) {
                    return NoOp.getInstance();
                } else {
                    return push(methods.getInstance());
                }
            } else if (declaration instanceof FunctionDefinition) {
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

    BytecodeSequence push(Node node) {
        if (node instanceof ReflectionBaseField) {
            ReflectionBaseField reflectionBaseField = (ReflectionBaseField) node;
            if (reflectionBaseField.isStatic()) {
                return new PushStaticField(reflectionBaseField.toJvmField(compilation.getResolver()));
            } else {
                throw new UnsupportedOperationException();
            }
        } else if (node instanceof Property) {
            Property property = (Property) node;
            JvmFieldDefinition field = new JvmFieldDefinition(compilation.getInternalClassName(), property.fieldName(), property.getTypeUsage().jvmType(compilation.getResolver()).getDescriptor(), false);
            return new PushInstanceField(field);
        } else {
            throw new UnsupportedOperationException(node.getClass().getCanonicalName());
        }
    }

    BytecodeSequence pushExpression(Expression expr) {
        if (expr instanceof IntLiteral) {
            return new PushIntConst(((IntLiteral) expr).getValue());
        } else if (expr instanceof StringLiteral) {
            return new PushStringConst(((StringLiteral) expr).getValue());
        } else if (expr instanceof StaticFieldAccess) {
            StaticFieldAccess staticFieldAccess = (StaticFieldAccess) expr;
            return new PushStaticField(staticFieldAccess.toJvmField(compilation.getResolver()));
        } else if (expr instanceof StringInterpolation) {
            StringInterpolation stringInterpolation = (StringInterpolation) expr;

            List<BytecodeSequence> elements = new ArrayList<BytecodeSequence>();
            elements.add(new NewInvocationBS(new JvmConstructorDefinition("java/lang/StringBuilder", "()V"), Collections.emptyList()));

            for (Expression piece : stringInterpolation.getElements()) {
                compilation.appendToStringBuilder(piece, elements);
            }

            elements.add(new MethodInvocationBS(new JvmMethodDefinition("java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false, false)));
            return new ComposedBytecodeSequence(elements);
        } else if (expr instanceof ValueReference) {
            ValueReference valueReference = (ValueReference) expr;
            Optional<Integer> index = compilation.getLocalVarsSymbolTable().findIndex(valueReference.getName());
            if (index.isPresent()) {
                TypeUsage type = compilation.getLocalVarsSymbolTable().findDeclaration(valueReference.getName()).get().calcType(compilation.getResolver());
                return new PushLocalVar(compilation.loadTypeForTypeUsage(type), index.get());
            } else {
                return push(valueReference.resolve(compilation.getResolver()));
            }
        } else if (expr instanceof MathOperation) {
            MathOperation mathOperation = (MathOperation) expr;
            // TODO do proper conversions
            if (!mathOperation.getLeft().calcType(compilation.getResolver()).equals(PrimitiveTypeUsage.INT)) {
                throw new UnsupportedOperationException();
            }
            if (!mathOperation.getRight().calcType(compilation.getResolver()).equals(PrimitiveTypeUsage.INT)) {
                throw new UnsupportedOperationException();
            }

            return new ComposedBytecodeSequence(ImmutableList.of(
                    pushExpression(mathOperation.getLeft()),
                    pushExpression(mathOperation.getRight()),
                    new MathOperationBS(mathOperation.getLeft().calcType(compilation.getResolver()).jvmType(compilation.getResolver()).typeCategory(), mathOperation.getOperator())));
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
                    new RelationalOperationBS(relationalOperation.getOperator())
            ));
        } else if (expr instanceof FunctionCall) {
            FunctionCall functionCall = (FunctionCall) expr;
            functionCall.desugarize(compilation.getResolver());
            BytecodeSequence instancePush = pushInstance(functionCall);
            List<BytecodeSequence> argumentsPush = functionCall.getActualParamValuesInOrder().stream()
                    .map((ap) -> pushExpression(ap))
                    .collect(Collectors.toList());
            Optional<JvmMethodDefinition> methodDefinition = compilation.getResolver().findJvmDefinition(functionCall);
            if (!methodDefinition.isPresent()) {
                throw new UnsolvedMethodException(functionCall);
            }
            return new ComposedBytecodeSequence(ImmutableList.<BytecodeSequence>builder().add(instancePush).addAll(argumentsPush).add(new MethodInvocationBS(methodDefinition.get())).build());
        } else if (expr instanceof Creation) {
            Creation creation = (Creation) expr;
            creation.desugarize(compilation.getResolver());
            List<BytecodeSequence> argumentsPush = creation.getActualParamValuesInOrder().stream()
                    .map((ap) -> pushExpression(ap))
                    .collect(Collectors.toList());
            JvmConstructorDefinition constructorDefinition = creation.jvmDefinition(compilation.getResolver());
            return new NewInvocationBS(constructorDefinition, argumentsPush);
        } else if (expr instanceof ArrayAccess) {
            ArrayAccess arrayAccess = (ArrayAccess) expr;
            return new ComposedBytecodeSequence(ImmutableList.of(
                    pushExpression(arrayAccess.getArray()),
                    pushExpression(arrayAccess.getIndex()),
                    new ArrayAccessBS(arrayAccess.calcType(compilation.getResolver()).jvmType(compilation.getResolver()).typeCategory())));
        } else if (expr instanceof InstanceFieldAccess) {
            InstanceFieldAccess instanceFieldAccess = (InstanceFieldAccess) expr;
            // Ideally it should be desugarized before
            if (instanceFieldAccess.isArrayLength(compilation.getResolver())) {
                return new ComposedBytecodeSequence(pushExpression(instanceFieldAccess.getSubject()), new ArrayLengthBS());
            } else {
                throw new UnsupportedOperationException(expr.toString());
            }
        } else if (expr instanceof InstanceMethodInvokation) {
            InstanceMethodInvokation instanceMethodInvokation = (InstanceMethodInvokation) expr;
            instanceMethodInvokation.desugarize(compilation.getResolver());
            BytecodeSequence instancePush = pushExpression(instanceMethodInvokation.getSubject());
            JvmMethodDefinition methodDefinition = instanceMethodInvokation.findJvmDefinition(compilation.getResolver());
            List<BytecodeSequence> argumentsPush = new ArrayList<>();
            int i=0;
            for (Expression value : instanceMethodInvokation.getActualParamValuesInOrder()) {
                boolean isPrimitive = value.calcType(compilation.getResolver()).isPrimitive();
                if (isPrimitive && !methodDefinition.isParamPrimitive(i)){
                    // need boxing
                    argumentsPush.add(pushExpression(box(value)));
                } else {
                    argumentsPush.add(pushExpression(value));
                }
                i++;
            }
            return new ComposedBytecodeSequence(ImmutableList.<BytecodeSequence>builder().add(instancePush).addAll(argumentsPush).add(new MethodInvocationBS(methodDefinition)).build());
        } else {
            throw new UnsupportedOperationException(expr.getClass().getCanonicalName());
        }
    }

    private Expression box(Expression value) {
        PrimitiveTypeUsage typeUsage = value.calcType(compilation.getResolver()).asPrimitiveTypeUsage();
        if (typeUsage.isInt()) {
            return new Creation("java.lang.Integer", ImmutableList.of(new ActualParam(value)));
        } else {
            throw new UnsupportedOperationException();
        }
    }
}