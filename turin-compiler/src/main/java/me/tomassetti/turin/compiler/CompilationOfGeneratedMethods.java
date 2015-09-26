package me.tomassetti.turin.compiler;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.compiler.bytecode.BytecodeSequence;
import me.tomassetti.turin.compiler.bytecode.NewInvocationBS;
import me.tomassetti.turin.compiler.bytecode.ThrowBS;
import me.tomassetti.turin.compiler.bytecode.pushop.PushStringConst;
import me.tomassetti.turin.compiler.bytecode.pushop.PushThis;
import me.tomassetti.turin.compiler.bytecode.returnop.ReturnFalseBS;
import me.tomassetti.turin.compiler.bytecode.returnop.ReturnTrueBS;
import me.tomassetti.turin.implicit.BasicTypeUsage;
import me.tomassetti.turin.jvm.JvmConstructorDefinition;
import me.tomassetti.turin.jvm.JvmType;
import me.tomassetti.turin.parser.analysis.Property;
import me.tomassetti.turin.parser.ast.TurinTypeDefinition;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.List;
import java.util.stream.Collectors;

public class CompilationOfGeneratedMethods {
    private final Compilation compilation;

    private ClassWriter cw;

    public CompilationOfGeneratedMethods(Compilation compilation, ClassWriter cw) {
        this.compilation = compilation;
        this.cw = cw;
    }

    void enforceConstraint(Property property, MethodVisitor mv, JvmType jvmType, int varIndex) {
        if (property.getTypeUsage().equals(BasicTypeUsage.UINT)) {
            // index 0 is the "this"
            mv.visitVarInsn(OpcodesUtils.loadTypeFor(jvmType), varIndex + 1);
            Label label = new Label();

            // if the value is >= 0 we jump and skip the throw exception
            mv.visitJumpInsn(Opcodes.IFGE, label);
            JvmConstructorDefinition constructor = new JvmConstructorDefinition("java/lang/IllegalArgumentException", "(Ljava/lang/String;)V");
            BytecodeSequence instantiateException = new NewInvocationBS(constructor, ImmutableList.of(new PushStringConst(property.getName() + " should be positive")));
            new ThrowBS(instantiateException).operate(mv);

            mv.visitLabel(label);
        } else if (property.getTypeUsage().isReferenceTypeUsage() && property.getTypeUsage().asReferenceTypeUsage().getQualifiedName(compilation.getResolver()).equals(String.class.getCanonicalName())) {
            // index 0 is the "this"
            mv.visitVarInsn(OpcodesUtils.loadTypeFor(jvmType), varIndex + 1);
            Label label = new Label();

            // if not null skip the throw
            mv.visitJumpInsn(Opcodes.IFNONNULL, label);
            JvmConstructorDefinition constructor = new JvmConstructorDefinition("java/lang/IllegalArgumentException", "(Ljava/lang/String;)V");
            BytecodeSequence instantiateException = new NewInvocationBS(constructor, ImmutableList.of(new PushStringConst(property.getName() + " cannot be null")));
            new ThrowBS(instantiateException).operate(mv);

            mv.visitLabel(label);
        }
    }

    void generateSetter(Property property, String internalClassName) {
        String setterName = property.setterName();
        JvmType jvmType = property.getTypeUsage().jvmType(compilation.getResolver());
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, setterName, "(" + jvmType.getDescriptor() + ")V", "(" + jvmType.getSignature() + ")V", null);
        mv.visitCode();

        enforceConstraint(property, mv, jvmType, 0);

        // Assignment
        PushThis.getInstance().operate(mv);
        mv.visitVarInsn(OpcodesUtils.loadTypeFor(jvmType), 1);
        mv.visitFieldInsn(Opcodes.PUTFIELD, internalClassName, property.getName(), jvmType.getDescriptor());
        mv.visitInsn(Opcodes.RETURN);
        // calculated for us
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    void generateConstructor(TurinTypeDefinition typeDefinition, String className) {
        // TODO consider also inherited properties
        List<Property> directProperties = typeDefinition.getDirectProperties(compilation.getResolver());
        // we exclude all the values with an initial or a default value
        List<Property> directPropertiesAsParameters = directProperties.stream()
                .filter((p) -> !p.hasInitialValue() && !p.hasDefaultValue())
                .collect(Collectors.toList());
        String paramsDescriptor = String.join("", directPropertiesAsParameters.stream().map((dp) -> dp.getTypeUsage().jvmType(compilation.getResolver()).getDescriptor()).collect(Collectors.toList()));
        String paramsSignature = String.join("", directPropertiesAsParameters.stream().map((dp) -> dp.getTypeUsage().jvmType(compilation.getResolver()).getSignature()).collect(Collectors.toList()));
        if (typeDefinition.hasDefaultProperties(compilation.getResolver())) {
            paramsDescriptor += "Ljava/util/Map;";
            paramsSignature  += "Ljava/util/Map<Ljava/lang/String;Ljava/lang/Object;>;";
        }
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "(" + paramsDescriptor + ")V", "(" + paramsSignature + ")V", null);
        mv.visitCode();

        PushThis.getInstance().operate(mv);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Compilation.OBJECT_INTERNAL_NAME, "<init>", "()V", false);

        int propIndex = 0;
        for (Property property : directPropertiesAsParameters) {
            enforceConstraint(property, mv, property.getTypeUsage().jvmType(compilation.getResolver()), propIndex);
            propIndex++;
        }

        propIndex = 0;
        for (Property property : typeDefinition.propertiesAppearingInConstructor(compilation.getResolver())) {
            JvmType jvmType = property.getTypeUsage().jvmType(compilation.getResolver());
            mv.visitVarInsn(Opcodes.ALOAD, Compilation.LOCALVAR_INDEX_FOR_THIS_IN_METHOD);
            mv.visitVarInsn(OpcodesUtils.loadTypeFor(jvmType), propIndex + 1);
            mv.visitFieldInsn(Opcodes.PUTFIELD, className, property.getName(), jvmType.getDescriptor());
            propIndex++;
        }
        List<Property> directPropertiesWithInitialValue = directProperties.stream()
                .filter((p) -> p.hasInitialValue())
                .collect(Collectors.toList());
        for (Property property : directPropertiesWithInitialValue) {
            JvmType jvmType = property.getTypeUsage().jvmType(compilation.getResolver());
            mv.visitVarInsn(Opcodes.ALOAD, Compilation.LOCALVAR_INDEX_FOR_THIS_IN_METHOD);
            compilation.getPushUtils().pushExpression(property.getInitialValue().get()).operate(mv);
            mv.visitFieldInsn(Opcodes.PUTFIELD, className, property.getName(), jvmType.getDescriptor());
            propIndex++;
        }

        // now we should get values from the defaultParamsMap and assign them
        // to fields
        TODO
        we probably want to create a
                we should also enforce the constraints

        mv.visitInsn(Opcodes.RETURN);
        // calculated for us
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    void generateEqualsMethod(TurinTypeDefinition typeDefinition, String internalClassName) {
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "equals", "(" + Compilation.OBJECT_DESCRIPTOR + ")Z", "(" + Compilation.OBJECT_DESCRIPTOR + ")Z", null);
        mv.visitCode();

        // if (this == o) return true;
        mv.visitVarInsn(Opcodes.ALOAD, Compilation.LOCALVAR_INDEX_FOR_THIS_IN_METHOD);
        mv.visitVarInsn(Opcodes.ALOAD, Compilation.LOCALVAR_INDEX_FOR_PARAM_0);
        Label paramAndThisAreNotTheSame = new Label();
        mv.visitJumpInsn(Opcodes.IF_ACMPNE, paramAndThisAreNotTheSame);
        new ReturnTrueBS().operate(mv);
        mv.visitLabel(paramAndThisAreNotTheSame);

        // if (o == null || getClass() != o.getClass()) return false;
        mv.visitVarInsn(Opcodes.ALOAD, Compilation.LOCALVAR_INDEX_FOR_PARAM_0);
        Label paramIsNull = new Label();
        mv.visitJumpInsn(Opcodes.IFNULL, paramIsNull);
        mv.visitVarInsn(Opcodes.ALOAD, Compilation.LOCALVAR_INDEX_FOR_THIS_IN_METHOD);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
        mv.visitVarInsn(Opcodes.ALOAD, Compilation.LOCALVAR_INDEX_FOR_PARAM_0);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
        Label paramHasSameClassAsThis = new Label();
        mv.visitJumpInsn(Opcodes.IF_ACMPEQ, paramHasSameClassAsThis);
        mv.visitLabel(paramIsNull);
        new ReturnFalseBS().operate(mv);
        mv.visitLabel(paramHasSameClassAsThis);

        // MyType other = (MyType) o;
        mv.visitVarInsn(Opcodes.ALOAD, Compilation.LOCALVAR_INDEX_FOR_PARAM_0);
        mv.visitTypeInsn(Opcodes.CHECKCAST, internalClassName);
        final int localvar_index_for_other = 2;
        mv.visitVarInsn(Opcodes.ASTORE, localvar_index_for_other);

        // if (!this.aField.equals(other.aField)) return false;
        for (Property property : typeDefinition.getAllProperties(compilation.getResolver())) {
            TypeUsage propertyTypeUsage = property.getTypeUsage();
            String fieldTypeDescriptor = propertyTypeUsage.jvmType(compilation.getResolver()).getDescriptor();

            mv.visitVarInsn(Opcodes.ALOAD, Compilation.LOCALVAR_INDEX_FOR_THIS_IN_METHOD);
            mv.visitFieldInsn(Opcodes.GETFIELD, internalClassName, property.getName(), fieldTypeDescriptor);
            mv.visitVarInsn(Opcodes.ALOAD, localvar_index_for_other);
            mv.visitFieldInsn(Opcodes.GETFIELD, internalClassName, property.getName(), fieldTypeDescriptor);
            Label propertyIsEqual = new Label();

            if (propertyTypeUsage.isPrimitive()) {
                if (propertyTypeUsage.asPrimitiveTypeUsage().isLong()) {
                    mv.visitInsn(Opcodes.LCMP);
                    mv.visitJumpInsn(Opcodes.IFEQ, propertyIsEqual);
                } else if (propertyTypeUsage.asPrimitiveTypeUsage().isFloat()) {
                    mv.visitInsn(Opcodes.FCMPL);
                    mv.visitJumpInsn(Opcodes.IFEQ, propertyIsEqual);
                } else if (propertyTypeUsage.asPrimitiveTypeUsage().isDouble()) {
                    mv.visitInsn(Opcodes.DCMPL);
                    mv.visitJumpInsn(Opcodes.IFEQ, propertyIsEqual);
                } else {
                    mv.visitJumpInsn(Opcodes.IF_ICMPEQ, propertyIsEqual);
                }
            } else {
                boolean isInterface = propertyTypeUsage.asReferenceTypeUsage().isInterface(compilation.getResolver());
                if (isInterface) {
                    mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, propertyTypeUsage.jvmType(compilation.getResolver()).getInternalName(), "equals", "(Ljava/lang/Object;)Z", true);
                } else {
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, propertyTypeUsage.jvmType(compilation.getResolver()).getInternalName(), "equals", "(Ljava/lang/Object;)Z", false);
                }
                mv.visitJumpInsn(Opcodes.IFNE, propertyIsEqual);
            }

            new ReturnFalseBS().operate(mv);
            mv.visitLabel(propertyIsEqual);
        }

        new ReturnTrueBS().operate(mv);

        // calculated for us
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    void generateHashCodeMethod(TurinTypeDefinition typeDefinition, String internalClassName) {
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "hashCode", "()I", "()I", null);
        mv.visitCode();

        final int localvar_index_of_result = 1;

        // int result = 1;
        mv.visitInsn(Opcodes.ICONST_1);
        mv.visitVarInsn(Opcodes.ISTORE, localvar_index_of_result);

        for (Property property : typeDefinition.getAllProperties(compilation.getResolver())) {
            // result = 31 * result + this.aField.hashCode();
            TypeUsage propertyTypeUsage = property.getTypeUsage();
            String fieldTypeDescriptor = propertyTypeUsage.jvmType(compilation.getResolver()).getDescriptor();

            // 31 is just a prime number by which we multiply the current value of result
            mv.visitIntInsn(Opcodes.BIPUSH, 31);
            mv.visitVarInsn(Opcodes.ILOAD, localvar_index_of_result);
            mv.visitInsn(Opcodes.IMUL);

            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, internalClassName, property.getName(), fieldTypeDescriptor);

            if (propertyTypeUsage.isPrimitive()) {
                if (propertyTypeUsage.asPrimitiveTypeUsage().isLong()) {
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Long", "hashCode", "(J)I", false);
                } else if (propertyTypeUsage.asPrimitiveTypeUsage().isFloat()) {
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Float", "hashCode", "(F)I", false);
                } else if (propertyTypeUsage.asPrimitiveTypeUsage().isDouble()) {
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Double", "hashCode", "(D)I", false);
                } else {
                    // nothing to do, the value is already on the stack and we can sum it directly
                }
            } else {
                boolean isInterface = propertyTypeUsage.asReferenceTypeUsage().isInterface(compilation.getResolver());
                if (isInterface) {
                    mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, propertyTypeUsage.jvmType(compilation.getResolver()).getInternalName(), "hashCode", "()I", true);
                } else {
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, propertyTypeUsage.jvmType(compilation.getResolver()).getInternalName(), "hashCode", "()I", false);
                }
            }

            mv.visitInsn(Opcodes.IADD);
            mv.visitVarInsn(Opcodes.ISTORE, localvar_index_of_result);
        }

        // return result;
        mv.visitVarInsn(Opcodes.ILOAD, localvar_index_of_result);
        mv.visitInsn(Opcodes.IRETURN);

        // calculated for us
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
}