package me.tomassetti.bytecode_generation;

import org.objectweb.asm.MethodVisitor;

/**
 * A sequence of instructions to be performed inside of a method.
 */
public abstract class BytecodeSequence {

    public abstract void operate(MethodVisitor mv);

}
