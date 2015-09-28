package me.tomassetti.turin.parser.analysis.resolvers.compiled;

import javassist.*;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;
import me.tomassetti.turin.parser.analysis.resolvers.TypeResolver;
import me.tomassetti.turin.parser.ast.FormalParameter;
import me.tomassetti.turin.parser.ast.FunctionDefinition;
import me.tomassetti.turin.parser.ast.TypeDefinition;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Resolve types by looking in a dir of class files.
 */
public class DirClassesTypeResolver implements TypeResolver {

    private class ClasspathElement {
        private File file;
        private String path;

        public ClasspathElement(File file, String path) {
            this.file = file;
            this.path = path;
        }

        InputStream toInputStream() throws IOException {
            return new FileInputStream(file);
        }

        CtClass toCtClass() throws IOException {
            InputStream is = toInputStream();
            ClassPool classPool = ClassPoolFactory.INSTANCE.getClassPool();
            CtClass ctClass = classPool.makeClass(is);
            return ctClass;
        }

        public URL toURL() {
            String urlContent = "file:"+file.getAbsolutePath();
            try {
                return new URL(urlContent);
            } catch (MalformedURLException e) {
                throw new RuntimeException("URL was: " + urlContent, e);
            }
        }
    }

    private Map<String, ClasspathElement> classpathElements = new HashMap<>();
    private Map<String, ClasspathElement> functionElements = new HashMap<>();

    private class DirClassesClassPath implements ClassPath {

        @Override
        public InputStream openClassfile(String qualifiedName) throws NotFoundException {
            try {
                if (classpathElements.containsKey(qualifiedName)) {
                    return classpathElements.get(qualifiedName).toInputStream();
                } else {
                    return null;
                }
            } catch (IOException e) {
                throw new NotFoundException(e.getMessage());
            }
        }

        @Override
        public URL find(String qualifiedName) {
            if (classpathElements.containsKey(qualifiedName)) {
                return classpathElements.get(qualifiedName).toURL();
            } else {
                return null;
            }
        }

        @Override
        public void close() {
            // nothing to do here
        }
    }

    private File dir;

    /**
     * Note that it adds itself in the global ClassPool.
     */
    public DirClassesTypeResolver(File dir) throws IOException {
        if (!dir.exists() || !dir.isDirectory()) {
            throw new IllegalArgumentException("Not existing or not a directory: " + dir.getPath());
        }
        this.dir = dir;
        explore(dir);
        ClassPoolFactory.INSTANCE.addJar(new DirClassesClassPath());
    }

    private void explore(File file) {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                explore(child);
            }
        } else if (file.isFile()) {
            if (file.getName().endsWith(".class")) {
                if (file.getName().startsWith(FunctionDefinition.CLASS_PREFIX)) {
                    String name = classFileToFunctionName(file, dir);
                    functionElements.put(name, new ClasspathElement(file, name));
                } else {
                    String name = classFileToClassName(file, dir);
                    classpathElements.put(name, new ClasspathElement(file, name));
                }
            }
        }
    }

    private String classFileToClassName(File classFile, File root){
        String absPathFile = classFile.getAbsolutePath();
        String absPathRoot = root.getAbsolutePath();
        if (!(absPathFile.length() > absPathRoot.length())){
            throw new IllegalStateException();
        }
        String relativePath = absPathFile.substring(absPathRoot.length());
        if (!relativePath.endsWith(".class")){
            throw new IllegalStateException();
        }
        String className = relativePath.substring(0, relativePath.length() - ".class".length());
        className = className.replaceAll("/", ".");
        className = className.replaceAll("\\$", ".");
        return className;
    }

    private String classFileToFunctionName(File classFile, File root){
        String absPathFile = classFile.getParentFile().getAbsolutePath();
        String absPathRoot = root.getAbsolutePath();
        if (!(absPathFile.length() > absPathRoot.length())){
            throw new IllegalStateException();
        }
        String relativePath = absPathFile.substring(absPathRoot.length());
        String functionName = relativePath;
        functionName = functionName.replaceAll("/", ".");
        functionName = functionName.replaceAll("\\$", ".");
        functionName += ".";
        functionName += classFile.getName().substring(FunctionDefinition.CLASS_PREFIX.length());
        functionName = functionName.substring(0, functionName.length() - ".class".length());
        if (functionName.startsWith(".")) {
            functionName = functionName.substring(1);
        }
        return functionName;
    }

    @Override
    public Optional<TypeDefinition> resolveAbsoluteTypeName(String typeName) {
        if (classpathElements.containsKey(typeName)) {
            try {
                CtClass ctClass = classpathElements.get(typeName).toCtClass();
                return Optional.of(new JavassistTypeDefinition(ctClass));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<FunctionDefinition> resolveAbsoluteFunctionName(String typeName) {
        if (functionElements.containsKey(typeName)) {
            try {
                CtClass ctClass = functionElements.get(typeName).toCtClass();
                if (ctClass.getDeclaredMethods().length != 1) {
                    throw new UnsupportedOperationException();
                }
                CtMethod invokeMethod = ctClass.getDeclaredMethods()[0];
                if (!invokeMethod.getName().equals(FunctionDefinition.INVOKE_METHOD_NAME)) {
                    throw new UnsupportedOperationException();
                }
                // necessary to get local var names
                MethodInfo methodInfo = invokeMethod.getMethodInfo();
                CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
                LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);

                TypeUsage returnType = JavassistTypeDefinitionFactory.toTypeUsage(invokeMethod.getReturnType());
                List<FormalParameter> formalParameters = new ArrayList<>();

                int i=0;
                for (CtClass paramType : invokeMethod.getParameterTypes()) {
                    TypeUsage type =JavassistTypeDefinitionFactory.toTypeUsage(paramType);
                    String paramName = attr.variableName(i);
                    formalParameters.add(new FormalParameter(type, paramName));
                    i++;
                }
                FunctionDefinition functionDefinition = new LoadedFunctionDefinition(typeName, returnType, formalParameters);
                return Optional.of(functionDefinition);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (NotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            return Optional.empty();
        }
    }

}
