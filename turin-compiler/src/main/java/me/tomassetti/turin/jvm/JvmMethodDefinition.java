package me.tomassetti.turin.jvm;

public class JvmMethodDefinition extends JvmInvokableDefinition {

    private boolean _static;

    public JvmMethodDefinition(String ownerInternalName, String methodName, String descriptor, boolean _static) {
        super(ownerInternalName, methodName, descriptor);
        this._static = _static;
    }

    public boolean isStatic() {
        return _static;
    }

}
