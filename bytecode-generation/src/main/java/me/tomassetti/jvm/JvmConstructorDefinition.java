package me.tomassetti.jvm;

public class JvmConstructorDefinition extends JvmInvokableDefinition {

    public JvmConstructorDefinition(String ownerInternalName, String descriptor) {
        super(ownerInternalName, "<init>", descriptor);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JvmConstructorDefinition)) return false;

        JvmConstructorDefinition that = (JvmConstructorDefinition) o;

        if (!ownerInternalName.equals(that.ownerInternalName)) return false;
        if (!name.equals(that.name)) return false;
        if (!descriptor.equals(that.descriptor)) return false;

        return true;
    }
}
