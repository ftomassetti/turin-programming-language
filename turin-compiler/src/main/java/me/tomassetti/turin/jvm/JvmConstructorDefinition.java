package me.tomassetti.turin.jvm;

public class JvmConstructorDefinition extends JvmInvokableDefinition {

    public JvmConstructorDefinition(String ownerInternalName, String descriptor) {
        super(ownerInternalName, "<init>", descriptor);
    }

}
