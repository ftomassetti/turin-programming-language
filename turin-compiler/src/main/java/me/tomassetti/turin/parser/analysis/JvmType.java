package me.tomassetti.turin.parser.analysis;

/**
 * Created by federico on 29/08/15.
 */
public class JvmType {

    private String signature;

    public String getSignature() {
        return signature;
    }

    public JvmType(String signature) {
        this.signature = signature;
    }
}
