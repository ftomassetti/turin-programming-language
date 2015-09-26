package me.tomassetti.turin.jvm;

import javassist.bytecode.SignatureAttribute;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;
import sun.reflect.generics.parser.SignatureParser;
import sun.reflect.generics.tree.ClassTypeSignature;
import sun.reflect.generics.tree.MethodTypeSignature;
import sun.reflect.generics.tree.TypeSignature;

public class JvmMethodDefinition extends JvmInvokableDefinition {

    private boolean _static;

    public JvmMethodDefinition(String ownerInternalName, String methodName, String descriptor, boolean _static) {
        super(ownerInternalName, methodName, descriptor);
        this._static = _static;
    }

    public boolean isStatic() {
        return _static;
    }

    public boolean isParamPrimitive(int i) {
        MethodTypeSignature methodTypeSignature = SignatureParser.make().parseMethodSig(descriptor);
        TypeSignature typeSignature = methodTypeSignature.getParameterTypes()[i];
        if (typeSignature instanceof ClassTypeSignature) {
            return false;
        } else {
            throw new UnsupportedOperationException(typeSignature.getClass().getCanonicalName());
        }
        //return new JvmType().isPrimitive();
    }
}
