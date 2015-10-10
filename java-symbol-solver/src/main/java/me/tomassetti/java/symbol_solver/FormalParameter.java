package me.tomassetti.java.symbol_solver;

import lombok.Value;
import me.tomassetti.java.symbol_solver.type_usage.JavaTypeUsage;

@Value
public class FormalParameter {
    private JavaTypeUsage type;
    private String name;
}
