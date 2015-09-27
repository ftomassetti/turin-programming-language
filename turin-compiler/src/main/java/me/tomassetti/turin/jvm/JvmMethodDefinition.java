package me.tomassetti.turin.jvm;

import sun.reflect.generics.parser.SignatureParser;
import sun.reflect.generics.tree.*;

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

    public boolean isParamPrimitive(int i) {
        MethodTypeSignature methodTypeSignature = SignatureParser.make().parseMethodSig(descriptor);
        TypeSignature typeSignature = methodTypeSignature.getParameterTypes()[i];
        if (typeSignature instanceof ClassTypeSignature) {
            return false;
        } else if (typeSignature instanceof BaseType) {
            return true;
        } else {
            throw new UnsupportedOperationException(typeSignature.getClass().getCanonicalName());
        }
    }

    public boolean isDeclaredOnInterface() {
        return declaredOnInterface;
    }
}
