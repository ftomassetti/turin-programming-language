package turin.relations;


import java.util.HashMap;
import java.util.Map;

public class OneToOneRelation<A, B> implements Relation<A,B> {

    private Map<A, B> byEndpointA = new HashMap<>();
    private Map<B, A> byEndpointB = new HashMap<>();

    @Override
    public void link(A endpointA, B endpointB) {
        if (areLinked(endpointA, endpointB)) {
            return;
        }
        if (byEndpointB.containsKey(endpointB)) {
            unlink(byEndpointB.get(endpointB), endpointB);
        }
        if (byEndpointA.containsKey(endpointA)) {
            unlink(byEndpointA.get(endpointA), endpointA);
        }
        byEndpointB.put(endpointB, endpointA);
        byEndpointA.put(endpointA, endpointB);
    }

    @Override
    public void unlink(Object professor, Object course) {
        byEndpointA.remove(professor);
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

    public ReferenceSingleEndpoint getReferenceForA(A a) {
        return new ReferenceSingleEndpoint(a, byEndpointA, this);
    }

}