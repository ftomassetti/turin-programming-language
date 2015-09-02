package me.tomassetti.turin.compiler.bytecode;

import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class PushStaticField extends BytecodeSequence {

    private JvmFieldDefinition fieldDefinition;

    public PushStaticField(JvmFieldDefinition fieldDefinition) {
        this.fieldDefinition = fieldDefinition;
    }

    @Override
    public void operate(MethodVisitor mv) {
        if (fieldDefinition.isStatic()) {
            mv.visitFieldInsn(GETSTATIC, fieldDefinition.getDeclaringType(), fieldDefinition.getName(), fieldDefinition.getTypeName());
        } else {
            throw new UnsupportedOperationException();
        }
    }

}
