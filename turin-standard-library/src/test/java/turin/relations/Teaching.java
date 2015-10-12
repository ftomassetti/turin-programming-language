package turin.relations;

public class Teaching {

    public static final OneToManyRelation<Professor, Course> RELATION = new OneToManyRelation<Professor, Course>();

    public static Relation.ReferenceSingleEndpoint<Professor,Course> getTeacherOf(Course course){
        return RELATION.getReferenceForB(course);
    }

    public static Relation.ReferenceMultipleEndpoint coursesTaughtBy(Professor professor) {
        return RELATION.getReferenceForA(professor);
    }
}
