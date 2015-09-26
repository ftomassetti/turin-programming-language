package me.tomassetti.turin.compiler;

import me.tomassetti.turin.parser.ast.expressions.ActualParam;

import java.util.ArrayList;
import java.util.List;

public class ParamUtils {

    public static boolean allNamedParamsAreAtTheEnd(List<ActualParam> actualParams) {
        boolean findNamed = false;
        for (ActualParam actualParam : actualParams) {
            if (findNamed && (!actualParam.isNamed() && !actualParam.isAsterisk())) {
                return false;
            }
            findNamed = findNamed || actualParam.isNamed();
        }
        return true;
    }

    public static List<ActualParam> unnamedParams(List<ActualParam> actualParams) {
        List<ActualParam> res = new ArrayList<>();
        boolean findNamed = false;
        for (ActualParam actualParam : actualParams) {
            if (findNamed && (!actualParam.isNamed() && !actualParam.isAsterisk())) {
                throw new IllegalArgumentException();
            }
            if (!actualParam.isNamed() && !actualParam.isAsterisk()) {
                res.add(actualParam);
            }
            findNamed = findNamed || actualParam.isNamed();
        }
        return res;
    }

    public static List<ActualParam> namedParams(List<ActualParam> actualParams) {
        List<ActualParam> res = new ArrayList<>();
        boolean findNamed = false;
        for (ActualParam actualParam : actualParams) {
            if (findNamed && (!actualParam.isNamed() && !actualParam.isAsterisk())) {
                throw new IllegalArgumentException();
            }
            if (actualParam.isNamed() && !actualParam.isAsterisk()) {
                res.add(actualParam);
            }
            findNamed = findNamed || actualParam.isNamed();
        }
        return res;
    }


}
