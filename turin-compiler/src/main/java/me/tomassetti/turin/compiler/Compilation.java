package me.tomassetti.turin.compiler;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.compiler.bytecode.*;
import me.tomassetti.turin.implicit.BasicTypeUsage;
import me.tomassetti.turin.jvm.*;
import me.tomassetti.turin.parser.analysis.Property;
import me.tomassetti.turin.parser.analysis.resolvers.Resolver;
import me.tomassetti.turin.parser.ast.*;
import me.tomassetti.turin.parser.ast.expressions.*;
import me.tomassetti.turin.parser.ast.expressions.literals.IntLiteral;
import me.tomassetti.turin.parser.ast.expressions.literals.StringLiteral;
import me.tomassetti.turin.parser.ast.statements.ExpressionStatement;
import me.tomassetti.turin.parser.ast.statements.Statement;
import me.tomassetti.turin.parser.ast.statements.VariableDeclaration;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;
import org.objectweb.asm.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.objectweb.asm.Opcodes.*;

/**
 * Wrap the status of the compilation process, like the class being currently written.
 */
public class Compilation {

    private static final int JAVA_8_CLASS_VERSION = 52;
    public static final int LOCALVAR_INDEX_FOR_THIS_IN_METHOD = 0;
    private static final int LOCALVAR_INDEX_FOR_PARAM_0 = 1;

    private static final String OBJECT_INTERNAL_NAME = JvmNameUtils.canonicalToInternal(Object.class.getCanonicalName());
    private static final String OBJECT_DESCRIPTOR = "L" + OBJECT_INTERNAL_NAME + ";";

    private ClassWriter cw;
    private Resolver resolver;
    private int nParams = 0;
    private int nLocalVars = 0;

    public Compilation(Resolver resolver) {
        this.resolver = resolver;
    }

    public List<ClassFileDefinition> compile(TurinFile turinFile) {
        List<ClassFileDefinition> classFileDefinitions = new ArrayList<>();

        for (Node node : turinFile.getChildren()) {
            if (node instanceof TurinTypeDefinition) {
                classFileDefinitions.addAll(compile((TurinTypeDefinition)node));
            } else if (node instanceof Program) {
                classFileDefinitions.addAll(compile((Program) node));
            }
        }

        return classFileDefinitions;
    }

    private void generateField(Property property) {
        // TODO understand how to use description and signature (which is used for generics)
        JvmType jvmType = property.getTypeUsage().jvmType(resolver);
        FieldVisitor fv = cw.visitField(ACC_PRIVATE, property.getName(), jvmType.getDescriptor(), jvmType.getSignature(), null);
        fv.visitEnd();
    }

    private void generateGetter(Property property, String classInternalName) {
        String getterName = property.getterName();
        JvmType jvmType = property.getTypeUsage().jvmType(resolver);
        // TODO understand how to use description and signature (which is used for generics)
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, getterName, "()" + jvmType.getDescriptor(), "()" + jvmType.getSignature(), null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, LOCALVAR_INDEX_FOR_THIS_IN_METHOD);
        mv.visitFieldInsn(GETFIELD, classInternalName, property.fieldName(), jvmType.getDescriptor());
        mv.visitInsn(returnTypeFor(jvmType));
        // calculated for us
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void enforceConstraint(Property property, MethodVisitor mv, String className, JvmType jvmType, int varIndex) {
        // TODO enforce also arbitrary constraints associated to the property
        if (property.getTypeUsage().equals(BasicTypeUsage.UINT)) {
            // index 0 is the "this"
            mv.visitVarInsn(loadTypeFor(jvmType), varIndex + 1);
            Label label = new Label();

            // if the value is >= 0 we jump and skip the throw exception
            mv.visitJumpInsn(IFGE, label);
            JvmConstructorDefinition constructor = new JvmConstructorDefinition("java/lang/IllegalArgumentException", "(Ljava/lang/String;)V");
            BytecodeSequence instantiateException = new NewInvocation(constructor, ImmutableList.of(new PushStringConst(property.getName() + " should be positive")));
            new Throw(instantiateException).operate(mv);

            mv.visitLabel(label);
        } else if (property.getTypeUsage().isReferenceTypeUsage() && property.getTypeUsage().asReferenceTypeUsage().getQualifiedName(resolver).equals(String.class.getCanonicalName())) {
            // index 0 is the "this"
            mv.visitVarInsn(loadTypeFor(jvmType), varIndex + 1);
            Label label = new Label();

            // if not null skip the throw
            mv.visitJumpInsn(IFNONNULL, label);
            JvmConstructorDefinition constructor = new JvmConstructorDefinition("java/lang/IllegalArgumentException", "(Ljava/lang/String;)V");
            BytecodeSequence instantiateException = new NewInvocation(constructor, ImmutableList.of(new PushStringConst(property.getName() + " cannot be null")));
            new Throw(instantiateException).operate(mv);

            mv.visitLabel(label);
        }
    }

    private void generateSetter(Property property, String internalClassName) {
        String setterName = property.setterName();
        JvmType jvmType = property.getTypeUsage().jvmType(resolver);
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, setterName, "(" + jvmType.getDescriptor() + ")V", "(" + jvmType.getSignature() + ")V", null);
        mv.visitCode();

        enforceConstraint(property, mv, internalClassName, jvmType, 0);

        // Assignment
        PushThis.getInstance().operate(mv);
        mv.visitVarInsn(loadTypeFor(jvmType), 1);
        mv.visitFieldInsn(PUTFIELD, internalClassName, property.getName(), jvmType.getDescriptor());
        mv.visitInsn(RETURN);
        // calculated for us
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void generateConstructor(TurinTypeDefinition typeDefinition, String className) {
        // TODO consider also inherited properties
        List<Property> directProperties = typeDefinition.getDirectProperties(resolver);
        String paramsDescriptor = String.join("", directProperties.stream().map((dp) -> dp.getTypeUsage().jvmType(resolver).getDescriptor()).collect(Collectors.toList()));
        String paramsSignature = String.join("", directProperties.stream().map((dp) -> dp.getTypeUsage().jvmType(resolver).getSignature()).collect(Collectors.toList()));
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(" + paramsDescriptor + ")V", "(" + paramsSignature + ")V", null);
        mv.visitCode();

        PushThis.getInstance().operate(mv);
        mv.visitMethodInsn(INVOKESPECIAL, OBJECT_INTERNAL_NAME, "<init>", "()V", false);

        int propIndex = 0;
        for (Property property : directProperties) {
            enforceConstraint(property, mv, className, property.getTypeUsage().jvmType(resolver), propIndex);
            propIndex++;
        }

        propIndex = 0;
        for (Property property : directProperties) {
            JvmType jvmType = property.getTypeUsage().jvmType(resolver);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(loadTypeFor(jvmType), propIndex + 1);
            mv.visitFieldInsn(PUTFIELD, className, property.getName(), jvmType.getDescriptor());
            propIndex++;
        }

        mv.visitInsn(RETURN);
        // calculated for us
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private int returnTypeFor(JvmType jvmType) {
        switch (jvmType.getDescriptor()) {
            case "Z":
            case "B":
            case "S":
            case "C":
            case "I":
                // used for boolean, byte, short, char, or int
                return IRETURN;
            case "J":
                return LRETURN;
            case "F":
                return FRETURN;
            case "D":
                return DRETURN;
            case "V":
                return RETURN;
            default:
                return ARETURN;
        }
    }

    private int loadTypeFor(JvmType jvmType) {
        switch (jvmType.getDescriptor()) {
            case "Z":
            case "B":
            case "S":
            case "C":
            case "I":
                // used for boolean, byte, short, char, or int
                return ILOAD;
            case "J":
                return LLOAD;
            case "F":
                return FLOAD;
            case "D":
                return DLOAD;
            default:
                return ALOAD;
        }
    }

    private ClassFileDefinition endClass(String canonicalName) {
        cw.visitEnd();

        byte[] programBytecode = cw.toByteArray();
        cw = null;
        return new ClassFileDefinition(canonicalName, programBytecode);
    }

    private void generateEqualsMethod(TurinTypeDefinition typeDefinition, String internalClassName) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "equals", "(" + OBJECT_DESCRIPTOR + ")Z", "(" + OBJECT_DESCRIPTOR + ")Z", null);
        mv.visitCode();

        // if (this == o) return true;
        mv.visitVarInsn(ALOAD, LOCALVAR_INDEX_FOR_THIS_IN_METHOD);
        mv.visitVarInsn(ALOAD, LOCALVAR_INDEX_FOR_PARAM_0);
        Label paramAndThisAreNotTheSame = new Label();
        mv.visitJumpInsn(IF_ACMPNE, paramAndThisAreNotTheSame);
        new ReturnTrue().operate(mv);
        mv.visitLabel(paramAndThisAreNotTheSame);

        // if (o == null || getClass() != o.getClass()) return false;
        mv.visitVarInsn(ALOAD, LOCALVAR_INDEX_FOR_PARAM_0);
        Label paramIsNull = new Label();
        mv.visitJumpInsn(IFNULL, paramIsNull);
        mv.visitVarInsn(ALOAD, LOCALVAR_INDEX_FOR_THIS_IN_METHOD);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
        mv.visitVarInsn(ALOAD, LOCALVAR_INDEX_FOR_PARAM_0);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
        Label paramHasSameClassAsThis = new Label();
        mv.visitJumpInsn(IF_ACMPEQ, paramHasSameClassAsThis);
        mv.visitLabel(paramIsNull);
        new ReturnFalse().operate(mv);
        mv.visitLabel(paramHasSameClassAsThis);

        // MyType other = (MyType) o;
        mv.visitVarInsn(ALOAD, LOCALVAR_INDEX_FOR_PARAM_0);
        mv.visitTypeInsn(CHECKCAST, internalClassName);
        final int localvar_index_for_other = 2;
        mv.visitVarInsn(ASTORE, localvar_index_for_other);

        // if (!this.aField.equals(other.aField)) return false;
        for (Property property : typeDefinition.getAllProperties(resolver)) {
            TypeUsage propertyTypeUsage = property.getTypeUsage();
            String fieldTypeDescriptor = propertyTypeUsage.jvmType(resolver).getDescriptor();

            mv.visitVarInsn(ALOAD, LOCALVAR_INDEX_FOR_THIS_IN_METHOD);
            mv.visitFieldInsn(GETFIELD, internalClassName, property.getName(), fieldTypeDescriptor);
            mv.visitVarInsn(ALOAD, localvar_index_for_other);
            mv.visitFieldInsn(GETFIELD, internalClassName, property.getName(), fieldTypeDescriptor);
            Label propertyIsEqual = new Label();

            if (propertyTypeUsage.isPrimitive()) {
                if (propertyTypeUsage.asPrimitiveTypeUsage().isLong()) {
                    mv.visitInsn(LCMP);
                    mv.visitJumpInsn(IFEQ, propertyIsEqual);
                } else if (propertyTypeUsage.asPrimitiveTypeUsage().isFloat()) {
                    mv.visitInsn(FCMPL);
                    mv.visitJumpInsn(IFEQ, propertyIsEqual);
                } else if (propertyTypeUsage.asPrimitiveTypeUsage().isDouble()) {
                    mv.visitInsn(DCMPL);
                    mv.visitJumpInsn(IFEQ, propertyIsEqual);
                } else {
                    mv.visitJumpInsn(IF_ICMPEQ, propertyIsEqual);
                }
            } else {
                boolean isInterface = propertyTypeUsage.asReferenceTypeUsage().isInterface(resolver);
                if (isInterface) {
                    mv.visitMethodInsn(INVOKEINTERFACE, propertyTypeUsage.jvmType(resolver).getInternalName(), "equals", "(Ljava/lang/Object;)Z", true);
                } else {
                    mv.visitMethodInsn(INVOKEVIRTUAL, propertyTypeUsage.jvmType(resolver).getInternalName(), "equals", "(Ljava/lang/Object;)Z", false);
                }
                mv.visitJumpInsn(IFNE, propertyIsEqual);
            }

            new ReturnFalse().operate(mv);
            mv.visitLabel(propertyIsEqual);
        }

        new ReturnTrue().operate(mv);

        // calculated for us
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void generateHashCodeMethod(TurinTypeDefinition typeDefinition, String internalClassName) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "hashCode", "()I", "()I", null);
        mv.visitCode();

        final int localvar_index_of_result = 1;

        // int result = 1;
        mv.visitInsn(ICONST_1);
        mv.visitVarInsn(ISTORE, localvar_index_of_result);

        for (Property property : typeDefinition.getAllProperties(resolver)) {
            // result = 31 * result + this.aField.hashCode();
            TypeUsage propertyTypeUsage = property.getTypeUsage();
            String fieldTypeDescriptor = propertyTypeUsage.jvmType(resolver).getDescriptor();

            // 31 is just a prime number by which we multiply the current value of result
            mv.visitIntInsn(BIPUSH, 31);
            mv.visitVarInsn(ILOAD, localvar_index_of_result);
            mv.visitInsn(IMUL);

            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, internalClassName, property.getName(), fieldTypeDescriptor);

            if (propertyTypeUsage.isPrimitive()) {
                if (propertyTypeUsage.asPrimitiveTypeUsage().isLong()) {
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "hashCode", "(J)I", false);
                } else if (propertyTypeUsage.asPrimitiveTypeUsage().isFloat()) {
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "hashCode", "(F)I", false);
                } else if (propertyTypeUsage.asPrimitiveTypeUsage().isDouble()) {
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "hashCode", "(D)I", false);
                } else {
                    // nothing to do, the value is already on the stack and we can sum it directly
                }
            } else {
                boolean isInterface = propertyTypeUsage.asReferenceTypeUsage().isInterface(resolver);
                if (isInterface) {
                    mv.visitMethodInsn(INVOKEINTERFACE, propertyTypeUsage.jvmType(resolver).getInternalName(), "hashCode", "()I", true);
                } else {
                    mv.visitMethodInsn(INVOKEVIRTUAL, propertyTypeUsage.jvmType(resolver).getInternalName(), "hashCode", "()I", false);
                }
            }

            mv.visitInsn(IADD);
            mv.visitVarInsn(ISTORE, localvar_index_of_result);
        }

        // return result;
        mv.visitVarInsn(ILOAD, localvar_index_of_result);
        mv.visitInsn(IRETURN);

        // calculated for us
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private List<ClassFileDefinition> compile(TurinTypeDefinition typeDefinition) {
        String internalClassName = JvmNameUtils.canonicalToInternal(typeDefinition.getQualifiedName());

        // Note that COMPUTE_FRAMES implies COMPUTE_MAXS
        cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        // TODO consider generic signature, superclass and interfaces
        cw.visit(JAVA_8_CLASS_VERSION, ACC_PUBLIC + ACC_SUPER, internalClassName, null, OBJECT_INTERNAL_NAME, null);

        for (Property property : typeDefinition.getDirectProperties(resolver)){
            generateField(property);
            generateGetter(property, internalClassName);
            generateSetter(property, internalClassName);
        }

        generateConstructor(typeDefinition, internalClassName);
        generateEqualsMethod(typeDefinition, internalClassName);
        generateHashCodeMethod(typeDefinition, internalClassName);

        return ImmutableList.of(endClass(typeDefinition.getQualifiedName()));
    }

    private List<ClassFileDefinition> compile(Program program) {
        String canonicalClassName = program.getQualifiedName();
        String internalClassName = JvmNameUtils.canonicalToInternal(canonicalClassName);

        // Note that COMPUTE_FRAMES implies COMPUTE_MAXS
        cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cw.visit(JAVA_8_CLASS_VERSION, ACC_PUBLIC + ACC_SUPER, internalClassName, null, OBJECT_INTERNAL_NAME, null);

        // TODO consider exceptions
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);

        nParams = 1;
        nLocalVars = 0;

        mv.visitCode();

        for (Statement statement : program.getStatements()) {
            compile(statement).operate(mv);
        }

        // Implicit return
        mv.visitInsn(RETURN);

        // calculated for us
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        return ImmutableList.of(endClass(canonicalClassName));
    }

    private BytecodeSequence compile(Statement statement) {
        if (statement instanceof VariableDeclaration) {
            VariableDeclaration variableDeclaration = (VariableDeclaration) statement;
            int pos = nParams + nLocalVars;
            nLocalVars += 1;
            return new ComposedBytecodeSequence(ImmutableList.of(compile(variableDeclaration.getValue()), new LocalVarAssignment(pos, JvmTypeCategory.from(variableDeclaration.varType(resolver), resolver))));
        } else if (statement instanceof ExpressionStatement) {
            return executeEpression(((ExpressionStatement)statement).getExpression());
        } else {
            throw new UnsupportedOperationException(statement.toString());
        }
    }

    private BytecodeSequence compile(Expression expression) {
        if (expression instanceof IntLiteral) {
            throw new UnsupportedOperationException();
        } else if (expression instanceof StringLiteral) {
            throw new UnsupportedOperationException();
        } else if (expression instanceof FunctionCall) {
            throw new UnsupportedOperationException();
        } else if (expression instanceof Creation) {
            Creation creation = (Creation)expression;
            List<BytecodeSequence> argumentsPush = creation.getActualParamValuesInOrder().stream()
                    .map((ap) -> pushExpression(ap))
                    .collect(Collectors.toList());
            JvmConstructorDefinition constructorDefinition = creation.jvmDefinition(resolver);
            return new NewInvocation(constructorDefinition, argumentsPush);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private BytecodeSequence pushInstance(FunctionCall functionCall) {
        Expression function = functionCall.getFunction();
        if (function instanceof FieldAccess) {
            return pushExpression(((FieldAccess) function).getSubject());
        } else if (function instanceof ValueReference) {
            throw new UnsupportedOperationException(function.toString());
        } else {
            throw new UnsupportedOperationException(function.getClass().getCanonicalName());
        }
    }

    private BytecodeSequence executeEpression(Expression expr) {
        if (expr instanceof IntLiteral) {
            return NoOp.getInstance();
        } else if (expr instanceof StringLiteral) {
            return NoOp.getInstance();
        } else if (expr instanceof FunctionCall) {
            FunctionCall functionCall = (FunctionCall)expr;
            BytecodeSequence instancePush = pushInstance(functionCall);
            List<BytecodeSequence> argumentsPush = functionCall.getActualParamValuesInOrder().stream()
                    .map((ap) -> pushExpression(ap))
                    .collect(Collectors.toList());
            JvmMethodDefinition methodDefinition = resolver.findJvmDefinition(functionCall);
            return new ComposedBytecodeSequence(ImmutableList.<BytecodeSequence>builder().add(instancePush).addAll(argumentsPush).add(new MethodInvocation(methodDefinition)).build());
        } else {
            throw new UnsupportedOperationException(expr.toString());
        }
    }

    private BytecodeSequence pushExpression(Expression expr) {
        if (expr instanceof IntLiteral) {
            return new PushIntConst(((IntLiteral)expr).getValue());
        } else if (expr instanceof StringLiteral) {
            return new PushStringConst(((StringLiteral)expr).getValue());
        } else if (expr instanceof StaticFieldAccess) {
            StaticFieldAccess staticFieldAccess = (StaticFieldAccess)expr;
            return new PushStaticField(staticFieldAccess.toJvmField(resolver));
        } else {
            throw new UnsupportedOperationException(expr.getClass().getCanonicalName());
        }
    }

}
