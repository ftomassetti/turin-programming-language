package me.tomassetti.jvm;

public class JvmMethodDefinition extends JvmInvokableDefinition {

    private boolean _static;
    private boolean declaredOnInterface;

    public JvmMethodDefinition(String ownerInternalName, String methodName, String descriptor, boolean _static, boolean declaredOnInterface) {
        super(ownerInternalName, methodName, descriptor);
        this._static = _static;
        this.declaredOnInterface = declaredOnInterface;
    }

    public boolean isStatic() {
        return _static;
    }

    public boolean isDeclaredOnInterface() {
        return declaredOnInterface;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JvmMethodDefinition)) return false;

        JvmMethodDefinition that = (JvmMethodDefinition) o;

        if (_static != that._static) return false;
        if (declaredOnInterface != that.declaredOnInterface) return false;
        if (!ownerInternalName.equals(that.ownerInternalName)) return false;
        if (!name.equals(that.name)) return false;
        if (!descriptor.equals(that.descriptor)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (_static ? 1 : 0);
        result = 31 * result + (declaredOnInterface ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "JvmMethodDefinition{" +
                "_static=" + _static +
                ", declaredOnInterface=" + declaredOnInterface +
                '}';
    }
}
