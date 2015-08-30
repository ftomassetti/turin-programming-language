package me.tomassetti.turin.parser.analysis;

/**
 * Created by federico on 29/08/15.
 */
public class JvmStaticFieldDefinition {

    private String jvmType;
    private String name;
    private String signature;

    public JvmStaticFieldDefinition(String jvmType, String name, String signature) {
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
