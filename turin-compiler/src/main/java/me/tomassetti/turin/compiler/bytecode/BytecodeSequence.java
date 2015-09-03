package me.tomassetti.turin.compiler.bytecode;

import org.objectweb.asm.MethodVisitor;

/**
 * A sequence of instructions to be performed inside of a method.
 */
public abstract class BytecodeSequence {
    public abstract void operate(MethodVisitor mv);
}
