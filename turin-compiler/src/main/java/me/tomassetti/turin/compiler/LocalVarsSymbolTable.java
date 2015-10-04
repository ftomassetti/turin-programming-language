package me.tomassetti.turin.compiler;

import me.tomassetti.bytecode_generation.BytecodeSequence;
import me.tomassetti.turin.parser.ast.FormalParameter;
import me.tomassetti.turin.parser.ast.Node;

import java.util.*;

/**
 * An instance is created for each method.
 * The list (and the assignations of indexes) are global for the whole methods
 * but the names can be visible only inside inner blocks.
 */
public class LocalVarsSymbolTable {

    private List<Node> values = new LinkedList<>();
    private List<Block> blockOfDeckaration = new LinkedList<>();
    private List<String> orderedNames = new LinkedList<>();
    private int startIndex;
    private LocalVarsSymbolTable parent;
    private Block currentBlock = new Block(null);
    private Map<String, BytecodeSequence> aliases = new HashMap<>();

    public void recordAlias(String name, BytecodeSequence bs) {
        aliases.put(name, bs);
    }

    public boolean hasAlias(String name) {
        return aliases.containsKey(name);
    }

    public BytecodeSequence getAlias(String name) {
        if (!hasAlias(name)) {
            throw new IllegalArgumentException();
        }
        return aliases.get(name);
    }

    public void add(FormalParameter formalParameter) {
        add(formalParameter.getName(), formalParameter);
    }

    // Each instance is equals just to itself, by design.
    private class Block {
        Block parent;

        public Block(Block parent) {
            this.parent = parent;
        }
    }

    private LocalVarsSymbolTable(int startIndex, LocalVarsSymbolTable parent) {
        this.startIndex = startIndex;
        this.parent = parent;
    }

    public static LocalVarsSymbolTable forStaticMethod() {
        // no space needed for "this"
        return new LocalVarsSymbolTable(0, null);
    }

    public static LocalVarsSymbolTable forInstanceMethod() {
        // space needed for "this"
        return new LocalVarsSymbolTable(1, null);
    }

    /**
     * Return the index in the symbol table.
     */
    public int add(String name, Node value) {
        values.add(value);
        orderedNames.add(name);
        blockOfDeckaration.add(currentBlock);
        return orderedNames.size() - 1;
    }

    public Optional<Integer> findIndex(String name) {
        return findIndexInBlock(name, currentBlock);
    }

    public Optional<Node> findDeclaration(String name) {
        Optional<Integer> index = findIndex(name);
        if (index.isPresent()) {
            return Optional.of(values.get(index.get() - startIndex));
        } else {
            return Optional.empty();
        }
    }

    private Optional<Integer> findIndexInBlock(String name, Block block) {
        for (int i=0;i<orderedNames.size();i++) {
            if (orderedNames.get(i).equals(name)) {
                if (blockOfDeckaration.get(i).equals(block)) {
                    return Optional.of(i + startIndex);
                }
            }
        }
        if (block.parent == null) {
            return Optional.empty();
        } else {
            return findIndexInBlock(name, block.parent);
        }
    }

    public void enterBlock() {
        Block newBlock = new Block(currentBlock);
        currentBlock = newBlock;
    }

    public void exitBlock() {
        if (currentBlock.parent == null) {
            throw new IllegalStateException();
        }
        currentBlock = currentBlock.parent;
    }

}
