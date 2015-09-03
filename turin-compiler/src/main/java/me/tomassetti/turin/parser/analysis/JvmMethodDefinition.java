package me.tomassetti.turin.parser.analysis;

public class JvmMethodDefinition extends JvmInvokableDefinition {

    private boolean _static;
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
        super(signature, name, jvmType);
        if (jvmType.contains(".")) {
            jvmType = jvmType.replaceAll("\\.", "/");
        }
        if (signature.contains(".")) {
            signature = signature.replaceAll("\\.", "/");
        }
        this._static = _static;
    }

    public boolean isStatic() {
        return _static;
    }

    public boolean isOnInterface() {
        // TODO to be correctly implemented
        return false;
    }
}
