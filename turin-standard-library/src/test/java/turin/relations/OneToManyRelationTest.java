package turin.relations;

import org.junit.Test;

import static org.junit.Assert.*;

public class OneToManyRelationTest {

    @Test
    public void settingALinkTheTeacherIsSet() {
        Course math1 = new Course(100, "Math I");
        Course math2 = new Course(101, "Math II");
        Course eco1 = new Course(200, "Economics I");

        Professor prof1 = new Professor("Abraham");
        Professor prof2 = new Professor("James");

        Teaching.RELATION.link(prof1, math1);
        Teaching.RELATION.link(prof1, math2);

        Relation.ReferenceSingleEndpoint<Professor,Course> teacherOfMath1 = Teaching.getTeacherOf(math1);
        Relation.ReferenceSingleEndpoint<Professor,Course> teacherOfMath2 = Teaching.getTeacherOf(math2);
        Relation.ReferenceSingleEndpoint<Professor,Course> teacherOfEco1 = Teaching.getTeacherOf(eco1);

        assertEquals(true, teacherOfMath1.isPresent());
        assertEquals("Abraham", teacherOfMath1.get().getName());

        assertEquals(true, teacherOfMath2.isPresent());
        assertEquals("Abraham", teacherOfMath2.get().getName());

        assertEquals(false, teacherOfEco1.isPresent());
    }

    @Test
    public void settingALinkTheCoursesAreSet() {
        Course math1 = new Course(100, "Math I");
        Course math2 = new Course(101, "Math II");
        Course eco1 = new Course(200, "Economics I");

        Professor prof1 = new Professor("Abraham");
        Professor prof2 = new Professor("James");

        Teaching.RELATION.link(prof1, math1);
        Teaching.RELATION.link(prof1, math2);

        Relation.ReferenceMultipleEndpoint coursesTaughtByAbraham = Teaching.coursesTaughtBy(prof1);
        Relation.ReferenceMultipleEndpoint coursesTaughtByJames = Teaching.coursesTaughtBy(prof2);

        assertEquals(2, coursesTaughtByAbraham.size());
        assertEquals(true, coursesTaughtByAbraham.contains(math1));
        assertEquals(true, coursesTaughtByAbraham.contains(math2));
        assertEquals(false, coursesTaughtByAbraham.contains(eco1));

        assertEquals(0, coursesTaughtByJames.size());
        assertEquals(false, coursesTaughtByJames.contains(math1));
        assertEquals(false, coursesTaughtByJames.contains(math2));
        assertEquals(false, coursesTaughtByJames.contains(eco1));
    }

    @Test
    public void aLinkCanReplaceThePreviousProfessor() {
        Course math1 = new Course(100, "Math I");
        Course math2 = new Course(101, "Math II");
        Course eco1 = new Course(200, "Economics I");

        Professor prof1 = new Professor("Abraham");
        Professor prof2 = new Professor("James");

        Teaching.RELATION.link(prof1, math1);
        Teaching.RELATION.link(prof2, math1);

        Relation.ReferenceMultipleEndpoint coursesTaughtByAbraham = Teaching.coursesTaughtBy(prof1);
        Relation.ReferenceMultipleEndpoint coursesTaughtByJames = Teaching.coursesTaughtBy(prof2);

        assertEquals(0, coursesTaughtByAbraham.size());
        assertEquals(false, coursesTaughtByAbraham.contains(math1));
        assertEquals(false, coursesTaughtByAbraham.contains(math2));
        assertEquals(false, coursesTaughtByAbraham.contains(eco1));

        assertEquals(1, coursesTaughtByJames.size());
        assertEquals(true, coursesTaughtByJames.contains(math1));
        assertEquals(false, coursesTaughtByJames.contains(math2));
        assertEquals(false, coursesTaughtByJames.contains(eco1));

        Relation.ReferenceSingleEndpoint<Professor,Course> teacherOfMath1 = Teaching.getTeacherOf(math1);
        assertEquals(true, teacherOfMath1.isPresent());
        assertEquals("James", teacherOfMath1.get().getName());
    }

}
