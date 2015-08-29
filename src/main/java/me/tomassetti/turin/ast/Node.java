package me.tomassetti.turin.ast;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

public abstract class Node {

    public abstract Iterable<Node> getChildren();

}
