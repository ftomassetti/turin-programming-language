package me.tomassetti.turin.compiler;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.compiler.bytecode.*;
import me.tomassetti.turin.compiler.bytecode.logicalop.CastBS;
import me.tomassetti.turin.compiler.bytecode.pushop.PushInstanceField;
import me.tomassetti.turin.compiler.bytecode.pushop.PushLocalVar;
import me.tomassetti.turin.compiler.bytecode.pushop.PushStringConst;
import me.tomassetti.turin.compiler.bytecode.pushop.PushThis;
import me.tomassetti.turin.compiler.bytecode.returnop.ReturnFalseBS;
import me.tomassetti.turin.compiler.bytecode.returnop.ReturnTrueBS;
import me.tomassetti.turin.implicit.BasicTypeUsage;
import me.tomassetti.turin.jvm.JvmConstructorDefinition;
import me.tomassetti.turin.jvm.JvmFieldDefinition;
import me.tomassetti.turin.jvm.JvmMethodDefinition;
import me.tomassetti.turin.jvm.JvmType;
import me.tomassetti.turin.parser.analysis.Property;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
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

    void enforceConstraint(Property property, MethodVisitor mv, JvmType jvmType, BytecodeSequence getValue) {
        if (property.getTypeUsage().equals(BasicTypeUsage.UINT)) {
            // index 0 is the "this"
            getValue.operate(mv);
            Label label = new Label();

            // if the value is >= 0 we jump and skip the throw exception
            mv.visitJumpInsn(Opcodes.IFGE, label);
            JvmConstructorDefinition constructor = new JvmConstructorDefinition("java/lang/IllegalArgumentException", "(Ljava/lang/String;)V");
            BytecodeSequence instantiateException = new NewInvocationBS(constructor, ImmutableList.of(new PushStringConst(property.getName() + " should be positive")));
            new ThrowBS(instantiateException).operate(mv);

            mv.visitLabel(label);
        } else if (property.getTypeUsage().isReferenceTypeUsage() && property.getTypeUsage().asReferenceTypeUsage().getQualifiedName(compilation.getResolver()).equals(String.class.getCanonicalName())) {
            // index 0 is the "this"
            getValue.operate(mv);
            Label label = new Label();

            // if not null skip the throw
            mv.visitJumpInsn(Opcodes.IFNONNULL, label);
            JvmConstructorDefinition constructor = new JvmConstructorDefinition("java/lang/IllegalArgumentException", "(Ljava/lang/String;)V");
            BytecodeSequence instantiateException = new NewInvocationBS(constructor, ImmutableList.of(new PushStringConst(property.getName() + " cannot be null")));
            new ThrowBS(instantiateException).operate(mv);

            mv.visitLabel(label);
        }
    }


    void enforceConstraint(Property property, MethodVisitor mv, JvmType jvmType, int varIndex) {
        enforceConstraint(property, mv, jvmType, new BytecodeSequence() {
            @Override
            public void operate(MethodVisitor mv) {
                mv.visitVarInsn(OpcodesUtils.loadTypeFor(jvmType), varIndex + 1);
            }
        });
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
        SymbolResolver resolver = compilation.getResolver();
        List<Property> directProperties = typeDefinition.getDirectProperties(resolver);

        //
        // Define the constructor prototype
        //
        List<Property> directPropertiesAsParameters = typeDefinition.propertiesAppearingInConstructor(resolver);
        String paramsDescriptor = String.join("", directPropertiesAsParameters.stream().map((dp) -> dp.getTypeUsage().jvmType(resolver).getDescriptor()).collect(Collectors.toList()));
        String paramsSignature = String.join("", directPropertiesAsParameters.stream().map((dp) -> dp.getTypeUsage().jvmType(resolver).getSignature()).collect(Collectors.toList()));
        if (typeDefinition.hasDefaultProperties(resolver)) {
            paramsDescriptor += "Ljava/util/Map;";
            paramsSignature  += "Ljava/util/Map<Ljava/lang/String;Ljava/lang/Object;>;";
        }
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "(" + paramsDescriptor + ")V", "(" + paramsSignature + ")V", null);
        mv.visitCode();

        //
        // Invoke super constructor
        //

        PushThis.getInstance().operate(mv);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Compilation.OBJECT_INTERNAL_NAME, "<init>", "()V", false);

        //
        // Assign the properties passed explicitly
        //

        assignPropertiesPassedExplicitely(typeDefinition, className, resolver, directPropertiesAsParameters, mv);

        //
        // Assign the properties with initial value
        //

        assignPropertiesWithInitialValue(className, resolver, directProperties, mv);

        // now we should get values from the defaultParamsMap and assign them
        // to fields
        if (typeDefinition.hasDefaultProperties(resolver)) {
            assignDefaultPropertiesFromMapParam(typeDefinition, className, resolver, mv);
        }

        mv.visitInsn(Opcodes.RETURN);
        // calculated for us
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void assignDefaultPropertiesFromMapParam(TurinTypeDefinition typeDefinition, final String className, SymbolResolver resolver, MethodVisitor mv) {
        int localVarIndex = 1 + typeDefinition.propertiesAppearingInConstructor(resolver).size();
        for (Property property : typeDefinition.defaultPropeties(resolver)) {
            JvmType jvmType = property.getTypeUsage().jvmType(resolver);
            BytecodeSequence isPropertyInMap = new ComposedBytecodeSequence(
                    // we push the map
                    new PushLocalVar(Opcodes.ALOAD, localVarIndex),
                    new PushStringConst(property.getName()),
                    new MethodInvocationBS(new JvmMethodDefinition("java/util/Map", "containsKey", "(Ljava/lang/Object;)Z", false, true)));
            BytecodeSequence getSequence = new ComposedBytecodeSequence(
                // we push the map
                new PushLocalVar(Opcodes.ALOAD, localVarIndex),
                new PushStringConst(property.getName()),
                new MethodInvocationBS(new JvmMethodDefinition("java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;", false, true))
            );
            String propertyBoxedType = property.getTypeUsage().isPrimitive() ?
                      property.getTypeUsage().asPrimitiveTypeUsage().getBoxType().jvmType(resolver).getInternalName()
                    : property.getTypeUsage().jvmType(resolver).getInternalName();
            BytecodeSequence assignPropertyFromMap = new ComposedBytecodeSequence(
                    PushThis.getInstance(),
                    getSequence,
                    new CastBS(propertyBoxedType),
                    property.getTypeUsage().isPrimitive() ?
                              new UnboxBS(property.getTypeUsage().jvmType(resolver))
                            : NoOp.getInstance(),
                    new BytecodeSequence() {
                        @Override
                        public void operate(MethodVisitor mv) {
                            mv.visitFieldInsn(Opcodes.PUTFIELD, className, property.getName(), jvmType.getDescriptor());
                        }
                    });
            BytecodeSequence assignPropertyFromDefaultValue =  new ComposedBytecodeSequence(
                    PushThis.getInstance(),
                    compilation.getPushUtils().pushExpression(property.getDefaultValue().get()),
                    new BytecodeSequence() {
                        @Override
                        public void operate(MethodVisitor mv) {
                            mv.visitFieldInsn(Opcodes.PUTFIELD, className, property.getName(), jvmType.getDescriptor());
                        }
                    });
            new IfBS(isPropertyInMap, assignPropertyFromMap, assignPropertyFromDefaultValue).operate(mv);
            JvmFieldDefinition jvmFieldDefinition = new JvmFieldDefinition(className, property.getName(), property.getTypeUsage().jvmType(resolver).getDescriptor(),false);
            enforceConstraint(property, mv, jvmType, new PushInstanceField(jvmFieldDefinition));
        }
    }

    private void assignPropertiesWithInitialValue(String className, SymbolResolver resolver, List<Property> directProperties, MethodVisitor mv) {
        List<Property> directPropertiesWithInitialValue = directProperties.stream()
                .filter((p) -> p.hasInitialValue())
                .collect(Collectors.toList());
        for (Property property : directPropertiesWithInitialValue) {
            JvmType jvmType = property.getTypeUsage().jvmType(resolver);
            mv.visitVarInsn(Opcodes.ALOAD, Compilation.LOCALVAR_INDEX_FOR_THIS_IN_METHOD);
            compilation.getPushUtils().pushExpression(property.getInitialValue().get()).operate(mv);
            mv.visitFieldInsn(Opcodes.PUTFIELD, className, property.getName(), jvmType.getDescriptor());
            JvmFieldDefinition jvmFieldDefinition = new JvmFieldDefinition(className, property.getName(), property.getTypeUsage().jvmType(resolver).getDescriptor(),false);
            enforceConstraint(property, mv, property.getTypeUsage().jvmType(resolver), new PushInstanceField(jvmFieldDefinition));
        }
    }

    private void assignPropertiesPassedExplicitely(TurinTypeDefinition typeDefinition, String className, SymbolResolver resolver, List<Property> directPropertiesAsParameters, MethodVisitor mv) {
        int propIndex = 0;
        for (Property property : directPropertiesAsParameters) {
            enforceConstraint(property, mv, property.getTypeUsage().jvmType(resolver), propIndex);
            propIndex++;
        }

        propIndex = 0;
        for (Property property : typeDefinition.propertiesAppearingInConstructor(resolver)) {
            JvmType jvmType = property.getTypeUsage().jvmType(resolver);
            mv.visitVarInsn(Opcodes.ALOAD, Compilation.LOCALVAR_INDEX_FOR_THIS_IN_METHOD);
            mv.visitVarInsn(OpcodesUtils.loadTypeFor(jvmType), propIndex + 1);
            mv.visitFieldInsn(Opcodes.PUTFIELD, className, property.getName(), jvmType.getDescriptor());
            propIndex++;
        }
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