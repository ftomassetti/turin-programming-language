package me.tomassetti.turin.typesystem;

import me.tomassetti.jvm.JvmMethodDefinition;
import me.tomassetti.jvm.JvmType;
import me.tomassetti.turin.parser.analysis.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.expressions.ActualParam;
import me.tomassetti.turin.parser.ast.typeusage.ArrayTypeUsage;
import me.tomassetti.turin.parser.ast.typeusage.PrimitiveTypeUsage;
import me.tomassetti.turin.parser.ast.typeusage.ReferenceTypeUsage;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsageNode;
import me.tomassetti.turin.symbols.Symbol;

import java.util.List;
import java.util.Map;

public interface TypeUsage extends Symbol {
    JvmType jvmType(SymbolResolver resolver);

    boolean isReferenceTypeUsage();

    ReferenceTypeUsage asReferenceTypeUsage();

    ArrayTypeUsage asArrayTypeUsage();

    JvmMethodDefinition findMethodFor(String name, List<JvmType> argsTypes, SymbolResolver resolver, boolean staticContext);

    boolean canBeAssignedTo(TypeUsageNode type, SymbolResolver resolver);

    boolean isArray();

    boolean isPrimitive();

    boolean isReference();

    PrimitiveTypeUsage asPrimitiveTypeUsage();

    Node getFieldOnInstance(String fieldName, Node instance, SymbolResolver resolver);

    TypeUsageNode returnTypeWhenInvokedWith(List<ActualParam> actualParams, SymbolResolver resolver);

    TypeUsageNode returnTypeWhenInvokedWith(String methodName, List<ActualParam> actualParams, SymbolResolver resolver, boolean staticContext);

    boolean isMethodOverloaded(SymbolResolver resolver, String methodName);

    boolean isOverloaded();

    boolean isVoid();

    TypeUsageNode replaceTypeVariables(Map<String, TypeUsageNode> typeParams);
}
