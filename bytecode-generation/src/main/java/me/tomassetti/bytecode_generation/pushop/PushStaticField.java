package me.tomassetti.bytecode_generation.pushop;

import me.tomassetti.bytecode_generation.BytecodeSequence;
import me.tomassetti.jvm.JvmFieldDefinition;
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
            mv.visitFieldInsn(GETSTATIC, fieldDefinition.getOwnerInternalName(), fieldDefinition.getFieldName(), fieldDefinition.getDescriptor());
        } else {
            throw new UnsupportedOperationException();
        }
    }

}
