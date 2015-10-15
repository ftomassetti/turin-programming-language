package me.tomassetti.turin.compiler;

import com.google.common.collect.ImmutableList;
import me.tomassetti.bytecode_generation.*;
import me.tomassetti.bytecode_generation.pushop.PushLocalVar;
import me.tomassetti.bytecode_generation.pushop.PushStaticField;
import me.tomassetti.bytecode_generation.returnop.ReturnValueBS;
import me.tomassetti.jvm.*;
import me.tomassetti.bytecode_generation.returnop.ReturnVoidBS;
import me.tomassetti.turin.classloading.ClassFileDefinition;
import me.tomassetti.turin.compiler.errorhandling.ErrorCollector;
import me.tomassetti.turin.parser.analysis.Property;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.*;
import me.tomassetti.turin.parser.ast.invokables.FunctionDefinition;
import me.tomassetti.turin.parser.ast.invokables.InvokableDefinition;
import me.tomassetti.turin.parser.ast.annotations.AnnotationUsage;
import me.tomassetti.turin.parser.ast.expressions.*;
import me.tomassetti.turin.parser.ast.expressions.literals.StringLiteral;
import me.tomassetti.turin.parser.ast.invokables.TurinTypeContructorDefinition;
import me.tomassetti.turin.parser.ast.invokables.TurinTypeMethodDefinition;
import me.tomassetti.turin.parser.ast.relations.RelationDefinition;
import me.tomassetti.turin.parser.ast.relations.RelationFieldDefinition;
import me.tomassetti.turin.parser.ast.typeusage.*;
import me.tomassetti.turin.typesystem.TypeUsage;
import org.objectweb.asm.*;
import turin.compilation.DefaultParam;
import turin.relations.ManyToManyRelation;
import turin.relations.OneToManyRelation;
import turin.relations.OneToOneRelation;
import turin.relations.Relation;

import java.util.*;
import java.util.stream.Collectors;

import static me.tomassetti.bytecode_generation.OpcodesUtils.*;

import static org.objectweb.asm.Opcodes.*;

/**
 * Wrap the status of the compilation process, like the class being currently written.
 */
public class Compilation {

    public static final int LOCALVAR_INDEX_FOR_THIS_IN_METHOD = 0;
    static final int LOCALVAR_INDEX_FOR_PARAM_0 = 1;
    static final String OBJECT_INTERNAL_NAME = JvmNameUtils.canonicalToInternal(Object.class.getCanonicalName());
    static final String OBJECT_DESCRIPTOR = JvmNameUtils.canonicalToDescriptor(Object.class.getCanonicalName());
    private static final int JAVA_8_CLASS_VERSION = 52;
    private final CompilationOfPush pushUtils = new CompilationOfPush(this);
    private final CompilationOfStatements compilationOfStatements = new CompilationOfStatements(this);
    private final static String METHOD_NAME_OF_FUNCTION = "invoke";

    private ClassWriter cw;
    private SymbolResolver resolver;
    private LocalVarsSymbolTable localVarsSymbolTable;
    private String internalClassName;
    private ErrorCollector errorCollector;

    public Compilation(SymbolResolver resolver, ErrorCollector errorCollector) {
        this.resolver = resolver;
        this.errorCollector = errorCollector;
    }

    public List<ClassFileDefinition> compile(TurinFile turinFile) {
        boolean valid = turinFile.validate(resolver, errorCollector);

        if (!valid) {
            return Collections.emptyList();
        }

        List<ClassFileDefinition> classFileDefinitions = new ArrayList<>();

        for (Node node : turinFile.getChildren()) {
            if (node instanceof TurinTypeDefinition) {
                classFileDefinitions.addAll(compile((TurinTypeDefinition)node));
            } else if (node instanceof Program) {
                classFileDefinitions.addAll(compile((Program) node));
            } else if (node instanceof FunctionDefinition) {
                classFileDefinitions.addAll(compile((FunctionDefinition) node, turinFile.getNamespaceDefinition()));
            } else if (node instanceof RelationDefinition) {
                classFileDefinitions.addAll(compile((RelationDefinition) node, turinFile.getNamespaceDefinition()));
            }
        }

        return classFileDefinitions;
    }

    private List<ClassFileDefinition> compile(RelationDefinition relationDefinition, NamespaceDefinition namespaceDefinition) {
        String canonicalClassName = namespaceDefinition.getName() + "." + RelationDefinition.CLASS_PREFIX + relationDefinition.getName();
        String internalClassName = JvmNameUtils.canonicalToInternal(canonicalClassName);

        // Note that COMPUTE_FRAMES implies COMPUTE_MAXS
        cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cw.visit(JAVA_8_CLASS_VERSION, ACC_PUBLIC + ACC_SUPER, internalClassName, null, OBJECT_INTERNAL_NAME, null);

        // Add the relation field, example:
        // public static final OneToManyRelation<Professor, Course> RELATION = new OneToManyRelation<Professor, Course>();
        String fieldDescriptor = relationDefinition.staticFieldDescriptor();
        String fieldSignature = null;
        String fieldTypeInternalName = null;
        switch (relationDefinition.getRelationType()) {
            case MANY_TO_MANY:
                fieldSignature = fieldDescriptor.substring(0, fieldDescriptor.length() - 1);
                fieldSignature += "<";
                fieldSignature += relationDefinition.firstField().getType().jvmType(resolver).getSignature();
                fieldSignature += relationDefinition.secondField().getType().jvmType(resolver).getSignature();
                fieldSignature += ">;";
                fieldTypeInternalName = JvmNameUtils.internalName(ManyToManyRelation.class);
                break;
            case ONE_TO_MANY:
                fieldSignature = fieldDescriptor.substring(0, fieldDescriptor.length() - 1);
                fieldSignature += "<";
                fieldSignature += relationDefinition.singleField().getType().jvmType(resolver).getSignature();
                fieldSignature += relationDefinition.manyField().getType().jvmType(resolver).getSignature();
                fieldSignature += ">;";
                fieldTypeInternalName = JvmNameUtils.internalName(OneToManyRelation.class);
                break;
            case ONE_TO_ONE:
                fieldSignature = fieldDescriptor.substring(0, fieldDescriptor.length() - 1);
                fieldSignature += "<";
                fieldSignature += relationDefinition.firstField().getType().jvmType(resolver).getSignature();
                fieldSignature += relationDefinition.secondField().getType().jvmType(resolver).getSignature();
                fieldTypeInternalName = JvmNameUtils.internalName(OneToOneRelation.class);
                fieldSignature += ">;";
                break;
            default:
                throw new UnsupportedOperationException();
        }
        final String fieldName = "RELATION";
        cw.visitField(ACC_PUBLIC + ACC_STATIC + ACC_FINAL, fieldName, fieldDescriptor, fieldSignature, null);

        // initialize the field in a static initializer
        MethodVisitor mv = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
        mv.visitTypeInsn(NEW, fieldTypeInternalName);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, fieldTypeInternalName, "<init>", "()V");
        mv.visitFieldInsn(PUTSTATIC, internalClassName, fieldName, fieldDescriptor);
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        // generate methods to access the endpoints
        // e.g.,    public static Relation.ReferenceSingleEndpoint<Node, Node> parentForChildrenElement(Node childrenElement){
        //              return RELATION.getReferenceForB(childrenElement);
        //          }
        //
        //          public static Relation.ReferenceMultipleEndpoint<Node, Node> childrenForParent(Node parent) {
        //              return RELATION.getReferenceForA(parent);
        //          }
        switch (relationDefinition.getRelationType()) {
            case MANY_TO_MANY:
                generateMethodForRelationMultipleEndpoint(cw, relationDefinition.firstField(), relationDefinition.secondField(),
                        relationDefinition.firstField().getType().copy(),
                        relationDefinition.secondField().getType().copy(),
                        true);
                generateMethodForRelationMultipleEndpoint(cw, relationDefinition.secondField(), relationDefinition.firstField(),
                        relationDefinition.firstField().getType().copy(),
                        relationDefinition.secondField().getType().copy(),
                        false);
                break;
            case ONE_TO_MANY:
                generateMethodForRelationSingleEndpoint(cw, relationDefinition.singleField(), relationDefinition.manyField(),
                        relationDefinition.singleField().getType().copy(),
                        relationDefinition.manyField().getType().copy(),
                        false);
                generateMethodForRelationMultipleEndpoint(cw, relationDefinition.manyField(), relationDefinition.singleField(),
                        relationDefinition.singleField().getType().copy(),
                        relationDefinition.manyField().getType().copy(),
                        true);
                break;
            case ONE_TO_ONE:
                generateMethodForRelationSingleEndpoint(cw, relationDefinition.firstField(), relationDefinition.secondField(),
                        relationDefinition.firstField().getType().copy(),
                        relationDefinition.secondField().getType().copy(),
                        true);
                generateMethodForRelationSingleEndpoint(cw, relationDefinition.secondField(), relationDefinition.firstField(),
                        relationDefinition.firstField().getType().copy(),
                        relationDefinition.secondField().getType().copy(),
                        false);
                break;
            default:
                throw new UnsupportedOperationException();
        }

        return ImmutableList.of(endClass(canonicalClassName));
    }

    // e.g.,    public static Relation.ReferenceMultipleEndpoint<Node, Node> childrenForParent(Node parent) {
    //              return RELATION.getReferenceForA(parent);
    //          }
    private void generateMethodForRelationMultipleEndpoint(ClassWriter cw, RelationFieldDefinition fieldAccessed, RelationFieldDefinition otherField,
                    TypeUsageNode firstParamType, TypeUsageNode secondParamType, boolean accessingFirstField) {
        String methodName = fieldAccessed.methodName();
        String descriptor = fieldAccessed.methodDescriptor(resolver);
        String signature = "(" + otherField.getType().jvmType(resolver).getSignature() + ")"
                + JvmNameUtils.descriptor(Relation.ReferenceMultipleEndpoint.class)
                + "<"
                + firstParamType.jvmType(resolver).getSignature()
                + secondParamType.jvmType(resolver).getSignature()
                + ">";
        // TODO record parameter name
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, methodName, descriptor, signature, null);
        String invokedMethodName = accessingFirstField ? "getReferenceForA" : "getReferenceForB";
        // the method should be invoked on the instance of the relation (MyRelationClass.INSTANCE)
        JvmFieldDefinition instanceField = new JvmFieldDefinition(
                JvmNameUtils.canonicalToInternal(fieldAccessed.getRelationDefinition().getGeneratedClassQualifiedName()),
                "RELATION",
                fieldAccessed.getRelationDefinition().staticFieldDescriptor(),
                true
        );
        new PushStaticField(instanceField).operate(mv);
        new PushLocalVar(OpcodesUtils.loadTypeFor(otherField.getType().jvmType(resolver)), 0).operate(mv);
        JvmMethodDefinition methodDefinition = new JvmMethodDefinition(
                fieldAccessed.getRelationDefinition().relationClassInternalName(),
                invokedMethodName,
                "(" + JvmNameUtils.descriptor(Object.class) + ")" + JvmNameUtils.descriptor(Relation.ReferenceMultipleEndpoint.class),
                false, false);
        new ReturnValueBS(Opcodes.ARETURN, new MethodInvocationBS(methodDefinition)).operate(mv);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    // e.g.,    public static Relation.ReferenceSingleEndpoint<Node, Node> parentForChildrenElement(Node childrenElement){
    //              return RELATION.getReferenceForB(childrenElement);
    //          }
    private void generateMethodForRelationSingleEndpoint(ClassWriter cw, RelationFieldDefinition fieldAccessed, RelationFieldDefinition otherField,
                                                         TypeUsageNode firstParamType, TypeUsageNode secondParamType, boolean accessingFirstField) {
        String methodName = fieldAccessed.methodName();
        String descriptor = fieldAccessed.methodDescriptor(resolver);
        String signature = "(" + otherField.getType().jvmType(resolver).getSignature() + ")"
                + JvmNameUtils.descriptor(Relation.ReferenceSingleEndpoint.class)
                + "<"
                + firstParamType.jvmType(resolver).getSignature()
                + secondParamType.jvmType(resolver).getSignature()
                + ">";
        // TODO record parameter name
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, methodName, descriptor, signature, null);
        String invokedMethodName = accessingFirstField ? "getReferenceForA" : "getReferenceForB";
        // the method should be invoked on the instance of the relation (MyRelationClass.INSTANCE)
        JvmFieldDefinition instanceField = new JvmFieldDefinition(
                JvmNameUtils.canonicalToInternal(fieldAccessed.getRelationDefinition().getGeneratedClassQualifiedName()),
                "RELATION",
                fieldAccessed.getRelationDefinition().staticFieldDescriptor(),
                true
        );
        new PushStaticField(instanceField).operate(mv);
        new PushLocalVar(OpcodesUtils.loadTypeFor(otherField.getType().jvmType(resolver)), 0).operate(mv);
        JvmMethodDefinition methodDefinition = new JvmMethodDefinition(
                fieldAccessed.getRelationDefinition().relationClassInternalName(),
                invokedMethodName,
                "(" + JvmNameUtils.descriptor(Object.class) + ")" + JvmNameUtils.descriptor(Relation.ReferenceSingleEndpoint.class),
                false, false);
        new ReturnValueBS(Opcodes.ARETURN, new MethodInvocationBS(methodDefinition)).operate(mv);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private List<ClassFileDefinition> compile(FunctionDefinition functionDefinition, NamespaceDefinition namespaceDefinition) {
        String canonicalClassName = namespaceDefinition.getName() + "." + FunctionDefinition.CLASS_PREFIX + functionDefinition.getName();
        String internalClassName = JvmNameUtils.canonicalToInternal(canonicalClassName);

        // Note that COMPUTE_FRAMES implies COMPUTE_MAXS
        cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cw.visit(JAVA_8_CLASS_VERSION, ACC_PUBLIC + ACC_SUPER, internalClassName, null, OBJECT_INTERNAL_NAME, null);

        for (AnnotationUsage annotation : functionDefinition.getAnnotations()) {
            cw.visitAnnotation(annotation.getDescriptor(resolver), true);
        }

        generateInvokable(functionDefinition, METHOD_NAME_OF_FUNCTION, true);

        return ImmutableList.of(endClass(canonicalClassName));
    }

    private void generateField(Property property) {
        JvmType jvmType = property.getTypeUsage().jvmType(resolver);
        FieldVisitor fv = cw.visitField(ACC_PRIVATE, property.getName(), jvmType.getDescriptor(), jvmType.getSignature(), null);
        fv.visitEnd();
    }

    private void generateGetter(Property property, String classInternalName) {
        String getterName = property.getterName();
        JvmType jvmType = property.getTypeUsage().jvmType(resolver);
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, getterName, "()" + jvmType.getDescriptor(), "()" + jvmType.getSignature(), null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, LOCALVAR_INDEX_FOR_THIS_IN_METHOD);
        mv.visitFieldInsn(GETFIELD, classInternalName, property.fieldName(), jvmType.getDescriptor());
        mv.visitInsn(returnTypeFor(jvmType));
        // calculated for us
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private ClassFileDefinition endClass(String canonicalName) {
        cw.visitEnd();

        byte[] programBytecode = cw.toByteArray();
        cw = null;
        return new ClassFileDefinition(canonicalName, programBytecode);
    }

    private void generateToStringMethod(TurinTypeDefinition typeDefinition) {
        localVarsSymbolTable = LocalVarsSymbolTable.forInstanceMethod();
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "toString", "()Ljava/lang/String;", "()Ljava/lang/String;", null);
        mv.visitCode();

        if (typeDefinition.getAllProperties(resolver).isEmpty()) {
            pushUtils.pushExpression(new StringLiteral(typeDefinition.getName())).operate(mv);
        } else {
            List<BytecodeSequence> elements = new ArrayList<>();
            elements.add(new NewInvocationBS(new JvmConstructorDefinition("java/lang/StringBuilder", "()V"), NoOp.getInstance()));
            appendToStringBuilder(new StringLiteral(typeDefinition.getName()+"{"), elements);

            int remaining = typeDefinition.getAllProperties(resolver).size();
            for (Property property : typeDefinition.getAllProperties(resolver)) {
                appendToStringBuilder(new StringLiteral(property.getName() + "="), elements);
                ValueReference valueReference = new ValueReference(property.getName());
                // in this way the field can be solved
                valueReference.setParent(typeDefinition);
                appendToStringBuilder(valueReference, elements);
                remaining--;
                if (remaining > 0) {
                    appendToStringBuilder(new StringLiteral(", "), elements);
                }
            }

            appendToStringBuilder(new StringLiteral("}"), elements);

            elements.add(new MethodInvocationBS(new JvmMethodDefinition("java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false, false)));
            new ComposedBytecodeSequence(elements).operate(mv);
        }
        mv.visitInsn(ARETURN);

        // calculated for us
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private List<ClassFileDefinition> compile(TurinTypeDefinition typeDefinition) {
        this.internalClassName = JvmNameUtils.canonicalToInternal(typeDefinition.getQualifiedName());

        // Note that COMPUTE_FRAMES implies COMPUTE_MAXS
        cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        // TODO consider generic signature
        // TODO consider visibility
        // calculate superclass
        String superClassInternalName = OBJECT_INTERNAL_NAME;
        if (typeDefinition.getBaseType().isPresent()) {
            superClassInternalName = JvmNameUtils.canonicalToInternal(typeDefinition.getBaseType().get().asReferenceTypeUsage().getQualifiedName(resolver));
        }
        // calculate interfaces
        String[] interfaces = typeDefinition.getInterfaces().stream()
                .map((i)->JvmNameUtils.canonicalToInternal(i.asReferenceTypeUsage().getQualifiedName(resolver)))
                .collect(Collectors.toList()).toArray(new String[]{});
        cw.visit(JAVA_8_CLASS_VERSION, ACC_PUBLIC + ACC_SUPER, internalClassName, null, superClassInternalName, interfaces);

        for (AnnotationUsage annotation : typeDefinition.getAnnotations()) {
            cw.visitAnnotation(annotation.getDescriptor(resolver), true);
        }

        // TODO consider if the property is readable and writable
        for (Property property : typeDefinition.getDirectProperties(resolver)){
            generateField(property);
            generateGetter(property, internalClassName);
            new CompilationOfGeneratedMethods(this, cw).generateSetter(property, internalClassName);
        }

        if (!typeDefinition.defineExplicitConstructor(resolver)) {
            new CompilationOfGeneratedMethods(this, cw).generateConstructor(typeDefinition, internalClassName);
        }
        if (!typeDefinition.defineMethodEquals(resolver)) {
            new CompilationOfGeneratedMethods(this, cw).generateEqualsMethod(typeDefinition, internalClassName);
        }
        if (!typeDefinition.defineMethodHashCode(resolver)) {
            new CompilationOfGeneratedMethods(this, cw).generateHashCodeMethod(typeDefinition, internalClassName);
        }
        if (!typeDefinition.defineMethodToString(resolver)) {
            generateToStringMethod(typeDefinition);
        }

        typeDefinition.getDirectMethods().forEach((m)-> generateTurinTypeMethod(m));

        typeDefinition.getExplicitConstructors().forEach((c)-> generateTurinTypeConstructor(c));

        return ImmutableList.of(endClass(typeDefinition.getQualifiedName()));
    }

    private void generateTurinTypeMethod(TurinTypeMethodDefinition methodDefinition) {
        generateInvokable(methodDefinition, methodDefinition.getName(), false);
    }

    private void generateTurinTypeConstructor(TurinTypeContructorDefinition methodDefinition) {
        generateInvokable(methodDefinition, "<init>", false);
    }

    void addDefaultParamAnnotations(MethodVisitor mv, List<FormalParameter> formalParameters) {
        int defaultParamIndex = 0;
        for (FormalParameter defaultParam : formalParameters.stream()
                .filter((p)->p.hasDefaultValue())
                .collect(Collectors.toList())) {
            AnnotationVisitor annotationVisitor = mv.visitAnnotation(JvmNameUtils.canonicalToDescriptor(DefaultParam.class.getCanonicalName()), true);
            annotationVisitor.visit("name", defaultParam.getName());
            annotationVisitor.visit("typeSignature", defaultParam.getType().jvmType(resolver).getSignature());
            annotationVisitor.visit("index", defaultParamIndex);
            annotationVisitor.visitEnd();
            defaultParamIndex++;
        }
    }

    private void generateInvokable(InvokableDefinition invokableDefinition, String invokableName, boolean isStatic) {
        if (isStatic) {
            localVarsSymbolTable = LocalVarsSymbolTable.forStaticMethod();
        } else {
            localVarsSymbolTable = LocalVarsSymbolTable.forInstanceMethod();
        }

        String paramsDescriptor = String.join("", invokableDefinition.getParameters().stream().map((dp) -> dp.getType().jvmType(resolver).getDescriptor()).collect(Collectors.toList()));
        String paramsSignature = String.join("", invokableDefinition.getParameters().stream().map((dp) -> dp.getType().jvmType(resolver).getSignature()).collect(Collectors.toList()));
        String methodDescriptor = "(" + paramsDescriptor + ")" + invokableDefinition.getReturnType().jvmType(resolver).getDescriptor();
        String methodSignature = "(" + paramsSignature + ")" + invokableDefinition.getReturnType().jvmType(resolver).getSignature();
        // TODO consider exceptions
        int modifiers = ACC_PUBLIC;
        if (isStatic) {
            modifiers = modifiers | ACC_STATIC;
        }
        MethodVisitor mv = cw.visitMethod(modifiers, invokableName, methodDescriptor, methodSignature, null);

        addDefaultParamAnnotations(mv, invokableDefinition.getParameters());

        mv.visitCode();

        // Add local variables: they are necessary for supporting named parameters and useful for debugging
        Label start = new Label();
        Label end = new Label();
        mv.visitLabel(start);
        for (FormalParameter formalParameter : invokableDefinition.getParameters()) {
            int index = localVarsSymbolTable.add(formalParameter.getName(), formalParameter);
            mv.visitLocalVariable(formalParameter.getName(),
                    formalParameter.getType().jvmType(resolver).getDescriptor(),
                    formalParameter.getType().jvmType(resolver).getSignature(),
                    start,
                    end,
                    index);
        }

        compilationOfStatements.compile(invokableDefinition.getBody()).operate(mv);

        // add implicit return when needed
        if (invokableDefinition.getReturnType() instanceof VoidTypeUsageNode) {
           // TODO do not add if there is already a return at the end
            new ReturnVoidBS().operate(mv);
        }

        mv.visitLabel(end);
        // calculated for us
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        localVarsSymbolTable = null;
    }

    private List<ClassFileDefinition> compile(Program program) {
        localVarsSymbolTable = LocalVarsSymbolTable.forStaticMethod();
        String canonicalClassName = program.getQualifiedName();
        String internalClassName = JvmNameUtils.canonicalToInternal(canonicalClassName);

        // Note that COMPUTE_FRAMES implies COMPUTE_MAXS
        cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cw.visit(JAVA_8_CLASS_VERSION, ACC_PUBLIC + ACC_SUPER, internalClassName, null, OBJECT_INTERNAL_NAME, null);

        // TODO consider exceptions
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);

        localVarsSymbolTable.add(program.getFormalParameter());

        mv.visitCode();

        compilationOfStatements.compile(program.getStatement()).operate(mv);

        // Implicit return
        // TODO remove if already present or not needed (for example if there is an exception)
        mv.visitInsn(RETURN);

        // calculated for us
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        localVarsSymbolTable = null;

        return ImmutableList.of(endClass(canonicalClassName));
    }

    void appendToStringBuilder(Expression piece, List<BytecodeSequence> elements) {
        TypeUsage pieceType = piece.calcType(resolver);
        if (pieceType.equals(ReferenceTypeUsage.STRING)) {
            elements.add(pushUtils.pushExpression(piece));
            elements.add(new MethodInvocationBS(new JvmMethodDefinition("java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false, false)));
        } else if (pieceType.isReference()) {
            elements.add(pushUtils.pushExpression(piece));
            elements.add(new MethodInvocationBS(new JvmMethodDefinition("java/lang/StringBuilder", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;", false, false)));
        } else if (pieceType.isPrimitive() && pieceType.asPrimitiveTypeUsage().isStoredInInt()) {
            elements.add(pushUtils.convertAndPush(piece, JvmType.INT));
            elements.add(new MethodInvocationBS(new JvmMethodDefinition("java/lang/StringBuilder", "append", "(I)Ljava/lang/StringBuilder;", false, false)));
        } else if (pieceType.equals(PrimitiveTypeUsageNode.BOOLEAN)) {
            elements.add(pushUtils.pushExpression(piece));
            elements.add(new MethodInvocationBS(new JvmMethodDefinition("java/lang/StringBuilder", "append", "(Z)Ljava/lang/StringBuilder;", false, false)));
        } else if (pieceType.equals(PrimitiveTypeUsageNode.CHAR)) {
            elements.add(pushUtils.pushExpression(piece));
            elements.add(new MethodInvocationBS(new JvmMethodDefinition("java/lang/StringBuilder", "append", "(C)Ljava/lang/StringBuilder;", false, false)));
        } else if (pieceType.isPrimitive() && pieceType.asPrimitiveTypeUsage().isLong()) {
            elements.add(pushUtils.pushExpression(piece));
            elements.add(new MethodInvocationBS(new JvmMethodDefinition("java/lang/StringBuilder", "append", "(J)Ljava/lang/StringBuilder;", false, false)));
        } else if (pieceType.isPrimitive() && pieceType.asPrimitiveTypeUsage().isFloat()) {
            elements.add(pushUtils.pushExpression(piece));
            elements.add(new MethodInvocationBS(new JvmMethodDefinition("java/lang/StringBuilder", "append", "(F)Ljava/lang/StringBuilder;", false, false)));
        } else if (pieceType.isPrimitive() && pieceType.asPrimitiveTypeUsage().isDouble()) {
            elements.add(pushUtils.pushExpression(piece));
            elements.add(new MethodInvocationBS(new JvmMethodDefinition("java/lang/StringBuilder", "append", "(D)Ljava/lang/StringBuilder;", false, false)));
        } else {
            throw new UnsupportedOperationException(pieceType.toString());
        }
    }

    SymbolResolver getResolver() {
        return resolver;
    }

    String getInternalClassName() {
        return internalClassName;
    }

    LocalVarsSymbolTable getLocalVarsSymbolTable() {
        return localVarsSymbolTable;
    }

    public void setLocalVarsSymbolTable(LocalVarsSymbolTable localVarsSymbolTable) {
        this.localVarsSymbolTable = localVarsSymbolTable;
    }

    public CompilationOfPush getPushUtils() {
        return pushUtils;
    }
}
