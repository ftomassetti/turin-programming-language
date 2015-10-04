package me.tomassetti.bytecode_generation;

import com.google.common.collect.ImmutableList;
import org.objectweb.asm.MethodVisitor;

import java.util.List;

public class ComposedBytecodeSequence extends BytecodeSequence {

    private List<BytecodeSequence> components;

    public ComposedBytecodeSequence(BytecodeSequence... components) {
        this(ImmutableList.<BytecodeSequence>builder().add(components).build());
    }

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
