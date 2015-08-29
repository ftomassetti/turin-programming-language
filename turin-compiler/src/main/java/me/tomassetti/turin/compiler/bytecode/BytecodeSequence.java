package me.tomassetti.turin.compiler.bytecode;

import me.tomassetti.turin.compiler.*;
import me.tomassetti.turin.compiler.Compiler;
import org.objectweb.asm.MethodVisitor;

/**
* Created by federico on 29/08/15.
*/
public abstract class BytecodeSequence {
    public abstract void operate(MethodVisitor mv);
}
