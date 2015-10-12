package turin.relations;

import java.util.*;

public interface Relation<A, B> {
    public void link(A endpointA, B endpointB);

    public void unlink(Object endpointA, Object endpointB);

    public boolean areLinked(Object endpointA, Object endpointB);

    public static class ReferenceSingleEndpoint<A, B> {

        private B b;
        private Map<B, A> byEndpointB;
        private Relation<A,B> relation;

        public ReferenceSingleEndpoint(B b, Map<B, A> byEndpointB, Relation<A, B> relation) {
            this.b = b;
            this.byEndpointB = byEndpointB;
            this.relation = relation;
        }

        public boolean isPresent() {
            return byEndpointB.containsKey(b);
        }

        public A get() {
            return byEndpointB.get(b);
        }

        public void set(A a) {
            relation.link(a, b);
        }
    }

    public static class ReferenceMultipleEndpoint<A, B> implements List<B> {

        private A a;
        private Relation<A, B> relation;
        private Map<A, List<B>> byEndpointA;

        public ReferenceMultipleEndpoint(A a, Map<A, List<B>> byEndpointA, Relation<A, B> relation) {
            this.a = a;
            this.byEndpointA = byEndpointA;
            this.relation = relation;
        }

        @Override
        public int size() {
            if (byEndpointA.containsKey(a)) {
                return byEndpointA.get(a).size();
            } else {
                return 0;
            }
        }

        @Override
        public boolean isEmpty() {
            if (byEndpointA.containsKey(a)) {
                return byEndpointA.get(a).isEmpty();
            } else {
                return true;
            }
        }

        @Override
        public boolean contains(Object o) {
            if (byEndpointA.containsKey(a)) {
                return byEndpointA.get(a).contains(o);
            } else {
                return false;
            }
        }

        @Override
        public Iterator<B> iterator() {
            if (byEndpointA.containsKey(a)) {
                return byEndpointA.get(a).iterator();
            } else {
                return Collections.emptyIterator();
            }
        }

        @Override
        public Object[] toArray() {
            if (byEndpointA.containsKey(a)) {
                return byEndpointA.get(a).toArray();
            } else {
                return new Object[]{};
            }
        }

        @Override
        public <T> T[] toArray(T[] a) {
            if (byEndpointA.containsKey(a)) {
                return byEndpointA.get(a).toArray(a);
            } else {
                return Arrays.copyOf(a, 0);
            }
        }

        @Override
        public boolean add(B b) {
            if (relation.areLinked(a, b)) {
                return false;
            } else {
                relation.link(a, b);
                return true;
            }
        }

        @Override
        public boolean remove(Object o) {
            if (relation.areLinked(a, o)) {
                relation.unlink(a, o);
                return true;
            } else {
                return false;
            }
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            for (Object o : c) {
                if (!contains(o)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean addAll(Collection<? extends B> c) {
            boolean changed = false;
            for (B o : c) {
                boolean partial = add(o);
                changed = changed || partial;
            }
            return changed;
        }

        @Override
        public boolean addAll(int index, Collection<? extends B> c) {
            boolean changed = false;
            for (B o : c) {
                boolean partial = !relation.areLinked(a, o);
                if (partial) {
                    add(index, o);
                    changed = true;
                    index++;
                }
            }
            return changed;
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            boolean changed = false;
            for (Object o : c) {
                if (!contains(o)) {
                    boolean partial = remove(o);
                    changed = changed || partial;
                }
            }
            return changed;
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            boolean changed = false;
            for (Object o : c) {
                boolean partial = remove(o);
                changed = changed || partial;
            }
            return changed;
        }

        @Override
        public void clear() {
            if (isEmpty()) {
                return;
            }
            relation.unlink(a, iterator().next());
            clear();
        }

        @Override
        public B get(int index) {
            return byEndpointA.get(a).get(index);
        }

        @Override
        public B set(int index, B element) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(int index, B element) {
            throw new UnsupportedOperationException();
        }

        @Override
        public B remove(int index) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int indexOf(Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int lastIndexOf(Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ListIterator<B> listIterator() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ListIterator<B> listIterator(int index) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<B> subList(int fromIndex, int toIndex) {
            throw new UnsupportedOperationException();
        }


    }
}
