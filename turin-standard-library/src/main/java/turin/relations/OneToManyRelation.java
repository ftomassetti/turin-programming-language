package turin.relations;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class OneToManyRelation<A, B> implements Relation<A,B> {

    private Map<A, List<B>> byEndpointA = new HashMap<>();
    private Map<B, A> byEndpointB = new HashMap<>();

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
    public void unlink(Object professor, Object course) {
        byEndpointA.get(professor).remove(course);
        byEndpointB.remove(course);
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

    public ReferenceMultipleEndpoint getReferenceForA(A a) {
        return new ReferenceMultipleEndpoint(a, byEndpointA, this);
    }

}