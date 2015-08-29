package me.tomassetti.turin.ast;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

public class Node {

    private List<Node> children = new ArrayList<>();

    public void addChild(Node child) {
        children.add(child);
    }

    public Iterable<Node> getChildren() {
        return ImmutableList.copyOf(children);
    }

}
