package me.tomassetti.turin.compiler;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.analysis.InFileResolver;
import me.tomassetti.turin.analysis.Property;
import me.tomassetti.turin.analysis.Resolver;
import me.tomassetti.turin.ast.*;
import org.objectweb.asm.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

public class Compiler {

    private static String VERSION = "Alpha 000";

    private static final int JAVA_8_CLASS_VERSION = 52;

    private Resolver resolver = new InFileResolver();

    private class Compilation {

        private ClassWriter cw = new ClassWriter(0);

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

        private int returnTypeFor(String jvmType) {
            if (jvmType.equals("I")) {
                return IRETURN;
            }
            return ARETURN;
        }

        private List<ClassFileDefinition> compile(TypeDefinition typeDefinition) {

            MethodVisitor mv;
            AnnotationVisitor av0;

            String className = typeDefinition.getQualifiedName().replaceAll("\\.", "/");

            cw.visit(JAVA_8_CLASS_VERSION, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", null);

            for (Property property : typeDefinition.getDirectProperties(resolver)){
                generateField(property);
                generateGetter(property, className);
            }

            {
                mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(Ljava/lang/String;I)V", null, null);
                mv.visitCode();
                mv.visitVarInsn(ALOAD, 0);
                mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
                mv.visitVarInsn(ALOAD, 1);
                Label l0 = new Label();
                mv.visitJumpInsn(IFNONNULL, l0);
                mv.visitTypeInsn(NEW, "java/lang/NullPointerException");
                mv.visitInsn(DUP);
                mv.visitLdcInsn("name cannot be null");
                mv.visitMethodInsn(INVOKESPECIAL, "java/lang/NullPointerException", "<init>", "(Ljava/lang/String;)V", false);
                mv.visitInsn(ATHROW);
                mv.visitLabel(l0);
                mv.visitFrame(Opcodes.F_FULL, 3, new Object[] {className, "java/lang/String", Opcodes.INTEGER}, 0, new Object[] {});

                mv.visitVarInsn(ILOAD, 2);
                Label l1 = new Label();
                mv.visitJumpInsn(IFGE, l1);
                mv.visitTypeInsn(NEW, "java/lang/IllegalArgumentException");
                mv.visitInsn(DUP);
                mv.visitLdcInsn("age should be positive");
                mv.visitMethodInsn(INVOKESPECIAL, "java/lang/IllegalArgumentException", "<init>", "(Ljava/lang/String;)V", false);
                mv.visitInsn(ATHROW);
                mv.visitLabel(l1);
                mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);

                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(ALOAD, 1);
                mv.visitFieldInsn(PUTFIELD, className, "name", "Ljava/lang/String;");
                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(ILOAD, 2);
                mv.visitFieldInsn(PUTFIELD, className, "age", "I");
                mv.visitInsn(RETURN);
                mv.visitMaxs(3, 3);
                mv.visitEnd();
            }
            {
                mv = cw.visitMethod(ACC_PUBLIC, "setAge", "(I)V", null, null);
                mv.visitCode();
                mv.visitVarInsn(ILOAD, 1);
                Label l0 = new Label();
                mv.visitJumpInsn(IFGE, l0);
                mv.visitTypeInsn(NEW, "java/lang/IllegalArgumentException");
                mv.visitInsn(DUP);
                mv.visitLdcInsn("age should be positive");
                mv.visitMethodInsn(INVOKESPECIAL, "java/lang/IllegalArgumentException", "<init>", "(Ljava/lang/String;)V", false);
                mv.visitInsn(ATHROW);
                mv.visitLabel(l0);
                mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(ILOAD, 1);
                mv.visitFieldInsn(PUTFIELD, className, "age", "I");
                mv.visitInsn(RETURN);
                mv.visitMaxs(3, 2);
                mv.visitEnd();
            }
            {
                mv = cw.visitMethod(ACC_PUBLIC, "setName", "(Ljava/lang/String;)V", null, null);
                mv.visitCode();
                mv.visitVarInsn(ALOAD, 1);
                Label l0 = new Label();
                mv.visitJumpInsn(IFNONNULL, l0);
                mv.visitTypeInsn(NEW, "java/lang/NullPointerException");
                mv.visitInsn(DUP);
                mv.visitLdcInsn("name cannot be null");
                mv.visitMethodInsn(INVOKESPECIAL, "java/lang/NullPointerException", "<init>", "(Ljava/lang/String;)V", false);
                mv.visitInsn(ATHROW);
                mv.visitLabel(l0);
                mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(ALOAD, 1);
                mv.visitFieldInsn(PUTFIELD, className, "name", "Ljava/lang/String;");
                mv.visitInsn(RETURN);
                mv.visitMaxs(3, 2);
                mv.visitEnd();
            }
            cw.visitEnd();

            return ImmutableList.of(new ClassFileDefinition(className.replaceAll("/", "."), cw.toByteArray()));
        }

        public List<ClassFileDefinition> compile(TurinFile turinFile) {
            List<ClassFileDefinition> classFileDefinitions = new ArrayList<>();

            for (Node node : turinFile.getChildren()) {
                if (node instanceof TypeDefinition) {
                    classFileDefinitions.addAll(compile((TypeDefinition)node));
                }
            }

            return classFileDefinitions;
        }

    }

    public List<ClassFileDefinition> compile(TurinFile turinFile) {
        return new Compilation().compile(turinFile);
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Turin Compiler - " + VERSION);

        TurinFile turinFile = new TurinFile();

        ReferenceTypeUsage stringType = new ReferenceTypeUsage("String");
        ReferenceTypeUsage intType = new ReferenceTypeUsage("Int");

        PropertyDefinition nameProperty = new PropertyDefinition("name", stringType);

        turinFile.add(nameProperty);

        TypeDefinition mangaCharacter = new TypeDefinition("MangaCharacter");
        PropertyDefinition ageProperty = new PropertyDefinition("age", intType);
        PropertyReference nameRef = new PropertyReference("name");
        mangaCharacter.add(nameRef);
        mangaCharacter.add(ageProperty);

        turinFile.add(mangaCharacter);

        Compiler instance = new Compiler();
        byte[] bytecode = instance.compile(turinFile).get(0).getBytecode();

        FileOutputStream fos = new FileOutputStream("dst/tests/MyMangaCharacter.class");
        fos.write(bytecode);
        fos.close();

    }

}
