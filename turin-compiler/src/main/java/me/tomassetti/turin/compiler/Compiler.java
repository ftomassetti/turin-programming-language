package me.tomassetti.turin.compiler;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.compiler.bytecode.Assignment;
import me.tomassetti.turin.compiler.bytecode.BytecodeSequence;
import me.tomassetti.turin.compiler.bytecode.JvmTypeCategory;
import me.tomassetti.turin.implicit.BasicTypes;
import me.tomassetti.turin.parser.Parser;
import me.tomassetti.turin.parser.analysis.InFileResolver;
import me.tomassetti.turin.parser.analysis.Property;
import me.tomassetti.turin.parser.analysis.Resolver;
import me.tomassetti.turin.parser.ast.*;
import me.tomassetti.turin.parser.ast.expressions.Expression;
import me.tomassetti.turin.parser.ast.statements.Statement;
import me.tomassetti.turin.parser.ast.statements.VariableDeclaration;
import org.objectweb.asm.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.objectweb.asm.Opcodes.*;

public class Compiler {

    private static String VERSION = "Alpha 000";

    private static final int JAVA_8_CLASS_VERSION = 52;

    private Resolver resolver = new InFileResolver();

    private class Compilation {

        private ClassWriter cw;

        private void generateField(Property property) {
            FieldVisitor fv = cw.visitField(ACC_PRIVATE, property.getName(), property.getTypeUsage().jvmType(resolver), null, null);
            fv.visitEnd();
        }

        private void generateGetter(Property property, String className) {
            String getterName = "get" + Character.toUpperCase(property.getName().charAt(0)) + property.getName().substring(1);
            String jvmType = property.getTypeUsage().jvmType(resolver);
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, getterName, "()" + jvmType, null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, className, property.getName(), jvmType);
            mv.visitInsn(returnTypeFor(jvmType));
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }

        private void enforceConstraint(Property property, MethodVisitor mv, String className, String jvmType, int varIndex) {
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
            String setterName = "set" + Character.toUpperCase(property.getName().charAt(0)) + property.getName().substring(1);
            String jvmType = property.getTypeUsage().jvmType(resolver);
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, setterName, "(" + jvmType + ")V", null, null);
            mv.visitCode();

            enforceConstraint(property, mv, className, jvmType, 0);

            // Assignment
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(loadTypeFor(jvmType), 1);
            mv.visitFieldInsn(PUTFIELD, className, property.getName(), jvmType);
            mv.visitInsn(RETURN);
            mv.visitMaxs(3, 2);
            mv.visitEnd();
        }

        private void generateContructor(TypeDefinition typeDefinition, String className) {
            List<Property> directPropertis = typeDefinition.getDirectProperties(resolver);
            String paramsSignature = String.join("", directPropertis.stream().map((dp)->dp.getTypeUsage().jvmType(resolver)).collect(Collectors.toList()));
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(" + paramsSignature + ")V", null, null);
            mv.visitCode();

            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);

            int propIndex = 0;
            for (Property property : directPropertis) {
                enforceConstraint(property, mv, className, property.getTypeUsage().jvmType(resolver), propIndex);
                propIndex++;
                if (propIndex == directPropertis.size()) {
                    mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
                } else {
                    List<Object> frameObject = new ArrayList<>();
                    frameObject.add(className);
                    for (Property p : directPropertis) {
                        frameObject.add(toFrameObject(p.getTypeUsage().jvmType(resolver)));
                    }
                    mv.visitFrame(Opcodes.F_FULL, 1 + directPropertis.size(), frameObject.toArray(), 0, new Object[] {});
                }
            }

            propIndex = 0;
            for (Property property : directPropertis) {
                String jvmType = property.getTypeUsage().jvmType(resolver);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(loadTypeFor(jvmType), propIndex + 1);
                mv.visitFieldInsn(PUTFIELD, className, property.getName(), jvmType);
                propIndex++;
            }

            mv.visitInsn(RETURN);
            mv.visitMaxs(3, 1 + directPropertis.size());
            mv.visitEnd();
        }

        private Object toFrameObject(String jvmType) {
            switch (jvmType) {
                case "I":
                    return Opcodes.INTEGER;
                default:
                    if (jvmType.charAt(0) != 'L') {
                        throw new UnsupportedOperationException();
                    }
                    if (jvmType.charAt(jvmType.length() - 1) != ';') {
                        throw new UnsupportedOperationException();
                    }
                    return jvmType.substring(1, jvmType.length() - 1);
            }
        }

        private int returnTypeFor(String jvmType) {
            if (jvmType.equals("I")) {
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

        private List<ClassFileDefinition> compile(TypeDefinition typeDefinition) {
            String className = typeDefinition.getQualifiedName().replaceAll("\\.", "/");

            cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
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

        public List<ClassFileDefinition> compile(TurinFile turinFile) {
            List<ClassFileDefinition> classFileDefinitions = new ArrayList<>();

            for (Node node : turinFile.getChildren()) {
                if (node instanceof TypeDefinition) {
                    classFileDefinitions.addAll(compile((TypeDefinition)node));
                } else if (node instanceof Program) {
                    classFileDefinitions.addAll(compile((Program) node));   
                }
            }
            

            return classFileDefinitions;
        }

        private int nParams = 0;
        private int nLocalVars = 0;

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
                VariableDeclaration variableDeclaration = (VariableDeclaration)statement;
                int pos = nParams + nLocalVars;
                nLocalVars += 1;
                return ImmutableList.of(compile(variableDeclaration.getValue()), new Assignment(pos, JvmTypeCategory.from(variableDeclaration.varType(resolver))));
            } else {
                throw new UnsupportedOperationException();
            }
        }

        private BytecodeSequence compile(Expression expression) {
            throw new UnsupportedOperationException();
        }

    }

    public List<ClassFileDefinition> compile(TurinFile turinFile) {
        return new Compilation().compile(turinFile);
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Turin Compiler - " + VERSION);

        File file = new File("/home/federico/repos/turin-programming-language/samples/ranma.to");
        TurinFile turinFile = new Parser().parse(new FileInputStream(file));

        Compiler instance = new Compiler();
        for (ClassFileDefinition classFileDefinition : instance.compile(turinFile)) {
            System.out.println(" [" + classFileDefinition.getName() + "]");
            File classFile = new File("dst/" + classFileDefinition.getName().replaceAll("\\.", "/") + ".class");
            classFile.getParentFile().mkdirs();
            FileOutputStream fos = new FileOutputStream(classFile);
            fos.write(classFileDefinition.getBytecode());
            fos.close();
        }

    }

}
