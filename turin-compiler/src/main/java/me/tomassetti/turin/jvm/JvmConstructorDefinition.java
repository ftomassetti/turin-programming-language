package me.tomassetti.turin.jvm;

public class JvmConstructorDefinition extends JvmInvokableDefinition {

    public JvmConstructorDefinition(String jvmType, String signature) {
        super(signature, "<init>", jvmType);
    }

}
