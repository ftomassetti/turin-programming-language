package turin.relations;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class OneToManyRelation<A, B> implements Relation<A,B> {

    private Map<A, List<B>> byEndpointA = new HashMap<>();
    private Map<B, A> byEndpointB = new HashMap<>();
    private Map<B, Subset> bSubsets = new HashMap<>();

    public Subset newBSubset() {
        return new Subset();
    }

    public void link(A endpointA, B endpointB, Subset bSubset) {
        link(endpointA, endpointB);
        bSubsets.put(endpointB, bSubset);
    }

    @Override
    public void link(A endpointA, B endpointB) {
        if (areLinked(endpointA, endpointB)) {
            return;
        }
        if (byEndpointB.containsKey(endpointB)) {
            unlink(byEndpointB.get(endpointB), endpointB);
        }
        if (!byEndpointA.containsKey(endpointA)) {
            byEndpointA.put(endpointA, new LinkedList<B>());
        }
        byEndpointA.get(endpointA).add(endpointB);
        byEndpointB.put(endpointB, endpointA);
    }

    @Override
    public void unlink(Object endpointA, Object endpointB) {
        byEndpointA.get(endpointA).remove(endpointB);
        byEndpointB.remove(endpointB);
        bSubsets.remove(endpointB);
    }

    @Override
    public boolean areLinked(Object a, Object b) {
        if (byEndpointB.containsKey(b)) {
            return byEndpointB.get(b).equals(a);
        } else {
            return false;
        }
    }

    public ReferenceSingleEndpoint getReferenceForB(B b) {
        return new ReferenceSingleEndpoint(b, byEndpointB, this);
    }

    public ReferenceSingleEndpoint getReferenceForSubsetB(B b) {
        return new ReferenceSingleEndpoint(b, byEndpointB, this);
    }

    public ReferenceMultipleEndpoint getReferenceForA(A a) {
        return new ReferenceMultipleEndpoint(a, byEndpointA, this);
    }

    public ReferenceMultipleEndpoint getReferenceForA(A a, Subset subset) {
        return new ReferenceMultipleEndpoint(a, byEndpointA, this, bSubsets, subset);
    }

}