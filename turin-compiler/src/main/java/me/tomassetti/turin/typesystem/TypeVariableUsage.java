package me.tomassetti.turin.typesystem;

import java.util.List;

public interface TypeVariableUsage extends TypeUsage {

    public static class GenericDeclaration {

        private String className;
        private String methodSignature;
        private String constructorSignature;

        public boolean isDeclaredOnClass() {
            return methodSignature == null && constructorSignature == null;
        }

        public boolean isDeclaredOnMethod() {
            return methodSignature != null;
        }

        public boolean isDeclaredOnConstructor() {
            return constructorSignature != null;
        }

        private GenericDeclaration(String className, String constructorSignature, String methodSignature) {
            this.className = className;
            this.constructorSignature = constructorSignature;
            this.methodSignature = methodSignature;
        }

        public static GenericDeclaration onClass(String className) {
            return new GenericDeclaration(className, null, null);
        }

        public static GenericDeclaration onMethod(String className, String methodSignature) {
            return new GenericDeclaration(className, methodSignature, null);
        }

        public static GenericDeclaration onConstructor(String className, String constructorSignature) {
            return new GenericDeclaration(className, null, constructorSignature);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof GenericDeclaration)) return false;

            GenericDeclaration that = (GenericDeclaration) o;

            if (className != null ? !className.equals(that.className) : that.className != null) return false;
            if (constructorSignature != null ? !constructorSignature.equals(that.constructorSignature) : that.constructorSignature != null)
                return false;
            if (methodSignature != null ? !methodSignature.equals(that.methodSignature) : that.methodSignature != null)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = className != null ? className.hashCode() : 0;
            result = 31 * result + (methodSignature != null ? methodSignature.hashCode() : 0);
            result = 31 * result + (constructorSignature != null ? constructorSignature.hashCode() : 0);
            return result;
        }
    }

    String getName();
    GenericDeclaration getGenericDeclaration();
    List<TypeUsage> getBounds();
}
