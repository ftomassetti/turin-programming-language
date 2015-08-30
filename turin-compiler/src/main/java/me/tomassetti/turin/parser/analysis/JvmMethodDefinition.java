package me.tomassetti.turin.parser.analysis;

/**
 * Created by federico on 29/08/15.
 */
public class JvmMethodDefinition {

    private boolean _static;
    private String jvmType;
    private String name;
    private String signature;
    // fake static methods are created by adding an instance which is pushed before invoking the
    // method. For example print in Turin seems static but it is actually a call on System.out
    private JvmStaticFieldDefinition staticField;

    public JvmStaticFieldDefinition getStaticField() {
        return staticField;
    }

    public void setStaticField(JvmStaticFieldDefinition staticField) {
        this.staticField = staticField;
    }

    public JvmMethodDefinition(String jvmType, String name, String signature, boolean _static) {
        this.signature = signature;
        this.name = name;
        this.jvmType = jvmType;
        this._static = _static;
    }

    public String getJvmType() {
        return jvmType;
    }

    public String getName() {
        return name;
    }

    public boolean isStatic() {
        return _static;
    }

    public String getSignature() {
        return signature;
    }
}
