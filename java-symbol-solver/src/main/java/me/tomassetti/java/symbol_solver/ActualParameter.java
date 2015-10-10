package me.tomassetti.java.symbol_solver;

import lombok.Value;
import me.tomassetti.jvm.JvmType;

@Value
public class ActualParameter {
    private JvmType valueType;
}
