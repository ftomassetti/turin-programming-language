package me.tomassetti.turin.parser.analysis;

public class JvmConstructorDefinition extends JvmInvokableDefinition {

    public JvmConstructorDefinition(String jvmType, String signature) {
        super(signature, "<init>", jvmType);
        if (jvmType.contains(".")) {
            jvmType = jvmType.replaceAll("\\.", "/");
        }
        if (signature.contains(".")) {
            signature = signature.replaceAll("\\.", "/");
        }
    }

}
