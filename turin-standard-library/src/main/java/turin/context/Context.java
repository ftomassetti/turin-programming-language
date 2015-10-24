package turin.context;

import java.util.Map;
import java.util.Optional;
import java.util.Stack;

public class Context {

    private static final ThreadLocal<Stack<Map<String, Object>>> threadContextStack =
            new ThreadLocal<Stack<Map<String, Object>>>() {
                @Override
                protected Stack<Map<String, Object>> initialValue() {
                    return new Stack<>();
                }
            };

    public static Optional<Object> get(String name) {
        Stack<Map<String, Object>> ctx = threadContextStack.get();
        for (int i=ctx.size();i>0;i--) {
            if (ctx.get(i - 1).containsKey(name)) {
                return Optional.of(ctx.get(i - 1).get(name));
            }
        }
        return Optional.empty();
    }

    public static void enterContext(Map<String, Object> ctxData) {
        Stack<Map<String, Object>> ctx = threadContextStack.get();
        ctx.push(ctxData);
    }

    public static void exitContext() {
        Stack<Map<String, Object>> ctx = threadContextStack.get();
        ctx.pop();
    }

    public static boolean isEmpty() {
        return threadContextStack.get().isEmpty();
    }

}
