package me.tomassetti.turin.compiler;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.compiler.bytecode.*;
import me.tomassetti.turin.implicit.BasicTypes;
import me.tomassetti.turin.jvm.JvmConstructorDefinition;
import me.tomassetti.turin.jvm.JvmMethodDefinition;
import me.tomassetti.turin.jvm.JvmType;
import me.tomassetti.turin.jvm.JvmTypeCategory;
import me.tomassetti.turin.parser.analysis.Property;
import me.tomassetti.turin.parser.analysis.Resolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.Program;
import me.tomassetti.turin.parser.ast.TurinFile;
import me.tomassetti.turin.parser.ast.TurinTypeDefinition;
import me.tomassetti.turin.parser.ast.expressions.*;
import me.tomassetti.turin.parser.ast.statements.ExpressionStatement;
import me.tomassetti.turin.parser.ast.statements.Statement;
import me.tomassetti.turin.parser.ast.statements.VariableDeclaration;
import org.objectweb.asm.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.objectweb.asm.Opcodes.*;

/**
 * Wrap the status of the compilation process, like the class being currently written.
 */
class Compilation {

    private static final int JAVA_8_CLASS_VERSION = 52;
    private static final int LOCALVAR_INDEX_FOR_THIS_IN_METHOD = 0;

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
        FieldVisitor fv = cw.visitField(ACC_PRIVATE, property.getName(), jvmType.getDescriptor(), null, null);
        fv.visitEnd();
    }

    private void generateGetter(Property property, String classInternalName) {
        String getterName = property.getterName();
        JvmType jvmType = property.getTypeUsage().jvmType(resolver);
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, getterName, "()" + jvmType.getSignature(), null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, LOCALVAR_INDEX_FOR_THIS_IN_METHOD);
        mv.visitFieldInsn(GETFIELD, classInternalName, property.fieldName(), jvmType.getDescriptor());
        mv.visitInsn(returnTypeFor(jvmType));
        // calculated for us
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void enforceConstraint(Property property, MethodVisitor mv, String className, String jvmType, int varIndex) {
        // TODO enforce also arbitrary constraints associated to the property
        if (property.getTypeUsage().isReferenceTypeUsage() && property.getTypeUsage().asReferenceTypeUsage().getQualifiedName(resolver).equals(BasicTypes.UINT.getQualifiedName())) {
            mv.visitVarInsn(loadTypeFor(jvmType), varIndex + 1);
            Label label = new Label();
            mv.visitJumpInsn(IFGE, label);
            mv.visitTypeInsn(NEW, "java/lang/IllegalArgumentException");
            mv.visitInsn(DUP);
            mv.visitLdcInsn(property.getName() + " should be positive");
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/IllegalArgumentException", "<init>", "(Ljava/lang/String;)V", false);
            mv.visitInsn(ATHROW);
            mv.visitLabel(label);
        } else if (property.getTypeUsage().isReferenceTypeUsage() && property.getTypeUsage().asReferenceTypeUsage().getQualifiedName(resolver).equals(BasicTypes.STRING.getQualifiedName())) {
            mv.visitVarInsn(loadTypeFor(jvmType), varIndex + 1);
            Label label = new Label();
            mv.visitJumpInsn(IFNONNULL, label);
            mv.visitTypeInsn(NEW, "java/lang/NullPointerException");
            mv.visitInsn(DUP);
            mv.visitLdcInsn(property.getName() + " cannot be null");
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/NullPointerException", "<init>", "(Ljava/lang/String;)V", false);
            mv.visitInsn(ATHROW);
            mv.visitLabel(label);
        }
    }

    private void generateSetter(Property property, String className) {
        String setterName = property.setterName();
        String jvmType = property.getTypeUsage().jvmType(resolver).getSignature();
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, setterName, "(" + jvmType + ")V", null, null);
        mv.visitCode();

        enforceConstraint(property, mv, className, jvmType, 0);

        // Assignment
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(loadTypeFor(jvmType), 1);
        mv.visitFieldInsn(PUTFIELD, className, property.getName(), jvmType);
        mv.visitInsn(RETURN);
        // calculated for us
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void generateContructor(TurinTypeDefinition typeDefinition, String className) {
        List<Property> directPropertis = typeDefinition.getDirectProperties(resolver);
        String paramsSignature = String.join("", directPropertis.stream().map((dp)->dp.getTypeUsage().jvmType(resolver).getSignature()).collect(Collectors.toList()));
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(" + paramsSignature + ")V", null, null);
        mv.visitCode();

        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);

        int propIndex = 0;
        for (Property property : directPropertis) {
            enforceConstraint(property, mv, className, property.getTypeUsage().jvmType(resolver).getSignature(), propIndex);
            propIndex++;
        }

        propIndex = 0;
        for (Property property : directPropertis) {
            String jvmType = property.getTypeUsage().jvmType(resolver).getSignature();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(loadTypeFor(jvmType), propIndex + 1);
            mv.visitFieldInsn(PUTFIELD, className, property.getName(), jvmType);
            propIndex++;
        }

        mv.visitInsn(RETURN);
        // calculated for us
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private int returnTypeFor(JvmType jvmType) {
        if (jvmType.getSignature().equals("I")) {
            return IRETURN;
        }
        return ARETURN;
    }

    private int loadTypeFor(String jvmType) {
        if (jvmType.equals("I")) {
            return ILOAD;
        }
        return ALOAD;
    }

    private List<ClassFileDefinition> compile(TurinTypeDefinition typeDefinition) {
        String className = typeDefinition.getQualifiedName().replaceAll("\\.", "/");

        // Note that COMPUTE_FRAMES implies COMPUTE_MAXS
        cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cw.visit(JAVA_8_CLASS_VERSION, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", null);

        for (Property property : typeDefinition.getDirectProperties(resolver)){
            generateField(property);
            generateGetter(property, className);
            generateSetter(property, className);
        }

        generateContructor(typeDefinition, className);
        cw.visitEnd();

        return ImmutableList.of(new ClassFileDefinition(className.replaceAll("/", "."), cw.toByteArray()));
    }

    private List<ClassFileDefinition> compile(Program program) {
        String qname = program.getQualifiedName();
        String className = qname.replaceAll("\\.", "/");

        cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(JAVA_8_CLASS_VERSION, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", null);

        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);

        nParams = 1;
        nLocalVars = 0;

        mv.visitCode();

        for (Statement statement : program.getStatements()) {
            for (BytecodeSequence bytecodeSequence : compile(statement)){
                bytecodeSequence.operate(mv);
            }
        }

        // Implicit return
        mv.visitInsn(RETURN);

        // calculated for us
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        byte[] programBytecode = cw.toByteArray();
        ClassFileDefinition classFileDefinition = new ClassFileDefinition(qname, programBytecode);
        return ImmutableList.of(classFileDefinition);
    }

    private List<BytecodeSequence> compile(Statement statement) {
        if (statement instanceof VariableDeclaration) {
            VariableDeclaration variableDeclaration = (VariableDeclaration) statement;
            int pos = nParams + nLocalVars;
            nLocalVars += 1;
            return ImmutableList.of(compile(variableDeclaration.getValue()), new LocalVarAssignment(pos, JvmTypeCategory.from(variableDeclaration.varType(resolver), resolver)));
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

    private List<BytecodeSequence> pushInstance(FunctionCall functionCall) {
        Expression function = functionCall.getFunction();
        if (function instanceof FieldAccess) {
            return ImmutableList.of(pushExpression(((FieldAccess) function).getSubject()));
        } else {
            throw new UnsupportedOperationException(functionCall.getFunction().getClass().getCanonicalName());
        }
    }

    private List<BytecodeSequence> executeEpression(Expression expr) {
        if (expr instanceof IntLiteral) {
            // no op
            return Collections.emptyList();
        } else if (expr instanceof StringLiteral) {
            // no op
            return Collections.emptyList();
        } else if (expr instanceof FunctionCall) {
            FunctionCall functionCall = (FunctionCall)expr;
            List<BytecodeSequence> instancePush = pushInstance(functionCall);
            List<BytecodeSequence> argumentsPush = functionCall.getActualParamValuesInOrder().stream()
                    .map((ap) -> pushExpression(ap))
                    .collect(Collectors.toList());
            JvmMethodDefinition methodDefinition = resolver.findJvmDefinition(functionCall);
            return ImmutableList.<BytecodeSequence>builder().addAll(instancePush).addAll(argumentsPush).add(new MethodInvocation(methodDefinition)).build();
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
