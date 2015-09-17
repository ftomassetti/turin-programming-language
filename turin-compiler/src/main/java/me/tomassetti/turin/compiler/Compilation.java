package me.tomassetti.turin.compiler;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.compiler.bytecode.*;
import me.tomassetti.turin.compiler.bytecode.logicalop.LogicalAndBS;
import me.tomassetti.turin.compiler.bytecode.logicalop.LogicalNotBS;
import me.tomassetti.turin.compiler.bytecode.logicalop.LogicalOrBS;
import me.tomassetti.turin.compiler.bytecode.pushop.*;
import me.tomassetti.turin.compiler.bytecode.returnop.ReturnFalseBS;
import me.tomassetti.turin.compiler.bytecode.returnop.ReturnTrueBS;
import me.tomassetti.turin.compiler.bytecode.returnop.ReturnValueBS;
import me.tomassetti.turin.compiler.bytecode.returnop.ReturnVoidBS;
import me.tomassetti.turin.compiler.errorhandling.ErrorCollector;
import me.tomassetti.turin.implicit.BasicTypeUsage;
import me.tomassetti.turin.jvm.*;
import me.tomassetti.turin.parser.analysis.Property;
import me.tomassetti.turin.parser.analysis.resolvers.Resolver;
import me.tomassetti.turin.parser.ast.*;
import me.tomassetti.turin.parser.ast.InvokableDefinition;
import me.tomassetti.turin.parser.ast.expressions.*;
import me.tomassetti.turin.parser.ast.expressions.literals.BooleanLiteral;
import me.tomassetti.turin.parser.ast.expressions.literals.IntLiteral;
import me.tomassetti.turin.parser.ast.expressions.literals.StringLiteral;
import me.tomassetti.turin.parser.analysis.resolvers.jdk.ReflectionBaseField;
import me.tomassetti.turin.parser.analysis.resolvers.jdk.ReflectionBasedSetOfOverloadedMethods;
import me.tomassetti.turin.parser.ast.statements.*;
import me.tomassetti.turin.parser.ast.typeusage.ArrayTypeUsage;
import me.tomassetti.turin.parser.ast.typeusage.PrimitiveTypeUsage;
import me.tomassetti.turin.parser.ast.typeusage.ReferenceTypeUsage;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;
import org.objectweb.asm.*;

import java.util.*;
import java.util.stream.Collectors;

import static me.tomassetti.turin.compiler.OpcodesUtils.*;

import static org.objectweb.asm.Opcodes.*;

/**
 * Wrap the status of the compilation process, like the class being currently written.
 */
public class Compilation {

    private static final int JAVA_8_CLASS_VERSION = 52;
    public static final int LOCALVAR_INDEX_FOR_THIS_IN_METHOD = 0;
    private static final int LOCALVAR_INDEX_FOR_PARAM_0 = 1;

    private static final String OBJECT_INTERNAL_NAME = JvmNameUtils.canonicalToInternal(Object.class.getCanonicalName());
    private static final String OBJECT_DESCRIPTOR = "L" + OBJECT_INTERNAL_NAME + ";";

    private ClassWriter cw;
    private Resolver resolver;
    private LocalVarsSymbolTable localVarsSymbolTable;
    private String internalClassName;
    private ErrorCollector errorCollector;

    public Compilation(Resolver resolver, ErrorCollector errorCollector) {
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
            }
        }

        return classFileDefinitions;
    }

    private List<ClassFileDefinition> compile(FunctionDefinition functionDefinition, NamespaceDefinition namespaceDefinition) {
        String canonicalClassName = namespaceDefinition.getName() + ".Function_"+functionDefinition.getName();
        String internalClassName = JvmNameUtils.canonicalToInternal(canonicalClassName);

        // Note that COMPUTE_FRAMES implies COMPUTE_MAXS
        cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cw.visit(JAVA_8_CLASS_VERSION, ACC_PUBLIC + ACC_SUPER, internalClassName, null, OBJECT_INTERNAL_NAME, null);

        generateInvokable(functionDefinition, "invoke", true);

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

    private void enforceConstraint(Property property, MethodVisitor mv, JvmType jvmType, int varIndex) {
        if (property.getTypeUsage().equals(BasicTypeUsage.UINT)) {
            // index 0 is the "this"
            mv.visitVarInsn(loadTypeFor(jvmType), varIndex + 1);
            Label label = new Label();

            // if the value is >= 0 we jump and skip the throw exception
            mv.visitJumpInsn(IFGE, label);
            JvmConstructorDefinition constructor = new JvmConstructorDefinition("java/lang/IllegalArgumentException", "(Ljava/lang/String;)V");
            BytecodeSequence instantiateException = new NewInvocationBS(constructor, ImmutableList.of(new PushStringConst(property.getName() + " should be positive")));
            new ThrowBS(instantiateException).operate(mv);

            mv.visitLabel(label);
        } else if (property.getTypeUsage().isReferenceTypeUsage() && property.getTypeUsage().asReferenceTypeUsage().getQualifiedName(resolver).equals(String.class.getCanonicalName())) {
            // index 0 is the "this"
            mv.visitVarInsn(loadTypeFor(jvmType), varIndex + 1);
            Label label = new Label();

            // if not null skip the throw
            mv.visitJumpInsn(IFNONNULL, label);
            JvmConstructorDefinition constructor = new JvmConstructorDefinition("java/lang/IllegalArgumentException", "(Ljava/lang/String;)V");
            BytecodeSequence instantiateException = new NewInvocationBS(constructor, ImmutableList.of(new PushStringConst(property.getName() + " cannot be null")));
            new ThrowBS(instantiateException).operate(mv);

            mv.visitLabel(label);
        }
    }

    private void generateSetter(Property property, String internalClassName) {
        String setterName = property.setterName();
        JvmType jvmType = property.getTypeUsage().jvmType(resolver);
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, setterName, "(" + jvmType.getDescriptor() + ")V", "(" + jvmType.getSignature() + ")V", null);
        mv.visitCode();

        enforceConstraint(property, mv, jvmType, 0);

        // Assignment
        PushThis.getInstance().operate(mv);
        mv.visitVarInsn(loadTypeFor(jvmType), 1);
        mv.visitFieldInsn(PUTFIELD, internalClassName, property.getName(), jvmType.getDescriptor());
        mv.visitInsn(RETURN);
        // calculated for us
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void generateConstructor(TurinTypeDefinition typeDefinition, String className) {
        // TODO consider also inherited properties
        List<Property> directProperties = typeDefinition.getDirectProperties(resolver);
        String paramsDescriptor = String.join("", directProperties.stream().map((dp) -> dp.getTypeUsage().jvmType(resolver).getDescriptor()).collect(Collectors.toList()));
        String paramsSignature = String.join("", directProperties.stream().map((dp) -> dp.getTypeUsage().jvmType(resolver).getSignature()).collect(Collectors.toList()));
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(" + paramsDescriptor + ")V", "(" + paramsSignature + ")V", null);
        mv.visitCode();

        PushThis.getInstance().operate(mv);
        mv.visitMethodInsn(INVOKESPECIAL, OBJECT_INTERNAL_NAME, "<init>", "()V", false);

        int propIndex = 0;
        for (Property property : directProperties) {
            enforceConstraint(property, mv, property.getTypeUsage().jvmType(resolver), propIndex);
            propIndex++;
        }

        propIndex = 0;
        for (Property property : directProperties) {
            JvmType jvmType = property.getTypeUsage().jvmType(resolver);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(loadTypeFor(jvmType), propIndex + 1);
            mv.visitFieldInsn(PUTFIELD, className, property.getName(), jvmType.getDescriptor());
            propIndex++;
        }

        mv.visitInsn(RETURN);
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

    private void generateEqualsMethod(TurinTypeDefinition typeDefinition, String internalClassName) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "equals", "(" + OBJECT_DESCRIPTOR + ")Z", "(" + OBJECT_DESCRIPTOR + ")Z", null);
        mv.visitCode();

        // if (this == o) return true;
        mv.visitVarInsn(ALOAD, LOCALVAR_INDEX_FOR_THIS_IN_METHOD);
        mv.visitVarInsn(ALOAD, LOCALVAR_INDEX_FOR_PARAM_0);
        Label paramAndThisAreNotTheSame = new Label();
        mv.visitJumpInsn(IF_ACMPNE, paramAndThisAreNotTheSame);
        new ReturnTrueBS().operate(mv);
        mv.visitLabel(paramAndThisAreNotTheSame);

        // if (o == null || getClass() != o.getClass()) return false;
        mv.visitVarInsn(ALOAD, LOCALVAR_INDEX_FOR_PARAM_0);
        Label paramIsNull = new Label();
        mv.visitJumpInsn(IFNULL, paramIsNull);
        mv.visitVarInsn(ALOAD, LOCALVAR_INDEX_FOR_THIS_IN_METHOD);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
        mv.visitVarInsn(ALOAD, LOCALVAR_INDEX_FOR_PARAM_0);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
        Label paramHasSameClassAsThis = new Label();
        mv.visitJumpInsn(IF_ACMPEQ, paramHasSameClassAsThis);
        mv.visitLabel(paramIsNull);
        new ReturnFalseBS().operate(mv);
        mv.visitLabel(paramHasSameClassAsThis);

        // MyType other = (MyType) o;
        mv.visitVarInsn(ALOAD, LOCALVAR_INDEX_FOR_PARAM_0);
        mv.visitTypeInsn(CHECKCAST, internalClassName);
        final int localvar_index_for_other = 2;
        mv.visitVarInsn(ASTORE, localvar_index_for_other);

        // if (!this.aField.equals(other.aField)) return false;
        for (Property property : typeDefinition.getAllProperties(resolver)) {
            TypeUsage propertyTypeUsage = property.getTypeUsage();
            String fieldTypeDescriptor = propertyTypeUsage.jvmType(resolver).getDescriptor();

            mv.visitVarInsn(ALOAD, LOCALVAR_INDEX_FOR_THIS_IN_METHOD);
            mv.visitFieldInsn(GETFIELD, internalClassName, property.getName(), fieldTypeDescriptor);
            mv.visitVarInsn(ALOAD, localvar_index_for_other);
            mv.visitFieldInsn(GETFIELD, internalClassName, property.getName(), fieldTypeDescriptor);
            Label propertyIsEqual = new Label();

            if (propertyTypeUsage.isPrimitive()) {
                if (propertyTypeUsage.asPrimitiveTypeUsage().isLong()) {
                    mv.visitInsn(LCMP);
                    mv.visitJumpInsn(IFEQ, propertyIsEqual);
                } else if (propertyTypeUsage.asPrimitiveTypeUsage().isFloat()) {
                    mv.visitInsn(FCMPL);
                    mv.visitJumpInsn(IFEQ, propertyIsEqual);
                } else if (propertyTypeUsage.asPrimitiveTypeUsage().isDouble()) {
                    mv.visitInsn(DCMPL);
                    mv.visitJumpInsn(IFEQ, propertyIsEqual);
                } else {
                    mv.visitJumpInsn(IF_ICMPEQ, propertyIsEqual);
                }
            } else {
                boolean isInterface = propertyTypeUsage.asReferenceTypeUsage().isInterface(resolver);
                if (isInterface) {
                    mv.visitMethodInsn(INVOKEINTERFACE, propertyTypeUsage.jvmType(resolver).getInternalName(), "equals", "(Ljava/lang/Object;)Z", true);
                } else {
                    mv.visitMethodInsn(INVOKEVIRTUAL, propertyTypeUsage.jvmType(resolver).getInternalName(), "equals", "(Ljava/lang/Object;)Z", false);
                }
                mv.visitJumpInsn(IFNE, propertyIsEqual);
            }

            new ReturnFalseBS().operate(mv);
            mv.visitLabel(propertyIsEqual);
        }

        new ReturnTrueBS().operate(mv);

        // calculated for us
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void generateHashCodeMethod(TurinTypeDefinition typeDefinition, String internalClassName) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "hashCode", "()I", "()I", null);
        mv.visitCode();

        final int localvar_index_of_result = 1;

        // int result = 1;
        mv.visitInsn(ICONST_1);
        mv.visitVarInsn(ISTORE, localvar_index_of_result);

        for (Property property : typeDefinition.getAllProperties(resolver)) {
            // result = 31 * result + this.aField.hashCode();
            TypeUsage propertyTypeUsage = property.getTypeUsage();
            String fieldTypeDescriptor = propertyTypeUsage.jvmType(resolver).getDescriptor();

            // 31 is just a prime number by which we multiply the current value of result
            mv.visitIntInsn(BIPUSH, 31);
            mv.visitVarInsn(ILOAD, localvar_index_of_result);
            mv.visitInsn(IMUL);

            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, internalClassName, property.getName(), fieldTypeDescriptor);

            if (propertyTypeUsage.isPrimitive()) {
                if (propertyTypeUsage.asPrimitiveTypeUsage().isLong()) {
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "hashCode", "(J)I", false);
                } else if (propertyTypeUsage.asPrimitiveTypeUsage().isFloat()) {
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "hashCode", "(F)I", false);
                } else if (propertyTypeUsage.asPrimitiveTypeUsage().isDouble()) {
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "hashCode", "(D)I", false);
                } else {
                    // nothing to do, the value is already on the stack and we can sum it directly
                }
            } else {
                boolean isInterface = propertyTypeUsage.asReferenceTypeUsage().isInterface(resolver);
                if (isInterface) {
                    mv.visitMethodInsn(INVOKEINTERFACE, propertyTypeUsage.jvmType(resolver).getInternalName(), "hashCode", "()I", true);
                } else {
                    mv.visitMethodInsn(INVOKEVIRTUAL, propertyTypeUsage.jvmType(resolver).getInternalName(), "hashCode", "()I", false);
                }
            }

            mv.visitInsn(IADD);
            mv.visitVarInsn(ISTORE, localvar_index_of_result);
        }

        // return result;
        mv.visitVarInsn(ILOAD, localvar_index_of_result);
        mv.visitInsn(IRETURN);

        // calculated for us
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void generateToStringMethod(TurinTypeDefinition typeDefinition) {
        localVarsSymbolTable = LocalVarsSymbolTable.forInstanceMethod();
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "toString", "()Ljava/lang/String;", "()Ljava/lang/String;", null);
        mv.visitCode();

        if (typeDefinition.getAllProperties(resolver).isEmpty()) {
            pushExpression(new StringLiteral(typeDefinition.getName())).operate(mv);
        } else {
            List<BytecodeSequence> elements = new ArrayList<>();
            elements.add(new NewInvocationBS(new JvmConstructorDefinition("java/lang/StringBuilder", "()V"), Collections.emptyList()));
            appendToStringBuilder(new StringLiteral(typeDefinition.getName()+"{"), elements);

            int remaining = typeDefinition.getAllProperties(resolver).size();
            for (Property property : typeDefinition.getAllProperties(resolver)) {
                appendToStringBuilder(new StringLiteral(property.getName()+"="), elements);
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

            elements.add(new MethodInvocationBS(new JvmMethodDefinition("java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false)));
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
        // TODO consider generic signature, superclass and interfaces
        cw.visit(JAVA_8_CLASS_VERSION, ACC_PUBLIC + ACC_SUPER, internalClassName, null, OBJECT_INTERNAL_NAME, null);

        for (Property property : typeDefinition.getDirectProperties(resolver)){
            generateField(property);
            generateGetter(property, internalClassName);
            generateSetter(property, internalClassName);
        }

        generateConstructor(typeDefinition, internalClassName);
        if (!typeDefinition.defineMethodEquals(resolver)) {
            generateEqualsMethod(typeDefinition, internalClassName);
        }
        if (!typeDefinition.defineMethodHashCode(resolver)) {
            generateHashCodeMethod(typeDefinition, internalClassName);
        }
        if (!typeDefinition.defineMethodToString(resolver)) {
            generateToStringMethod(typeDefinition);
        }

        typeDefinition.getDirectMethods().forEach((m)->generateMethod(m));

        return ImmutableList.of(endClass(typeDefinition.getQualifiedName()));
    }

    private void generateMethod(MethodDefinition methodDefinition) {
        generateInvokable(methodDefinition, methodDefinition.getName(), false);
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

        mv.visitCode();

        for (FormalParameter formalParameter : invokableDefinition.getParameters()) {
            localVarsSymbolTable.add(formalParameter.getName(), formalParameter);
        }

        compile(invokableDefinition.getBody()).operate(mv);

        // TODO add implicit return when needed

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

        localVarsSymbolTable.add("args", new FormalParameter(new ArrayTypeUsage(ReferenceTypeUsage.STRING), "args"));

        mv.visitCode();

        compile(program.getStatement()).operate(mv);

        // Implicit return
        mv.visitInsn(RETURN);

        // calculated for us
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        localVarsSymbolTable = null;

        return ImmutableList.of(endClass(canonicalClassName));
    }

    private BytecodeSequence compile(Statement statement) {
        if (statement instanceof VariableDeclaration) {
            VariableDeclaration variableDeclaration = (VariableDeclaration) statement;
            int pos = localVarsSymbolTable.add(variableDeclaration.getName(), variableDeclaration);
            return new ComposedBytecodeSequence(ImmutableList.of(compile(variableDeclaration.getValue()), new LocalVarAssignmentBS(pos, JvmTypeCategory.from(variableDeclaration.varType(resolver), resolver))));
        } else if (statement instanceof ExpressionStatement) {
            return executeEpression(((ExpressionStatement) statement).getExpression());
        } else if (statement instanceof BlockStatement){
            BlockStatement blockStatement = (BlockStatement)statement;
            List<BytecodeSequence> elements = blockStatement.getStatements().stream().map((s)->compile(s)).collect(Collectors.toList());
            return new ComposedBytecodeSequence(elements);
        } else if (statement instanceof ReturnStatement) {
            ReturnStatement returnStatement = (ReturnStatement) statement;
            if (returnStatement.hasValue()) {
                int returnType = returnStatement.getValue().calcType(resolver).jvmType(resolver).returnOpcode();
                return new ReturnValueBS(returnType, pushExpression(returnStatement.getValue()));
            } else {
                return new ReturnVoidBS();
            }
        } else if (statement instanceof IfStatement) {
            IfStatement ifStatement = (IfStatement) statement;
            BytecodeSequence ifCondition = pushExpression(ifStatement.getCondition());
            BytecodeSequence ifBody = compile(ifStatement.getIfBody());
            List<BytecodeSequence> elifConditions = ifStatement.getElifStatements().stream().map((ec) -> pushExpression(ec.getCondition())).collect(Collectors.toList());
            List<BytecodeSequence> elifBodys = ifStatement.getElifStatements().stream().map((ec) -> compile(ec.getBody())).collect(Collectors.toList());
            if (ifStatement.hasElse()) {
                return new IfBS(ifCondition, ifBody, elifConditions, elifBodys, compile(ifStatement.getElseBody()));
            } else {
                return new IfBS(ifCondition, ifBody, elifConditions, elifBodys);
            }
        } else if (statement instanceof ThrowStatement) {
            ThrowStatement throwStatement = (ThrowStatement) statement;
            return new ThrowBS(pushExpression(throwStatement.getException()));
        } else if (statement instanceof TryCatchStatement) {
            TryCatchStatement tryCatchStatement = (TryCatchStatement) statement;
            return compile(tryCatchStatement);
        } else {
            throw new UnsupportedOperationException(statement.toString());
        }
    }

    private BytecodeSequence compile(TryCatchStatement tryCatchStatement) {
        return new BytecodeSequence() {
            @Override
            public void operate(MethodVisitor mv) {
                Label tryStart = new Label();
                Label tryEnd = new Label();
                Label afterTryCatch = new Label();
                List<Label> catchSpecificLabels = new ArrayList<>();

                for (CatchClause catchClause : tryCatchStatement.getCatchClauses()) {
                    Label catchSpecificLabel = new Label();
                    mv.visitTryCatchBlock(tryStart, tryEnd, catchSpecificLabel, JvmNameUtils.canonicalToInternal(catchClause.getExceptionType().resolve(resolver).getQualifiedName()));
                    catchSpecificLabels.add(catchSpecificLabel);
                }

                mv.visitLabel(tryStart);
                compile(tryCatchStatement.getBody()).operate(mv);
                mv.visitLabel(tryEnd);
                mv.visitJumpInsn(GOTO, afterTryCatch);

                int i=0;
                for (CatchClause catchClause : tryCatchStatement.getCatchClauses()) {
                    Label catchSpecificLabel = catchSpecificLabels.get(i);
                    mv.visitLabel(catchSpecificLabel);
                    compile(catchClause.getBody()).operate(mv);
                    mv.visitJumpInsn(GOTO, afterTryCatch);
                    i++;
                }

                mv.visitLabel(afterTryCatch);
            }
        };
    }

    private BytecodeSequence compile(Expression expression) {
        if (expression instanceof IntLiteral) {
            throw new UnsupportedOperationException();
        } else if (expression instanceof StringLiteral) {
            throw new UnsupportedOperationException();
        } else if (expression instanceof FunctionCall) {
            throw new UnsupportedOperationException();
        } else if (expression instanceof Creation) {
            Creation creation = (Creation)expression;
            List<BytecodeSequence> argumentsPush = creation.getActualParamValuesInOrder().stream()
                    .map((ap) -> pushExpression(ap))
                    .collect(Collectors.toList());
            JvmConstructorDefinition constructorDefinition = creation.jvmDefinition(resolver);
            return new NewInvocationBS(constructorDefinition, argumentsPush);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private BytecodeSequence pushInstance(FunctionCall functionCall) {
        Expression function = functionCall.getFunction();
        if (function instanceof FieldAccess) {
            return pushExpression(((FieldAccess) function).getSubject());
        } else if (function instanceof ValueReference) {
            ValueReference valueReference = (ValueReference) function;
            Node declaration = valueReference.resolve(resolver);
            if (declaration instanceof ReflectionBasedSetOfOverloadedMethods) {
                ReflectionBasedSetOfOverloadedMethods methods = (ReflectionBasedSetOfOverloadedMethods) declaration;
                if (methods.isStatic()) {
                    return NoOp.getInstance();
                } else {
                    return push(methods.getInstance());
                }
            } else {
                throw new UnsupportedOperationException(declaration.getClass().getCanonicalName());
            }
        } else if (function instanceof StaticFieldAccess) {
            return NoOp.getInstance();
        } else {
            throw new UnsupportedOperationException(function.getClass().getCanonicalName());
        }
    }

    private BytecodeSequence push(Node node) {
        if (node instanceof ReflectionBaseField) {
            ReflectionBaseField reflectionBaseField = (ReflectionBaseField) node;
            if (reflectionBaseField.isStatic()) {
                return new PushStaticField(reflectionBaseField.toJvmField(resolver));
            } else {
                throw new UnsupportedOperationException();
            }
        } else if (node instanceof Property) {
            Property property = (Property)node;
            JvmFieldDefinition field = new JvmFieldDefinition(this.internalClassName, property.fieldName(), property.getTypeUsage().jvmType(resolver).getDescriptor(), false);
            return new PushInstanceField(field);
        } else {
            throw new UnsupportedOperationException(node.getClass().getCanonicalName());
        }
    }

    private BytecodeSequence executeEpression(Expression expr) {
        if (expr instanceof IntLiteral) {
            return NoOp.getInstance();
        } else if (expr instanceof StringLiteral) {
            return NoOp.getInstance();
        } else if (expr instanceof FunctionCall) {
            FunctionCall functionCall = (FunctionCall)expr;
            BytecodeSequence instancePush = pushInstance(functionCall);
            List<BytecodeSequence> argumentsPush = functionCall.getActualParamValuesInOrder().stream()
                    .map((ap) -> pushExpression(ap))
                    .collect(Collectors.toList());
            JvmMethodDefinition methodDefinition = resolver.findJvmDefinition(functionCall);
            return new ComposedBytecodeSequence(ImmutableList.<BytecodeSequence>builder().add(instancePush).addAll(argumentsPush).add(new MethodInvocationBS(methodDefinition)).build());
        } else {
            throw new UnsupportedOperationException(expr.toString());
        }
    }

    private void appendToStringBuilder(Expression piece, List<BytecodeSequence> elements) {
        TypeUsage pieceType = piece.calcType(resolver);
        if (pieceType.equals(ReferenceTypeUsage.STRING)) {
            elements.add(pushExpression(piece));
            elements.add(new MethodInvocationBS(new JvmMethodDefinition("java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false)));
        } else if (pieceType.isReference()) {
            elements.add(pushExpression(piece));
            elements.add(new MethodInvocationBS(new JvmMethodDefinition("java/lang/StringBuilder", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;", false)));
        } else if (pieceType.equals(BasicTypeUsage.UINT) || (pieceType.isPrimitive() && pieceType.asPrimitiveTypeUsage().isStoredInInt())) {
            elements.add(pushExpression(piece));
            elements.add(new MethodInvocationBS(new JvmMethodDefinition("java/lang/StringBuilder", "append", "(I)Ljava/lang/StringBuilder;", false)));
        } else if (pieceType.equals(PrimitiveTypeUsage.BOOLEAN)) {
            elements.add(pushExpression(piece));
            elements.add(new MethodInvocationBS(new JvmMethodDefinition("java/lang/StringBuilder", "append", "(Z)Ljava/lang/StringBuilder;", false)));
        } else if (pieceType.equals(PrimitiveTypeUsage.CHAR)) {
            elements.add(pushExpression(piece));
            elements.add(new MethodInvocationBS(new JvmMethodDefinition("java/lang/StringBuilder", "append", "(C)Ljava/lang/StringBuilder;", false)));
        } else if (pieceType.equals(PrimitiveTypeUsage.LONG)) {
            elements.add(pushExpression(piece));
            elements.add(new MethodInvocationBS(new JvmMethodDefinition("java/lang/StringBuilder", "append", "(J)Ljava/lang/StringBuilder;", false)));
        } else if (pieceType.equals(PrimitiveTypeUsage.FLOAT)) {
            elements.add(pushExpression(piece));
            elements.add(new MethodInvocationBS(new JvmMethodDefinition("java/lang/StringBuilder", "append", "(F)Ljava/lang/StringBuilder;", false)));
        } else if (pieceType.equals(PrimitiveTypeUsage.DOUBLE)) {
            elements.add(pushExpression(piece));
            elements.add(new MethodInvocationBS(new JvmMethodDefinition("java/lang/StringBuilder", "append", "(D)Ljava/lang/StringBuilder;", false)));
        } else {
            throw new UnsupportedOperationException(pieceType.toString());
        }
    }

    private BytecodeSequence pushExpression(Expression expr) {
        if (expr instanceof IntLiteral) {
            return new PushIntConst(((IntLiteral)expr).getValue());
        } else if (expr instanceof StringLiteral) {
            return new PushStringConst(((StringLiteral)expr).getValue());
        } else if (expr instanceof StaticFieldAccess) {
            StaticFieldAccess staticFieldAccess = (StaticFieldAccess) expr;
            return new PushStaticField(staticFieldAccess.toJvmField(resolver));
        } else if (expr instanceof StringInterpolation) {
            StringInterpolation stringInterpolation = (StringInterpolation) expr;

            List<BytecodeSequence> elements = new ArrayList<>();
            elements.add(new NewInvocationBS(new JvmConstructorDefinition("java/lang/StringBuilder", "()V"), Collections.emptyList()));

            for (Expression piece : stringInterpolation.getElements()) {
                appendToStringBuilder(piece, elements);
            }

            elements.add(new MethodInvocationBS(new JvmMethodDefinition("java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false)));
            return new ComposedBytecodeSequence(elements);
        } else if (expr instanceof ValueReference) {
            ValueReference valueReference = (ValueReference) expr;
            Optional<Integer> index = localVarsSymbolTable.findIndex(valueReference.getName());
            if (index.isPresent()) {
                TypeUsage type = localVarsSymbolTable.findDeclaration(valueReference.getName()).get().calcType(resolver);
                return new PushLocalVar(loadTypeForTypeUsage(type), index.get());
            } else {
                return push(valueReference.resolve(resolver));
            }
        } else if (expr instanceof MathOperation) {
            MathOperation mathOperation = (MathOperation) expr;
            // TODO do proper conversions
            if (!mathOperation.getLeft().calcType(resolver).equals(PrimitiveTypeUsage.INT)) {
                throw new UnsupportedOperationException();
            }
            if (!mathOperation.getRight().calcType(resolver).equals(PrimitiveTypeUsage.INT)) {
                throw new UnsupportedOperationException();
            }

            return new ComposedBytecodeSequence(ImmutableList.of(
                    pushExpression(mathOperation.getLeft()),
                    pushExpression(mathOperation.getRight()),
                    new MathOperationBS(mathOperation.getLeft().calcType(resolver).jvmType(resolver).typeCategory(), mathOperation.getOperator())));
        } else if (expr instanceof BooleanLiteral) {
            return new PushBoolean(((BooleanLiteral) expr).getValue());
        } else if (expr instanceof LogicOperation) {
            LogicOperation logicOperation = (LogicOperation)expr;
            switch (logicOperation.getOperator()) {
                case AND:
                    return new ComposedBytecodeSequence(ImmutableList.of(
                            pushExpression(logicOperation.getLeft()),
                            pushExpression(logicOperation.getRight()),
                            new LogicalAndBS()
                    ));
                case OR:
                    return new ComposedBytecodeSequence(ImmutableList.of(
                            pushExpression(logicOperation.getLeft()),
                            pushExpression(logicOperation.getRight()),
                            new LogicalOrBS()
                    ));
                default:
                    throw new UnsupportedOperationException(logicOperation.getOperator().name());
            }
        } else if (expr instanceof NotOperation) {
            NotOperation notOperation = (NotOperation) expr;
            return new ComposedBytecodeSequence(ImmutableList.of(pushExpression(notOperation.getValue()), new LogicalNotBS()));
        } else if (expr instanceof RelationalOperation) {
            RelationalOperation relationalOperation = (RelationalOperation)expr;
            return new ComposedBytecodeSequence(ImmutableList.of(
                    pushExpression(relationalOperation.getLeft()),
                    pushExpression(relationalOperation.getRight()),
                    new RelationalOperationBS(relationalOperation.getOperator())
            ));
        } else if (expr instanceof FunctionCall) {
            FunctionCall functionCall = (FunctionCall)expr;
            BytecodeSequence instancePush = pushInstance(functionCall);
            List<BytecodeSequence> argumentsPush = functionCall.getActualParamValuesInOrder().stream()
                    .map((ap) -> pushExpression(ap))
                    .collect(Collectors.toList());
            JvmMethodDefinition methodDefinition = resolver.findJvmDefinition(functionCall);
            return new ComposedBytecodeSequence(ImmutableList.<BytecodeSequence>builder().add(instancePush).addAll(argumentsPush).add(new MethodInvocationBS(methodDefinition)).build());
        } else if (expr instanceof Creation) {
            Creation creation = (Creation) expr;
            List<BytecodeSequence> argumentsPush = creation.getActualParamValuesInOrder().stream()
                    .map((ap) -> pushExpression(ap))
                    .collect(Collectors.toList());
            JvmConstructorDefinition constructorDefinition = creation.jvmDefinition(resolver);
            return new NewInvocationBS(constructorDefinition, argumentsPush);
        } else if (expr instanceof ArrayAccess) {
            ArrayAccess arrayAccess = (ArrayAccess) expr;
            return new ComposedBytecodeSequence(ImmutableList.of(
                    pushExpression(arrayAccess.getArray()),
                    pushExpression(arrayAccess.getIndex()),
                    new ArrayAccessBS(arrayAccess.calcType(resolver).jvmType(resolver).typeCategory())));
        } else if (expr instanceof InstanceFieldAccess) {
            InstanceFieldAccess instanceFieldAccess = (InstanceFieldAccess) expr;
            // Ideally it should be desugarized before
            if (instanceFieldAccess.isArrayLength(resolver)) {
                return new ComposedBytecodeSequence(pushExpression(instanceFieldAccess.getSubject()), new ArrayLengthBS());
            } else {
                throw new UnsupportedOperationException(expr.toString());
            }
        } else if (expr instanceof InstanceMethodInvokation) {
            InstanceMethodInvokation instanceMethodInvokation = (InstanceMethodInvokation)expr;
            BytecodeSequence instancePush = pushExpression(instanceMethodInvokation.getSubject());
            List<BytecodeSequence> argumentsPush = instanceMethodInvokation.getActualParamValuesInOrder().stream()
                    .map((ap) -> pushExpression(ap))
                    .collect(Collectors.toList());
            JvmMethodDefinition methodDefinition = instanceMethodInvokation.findJvmDefinition(resolver);
            return new ComposedBytecodeSequence(ImmutableList.<BytecodeSequence>builder().add(instancePush).addAll(argumentsPush).add(new MethodInvocationBS(methodDefinition)).build());
        } else {
            throw new UnsupportedOperationException(expr.getClass().getCanonicalName());
        }
    }

    private int loadTypeForTypeUsage(TypeUsage type) {
        return loadTypeFor(type.jvmType(resolver));
    }

}
