package me.tomassetti.jvm;

import com.google.common.collect.ImmutableSet;

import java.util.Arrays;
import java.util.Set;

public final class JvmNameUtils {

    /**
     * See http://docs.oracle.com/javase/specs/jls/se8/html/jls-3.html#jls-3.9
     */
    public static String[] KEYWORDS = new String[]{
        "abstract", "continue", "for", "new", "switch",
        "assert", "default", "if", "package", "synchronized",
        "boolean", "do", "goto", "private", "this",
        "break", "double", "implements", "protected", "throw",
        "byte", "else", "import", "public", "throws",
        "case", "enum", "instanceof", "return", "transient",
        "catch", "extends", "int", "short", "try",
        "char", "final", "interface", "static", "void",
        "class", "finally", "long", "strictfp", "volatile",
        "const", "float", "native", "super", "while"
    };

    private JvmNameUtils() {
        // prevent instantiation
    }

    public static boolean isValidJavaIdentifier(String name){
        if (name == null) {
            throw new IllegalArgumentException();
        }
        if (name.isEmpty()) {
            return false;
        }
        if (Arrays.asList(KEYWORDS).contains(name)) {
            return false;
        }
        char firstChar = name.charAt(0);
        if (firstChar != '$' && firstChar != '_' && !Character.isLetter(firstChar)) {
            return false;
        }
        for (int i=0; i<name.length(); i++){
            char c = name.charAt(i);
            if (c != '$' && c != '_' && !Character.isLetter(c) && !Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    public static String canonicalToInternal(String canonicalName) {
        return canonicalName.replaceAll("\\.", "/");
    }

    public static boolean isValidInternalName(String internalName) {
        if (internalName == null) {
            throw new IllegalArgumentException();
        }
        if (internalName.isEmpty()) {
            return false;
        }
        // the part would be discarded, we have to check explicitly
        if (internalName.endsWith("/")){
            return false;
        }
        String[] parts = internalName.split("/");
        for (String part : parts) {
            if (!isValidJavaIdentifier(part)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isValidQualifiedName(String name) {
        if (name == null) {
            throw new IllegalArgumentException();
        }
        if (name.isEmpty()) {
            return false;
        }
        // the part would be discarded, we have to check explicitly
        if (name.endsWith(".")){
            return false;
        }
        String[] parts = name.split("\\.");
        for (String part : parts) {
            if (!isValidJavaIdentifier(part)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isSimpleName(String name) {
        return -1 == name.indexOf('.');
    }

    public static boolean isPrimitiveTypeName(String name) {
        Set<String> names = ImmutableSet.of("boolean", "char", "byte", "short", "int", "long", "float", "double");
        return names.contains(name);
    }

    public static String canonicalToSimple(String qualifiedName) {
        String[] parts = qualifiedName.split("\\.");
        return parts[parts.length - 1];
    }

    public static String getPackagePart(String qualifiedName) {
        int index = qualifiedName.lastIndexOf('.');
        if (index == -1) {
            throw new IllegalArgumentException();
        }
        return qualifiedName.substring(0, index);
    }

    public static String getSimplePart(String qualifiedName) {
        int index = qualifiedName.lastIndexOf('.');
        if (index == -1) {
            throw new IllegalArgumentException();
        }
        return qualifiedName.substring(index + 1);
    }

    public static String canonicalToDescriptor(String canonicalName) {
        return "L" + canonicalToInternal(canonicalName) + ";";
    }
}
