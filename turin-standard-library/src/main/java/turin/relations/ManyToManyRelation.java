package turin.relations;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ManyToManyRelation<A, B> implements Relation<A,B> {

    private Map<A, List<B>> byEndpointA = new HashMap<>();
    private Map<B, List<A>> byEndpointB = new HashMap<>();
    private Map<A, Subset> aSubsets = new HashMap<>();
    private Map<B, Subset> bSubsets = new HashMap<>();

    public Subset newASubset() {
        return new Subset();
    }
    public Subset newBSubset() {
        return new Subset();
    }


    @Override
    public void link(A endpointA, B endpointB) {
        if (areLinked(endpointA, endpointB)) {
            return;
        }
        if (!byEndpointA.containsKey(endpointA)) {
            byEndpointA.put(endpointA, new LinkedList<B>());
        }
        if (!byEndpointB.containsKey(endpointB)) {
            byEndpointB.put(endpointB, new LinkedList<A>());
        }
        byEndpointA.get(endpointA).add(endpointB);
        byEndpointB.get(endpointB).add(endpointA);
    }

    @Override
    public void unlink(Object professor, Object course) {
        byEndpointA.get(professor).remove(course);
        byEndpointB.get(course).remove(professor);
    }

    @Override
    public boolean areLinked(Object a, Object b) {
        if (byEndpointB.containsKey(b)) {
            return byEndpointB.get(b).contains(a);
        } else {
            return false;
        }
    }

    public ReferenceMultipleEndpoint getReferenceForB(B b) {
        return new ReferenceMultipleEndpoint(b, byEndpointB, this);
    }

    public ReferenceMultipleEndpoint getReferenceForA(A a) {
        return new ReferenceMultipleEndpoint(a, byEndpointA, this);
    }

    public ReferenceMultipleEndpoint getReferenceForA(A a, Subset subset) {
        return new ReferenceMultipleEndpoint(a, byEndpointA, this, bSubsets, subset);
    }

    public ReferenceMultipleEndpoint getReferenceForB(B b, Subset subset) {
        return new ReferenceMultipleEndpoint(b, byEndpointB, this, aSubsets, subset);
    }
}