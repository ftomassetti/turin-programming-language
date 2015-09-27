package me.tomassetti.turin.util;

public class Either<L, R> {

    private L left;

    public L getLeft() {
        if (!isLeft()) {
            throw new UnsupportedOperationException();
        }
        return left;
    }

    public R getRight() {
        if (!isRight()) {
            throw new UnsupportedOperationException();
        }
        return right;
    }

    public boolean isLeft() {
        return left != null;
    }

    public boolean isRight() {
        return right != null;
    }

    private R right;

    public static <L, R> Either<L, R> left(L left) {
        if (left == null) {
            throw new NullPointerException();
        }
        return new Either(left, null);
    }

    public static <L, R> Either<L, R> right(R right) {
        if (right == null) {
            throw new NullPointerException();
        }
        return new Either(null, right);
    }

    private Either(L left, R right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public String toString() {
        if (left != null) {
            return "Either.left(" + left + ")";
        } else {
            return "Either.right(" + right + ")";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Either)) return false;

        Either either = (Either) o;

        if (left != null ? !left.equals(either.left) : either.left != null) return false;
        if (right != null ? !right.equals(either.right) : either.right != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = left != null ? left.hashCode() : 0;
        result = 31 * result + (right != null ? right.hashCode() : 0);
        return result;
    }
}
