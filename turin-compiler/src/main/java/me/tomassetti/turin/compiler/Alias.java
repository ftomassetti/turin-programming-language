package me.tomassetti.turin.compiler;

import me.tomassetti.bytecode_generation.BytecodeSequence;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

public class Alias {

    private BytecodeSequence bytecodeSequence;
    private TypeUsage type;

    public Alias(BytecodeSequence bytecodeSequence, TypeUsage type) {
        this.bytecodeSequence = bytecodeSequence;
        this.type = type;
    }

    public BytecodeSequence getBytecodeSequence() {
        return bytecodeSequence;
    }

    public TypeUsage getType() {
        return type;
    }
}
