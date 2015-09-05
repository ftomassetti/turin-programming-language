package me.tomassetti.turin.compiler;

import me.tomassetti.turin.parser.ast.expressions.ValueReference;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

public class LocalVarSymbolTableTest {

    @Test
    public void findInEmptySymbolTable() {
        LocalVarsSymbolTable localVarsSymbolTable = LocalVarsSymbolTable.forInstanceMethod();
        Optional<Integer> index = localVarsSymbolTable.findIndex("foo");
        assertEquals(false, index.isPresent());
    }

    @Test
    public void findOnlyValueSymbolTableForInstanceMethod() {
        LocalVarsSymbolTable localVarsSymbolTable = LocalVarsSymbolTable.forInstanceMethod();
        localVarsSymbolTable.add("foo", new ValueReference("foo"));
        Optional<Integer> index = localVarsSymbolTable.findIndex("foo");
        assertEquals(true, index.isPresent());
        assertEquals(1, index.get().intValue());
    }

    @Test
    public void findOnlyValueSymbolTableForStatucMethod() {
        LocalVarsSymbolTable localVarsSymbolTable = LocalVarsSymbolTable.forStaticMethod();
        localVarsSymbolTable.add("foo", new ValueReference("foo"));
        Optional<Integer> index = localVarsSymbolTable.findIndex("foo");
        assertEquals(true, index.isPresent());
        assertEquals(0, index.get().intValue());
    }

    @Test
    public void findAmongManyValuesSymbolTableForInstanceMethod() {
        LocalVarsSymbolTable localVarsSymbolTable = LocalVarsSymbolTable.forInstanceMethod();
        localVarsSymbolTable.add("foo1", new ValueReference("foo1"));
        localVarsSymbolTable.add("foo2", new ValueReference("foo2"));
        localVarsSymbolTable.add("foo3", new ValueReference("foo3"));
        Optional<Integer> index = localVarsSymbolTable.findIndex("foo2");
        assertEquals(true, index.isPresent());
        assertEquals(2, index.get().intValue());
    }

    @Test
    public void findInInternalScopeSymbolTableForInstanceMethod() {
        LocalVarsSymbolTable localVarsSymbolTable = LocalVarsSymbolTable.forInstanceMethod();
        localVarsSymbolTable.add("foo1", new ValueReference("foo1"));
        localVarsSymbolTable.add("foo2", new ValueReference("foo2"));
        localVarsSymbolTable.add("foo3", new ValueReference("foo3"));
        localVarsSymbolTable.enterBlock();
        localVarsSymbolTable.add("foo1", new ValueReference("foo1"));
        localVarsSymbolTable.add("foo2", new ValueReference("foo2"));
        localVarsSymbolTable.add("foo3", new ValueReference("foo3"));
        Optional<Integer> index = localVarsSymbolTable.findIndex("foo2");
        assertEquals(true, index.isPresent());
        assertEquals(5, index.get().intValue());
    }

    @Test
    public void ignoreAbandonedScopeWhilefindInInternalScopeSymbolTableForInstanceMethod() {
        LocalVarsSymbolTable localVarsSymbolTable = LocalVarsSymbolTable.forInstanceMethod();
        localVarsSymbolTable.add("foo1", new ValueReference("foo1"));
        localVarsSymbolTable.add("foo2", new ValueReference("foo2"));
        localVarsSymbolTable.add("foo3", new ValueReference("foo3"));
        localVarsSymbolTable.enterBlock();
        localVarsSymbolTable.add("foo1", new ValueReference("foo1"));
        localVarsSymbolTable.add("foo2", new ValueReference("foo2"));
        localVarsSymbolTable.add("foo3", new ValueReference("foo3"));
        localVarsSymbolTable.exitBlock();
        Optional<Integer> index = localVarsSymbolTable.findIndex("foo2");
        assertEquals(true, index.isPresent());
        assertEquals(2, index.get().intValue());
    }

    @Test
    public void findInVeryInternalScopeSymbolTableForInstanceMethod() {
        LocalVarsSymbolTable localVarsSymbolTable = LocalVarsSymbolTable.forInstanceMethod();
        localVarsSymbolTable.add("foo1", new ValueReference("foo1"));
        localVarsSymbolTable.add("foo2", new ValueReference("foo2"));
        localVarsSymbolTable.add("foo3", new ValueReference("foo3"));
        localVarsSymbolTable.enterBlock();
        localVarsSymbolTable.add("foo1", new ValueReference("foo1"));
        localVarsSymbolTable.add("foo2", new ValueReference("foo2"));
        localVarsSymbolTable.add("foo3", new ValueReference("foo3"));
        localVarsSymbolTable.enterBlock();
        localVarsSymbolTable.add("foo1", new ValueReference("foo1"));
        localVarsSymbolTable.add("foo2", new ValueReference("foo2"));
        localVarsSymbolTable.add("foo3", new ValueReference("foo3"));
        localVarsSymbolTable.enterBlock();
        localVarsSymbolTable.add("foo1", new ValueReference("foo1"));
        localVarsSymbolTable.add("foo2", new ValueReference("foo2"));
        localVarsSymbolTable.add("foo3", new ValueReference("foo3"));
        localVarsSymbolTable.enterBlock();
        localVarsSymbolTable.add("foo1", new ValueReference("foo1"));
        localVarsSymbolTable.add("foo2", new ValueReference("foo2"));
        localVarsSymbolTable.add("foo3", new ValueReference("foo3"));
        Optional<Integer> index = localVarsSymbolTable.findIndex("foo2");
        assertEquals(true, index.isPresent());
        assertEquals(14, index.get().intValue());
    }

}
