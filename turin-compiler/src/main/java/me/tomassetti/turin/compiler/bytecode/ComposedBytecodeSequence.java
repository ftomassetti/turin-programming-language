package me.tomassetti.turin.compiler.bytecode;

import org.objectweb.asm.MethodVisitor;

import java.util.List;

public class ComposedBytecodeSequence extends BytecodeSequence {

    private List<BytecodeSequence> components;

    public ComposedBytecodeSequence(List<BytecodeSequence> components) {
        this.components = components;
    }

    @Override
    public void operate(MethodVisitor mv) {
        for (BytecodeSequence component : components) {
            component.operate(mv);
        }
    }
}
