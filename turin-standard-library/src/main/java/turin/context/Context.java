package turin.context;

import java.util.Optional;
import java.util.Stack;

public abstract class Context<V> {

    private final ThreadLocal<Stack<V>> values =
            new ThreadLocal<Stack<V>>() {
                @Override
                protected Stack<V> initialValue() {
                    return new Stack<>();
                }
            };

    public Optional<V> get() {
        Stack<V> ctx = values.get();
        if (ctx.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(ctx.get(ctx.size() - 1));
        }
    }

    public void enterContext(V value) {
        Stack<V> ctx = values.get();
        ctx.push(value);
    }

    public void exitContext() {
        Stack<V> ctx = values.get();
        ctx.pop();
    }

    public boolean isEmpty() {
        Stack<V> ctx = values.get();
        return ctx.isEmpty();
    }

}
