package me.tomassetti.turin.resolvers.compiled;

import javassist.ClassPath;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;
import me.tomassetti.turin.definitions.TypeDefinition;
import me.tomassetti.turin.resolvers.InFileSymbolResolver;
import me.tomassetti.turin.resolvers.TypeResolver;
import me.tomassetti.turin.parser.ast.FormalParameterNode;
import me.tomassetti.turin.parser.ast.invokables.FunctionDefinitionNode;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsageNode;
import me.tomassetti.turin.typesystem.TypeUsage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

public abstract class AbstractCompiledTypeResolver<CE extends ClasspathElement> implements TypeResolver  {
    protected Map<String, CE> classpathElements = new HashMap<>();
    protected Map<String, CE> functionElements = new HashMap<>();
    protected Set<String> packages = new HashSet<>();

    @Override
    public boolean existPackage(String packageName) {
        return packages.contains(packageName);
    }

    protected class CompiledClassPath implements ClassPath {

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

    protected String classFileToClassName(File classFile, File root){
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
        if (className.startsWith(".")) {
            className = className.substring(1);
        }
        return className;
    }

    protected String classFileToFunctionName(File classFile, File root){
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
        functionName += classFile.getName().substring(FunctionDefinitionNode.CLASS_PREFIX.length());
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
                return Optional.of(new JavassistTypeDefinition(ctClass, new InFileSymbolResolver(this)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<FunctionDefinitionNode> resolveAbsoluteFunctionName(String typeName) {
        if (functionElements.containsKey(typeName)) {
            try {
                CtClass ctClass = functionElements.get(typeName).toCtClass();
                if (ctClass.getDeclaredMethods().length != 1) {
                    throw new UnsupportedOperationException();
                }
                CtMethod invokeMethod = ctClass.getDeclaredMethods()[0];
                if (!invokeMethod.getName().equals(FunctionDefinitionNode.INVOKE_METHOD_NAME)) {
                    throw new UnsupportedOperationException();
                }
                // necessary to get local var names
                MethodInfo methodInfo = invokeMethod.getMethodInfo();
                CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
                LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);

                TypeUsage returnType = JavassistTypeDefinitionFactory.toTypeUsage(invokeMethod.getReturnType(), new InFileSymbolResolver(this));
                List<FormalParameterNode> formalParameters = new ArrayList<>();

                int i=0;
                for (CtClass paramType : invokeMethod.getParameterTypes()) {
                    TypeUsage type =JavassistTypeDefinitionFactory.toTypeUsage(paramType, new InFileSymbolResolver(this));
                    String paramName = attr.variableName(i);
                    formalParameters.add(new FormalParameterNode(TypeUsageNode.wrap(type), paramName));
                    i++;
                }
                FunctionDefinitionNode functionDefinition = new LoadedFunctionDefinition(typeName, TypeUsageNode.wrap(returnType), formalParameters);
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
