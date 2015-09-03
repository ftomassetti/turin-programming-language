package me.tomassetti.turin.jvm;

/**
 * Created by federico on 03/09/15.
 */
public class JvmInvokableDefinition {
    protected String jvmType;
    protected String name;
    protected String signature;

    public JvmInvokableDefinition(String signature, String name, String jvmType) {
        this.signature = signature;
        this.name = name;
        this.jvmType = jvmType;
    }

    public String getJvmType() {
        return jvmType;
    }

    public String getName() {
        return name;
    }

    public String getSignature() {
        return signature;
    }
}
