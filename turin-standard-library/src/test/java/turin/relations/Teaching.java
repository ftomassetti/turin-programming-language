package turin.relations;

public class Teaching {

    public static final OneToManyRelation<Professor, Course> RELATION = new OneToManyRelation<Professor, Course>();

    public static Relation.ReferenceSingleEndpoint<Professor,Course> getTeacherOf(Course course){
        return RELATION.getReferenceForB(course);
    }

    public static Relation.ReferenceMultipleEndpoint<Professor,Course> coursesTaughtBy(Professor professor) {
        return RELATION.getReferenceForA(professor);
    }

    public static Relation.ReferenceMultipleEndpoint<Professor,Course> subsetOfCoursesTaughtBy(Professor professor, Subset subset) {
        return RELATION.getReferenceForA(professor, subset);
    }
}
