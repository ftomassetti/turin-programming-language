package me.tomassetti.turin.jvm;

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

    public JvmMethodDefinition(String ownerInternalName, String methodName, String descriptor, boolean _static) {
        super(ownerInternalName, methodName, descriptor);
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
