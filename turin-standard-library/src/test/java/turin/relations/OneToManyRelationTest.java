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

    @Test
    public void usingSubsetsDoNotMessUpReferencesEndpoints() {
        Course math1 = new Course(100, "Math I");
        Course math2 = new Course(101, "Math II");
        Course eco1 = new Course(200, "Economics I");
        Course info1 = new Course(300, "Info I");
        Course info2 = new Course(301, "Info II");

        Professor prof1 = new Professor("Abraham");
        Professor prof2 = new Professor("James");

        Subset abrahamTeach1stSemester = Teaching.RELATION.newBSubset();
        Subset abrahamTeach2ndSemester = Teaching.RELATION.newBSubset();
        Subset jamesTeach1stSemester = Teaching.RELATION.newBSubset();
        Subset jamesTeach2ndSemester = Teaching.RELATION.newBSubset();

        Teaching.RELATION.link(prof1, math1, abrahamTeach1stSemester);
        Teaching.RELATION.link(prof1, math2, abrahamTeach2ndSemester);
        Teaching.RELATION.link(prof2, eco1, jamesTeach2ndSemester);

        Relation.ReferenceMultipleEndpoint coursesTaughtByAbraham = Teaching.coursesTaughtBy(prof1);
        Relation.ReferenceMultipleEndpoint coursesTaughtByJames = Teaching.coursesTaughtBy(prof2);

        assertEquals(2, coursesTaughtByAbraham.size());
        assertEquals(true, coursesTaughtByAbraham.contains(math1));
        assertEquals(true, coursesTaughtByAbraham.contains(math2));
        assertEquals(false, coursesTaughtByAbraham.contains(eco1));

        assertEquals(1, coursesTaughtByJames.size());
        assertEquals(false, coursesTaughtByJames.contains(math1));
        assertEquals(false, coursesTaughtByJames.contains(math2));
        assertEquals(true, coursesTaughtByJames.contains(eco1));

        Relation.ReferenceSingleEndpoint<Professor,Course> teacherOfMath1 = Teaching.getTeacherOf(math1);
        Relation.ReferenceSingleEndpoint<Professor,Course> teacherOfMath2 = Teaching.getTeacherOf(math2);
        Relation.ReferenceSingleEndpoint<Professor,Course> teacherOfEco1 = Teaching.getTeacherOf(eco1);
        Relation.ReferenceSingleEndpoint<Professor,Course> teacherOfInfo1 = Teaching.getTeacherOf(info1);
        Relation.ReferenceSingleEndpoint<Professor,Course> teacherOfInfo2 = Teaching.getTeacherOf(info2);

        assertEquals(true, teacherOfMath1.isPresent());
        assertEquals("Abraham", teacherOfMath1.get().getName());
        assertEquals(true, teacherOfMath1.isPresent());
        assertEquals("Abraham", teacherOfMath1.get().getName());
        assertEquals(true, teacherOfMath1.isPresent());
        assertEquals("James", teacherOfEco1.get().getName());
        assertEquals(false, teacherOfInfo1.isPresent());
        assertEquals(false, teacherOfInfo2.isPresent());
    }

    @Test
    public void subsetsAreDistinct() {
        Course math1 = new Course(100, "Math I");
        Course math2 = new Course(101, "Math II");
        Course eco1 = new Course(200, "Economics I");
        Course info1 = new Course(300, "Info I");
        Course info2 = new Course(301, "Info II");

        Professor prof1 = new Professor("Abraham");
        Professor prof2 = new Professor("James");

        Subset abrahamTeach1stSemester = Teaching.RELATION.newBSubset();
        Subset abrahamTeach2ndSemester = Teaching.RELATION.newBSubset();
        Subset jamesTeach1stSemester = Teaching.RELATION.newBSubset();
        Subset jamesTeach2ndSemester = Teaching.RELATION.newBSubset();

        Teaching.RELATION.link(prof1, math1, abrahamTeach1stSemester);
        Teaching.RELATION.link(prof1, math2, abrahamTeach2ndSemester);
        Teaching.RELATION.link(prof2, eco1, jamesTeach2ndSemester);

        Relation.ReferenceMultipleEndpoint coursesTaughtByAbraham1stSemester = Teaching.subsetOfCoursesTaughtBy(prof1, abrahamTeach1stSemester);
        Relation.ReferenceMultipleEndpoint coursesTaughtByAbraham2ndSemester = Teaching.subsetOfCoursesTaughtBy(prof1, abrahamTeach2ndSemester);
        Relation.ReferenceMultipleEndpoint coursesTaughtByJames1stSemester = Teaching.subsetOfCoursesTaughtBy(prof2, jamesTeach1stSemester);
        Relation.ReferenceMultipleEndpoint coursesTaughtByJames2ndSemester = Teaching.subsetOfCoursesTaughtBy(prof2, jamesTeach2ndSemester);

        assertEquals(1, coursesTaughtByAbraham1stSemester.size());
        assertEquals(true, coursesTaughtByAbraham1stSemester.contains(math1));
        assertEquals(false, coursesTaughtByAbraham1stSemester.contains(math2));
        assertEquals(false, coursesTaughtByAbraham1stSemester.contains(eco1));

        assertEquals(1, coursesTaughtByAbraham2ndSemester.size());
        assertEquals(false, coursesTaughtByAbraham2ndSemester.contains(math1));
        assertEquals(true, coursesTaughtByAbraham2ndSemester.contains(math2));
        assertEquals(false, coursesTaughtByAbraham2ndSemester.contains(eco1));

        assertEquals(0, coursesTaughtByJames1stSemester.size());
        assertEquals(false, coursesTaughtByJames1stSemester.contains(math1));
        assertEquals(false, coursesTaughtByJames1stSemester.contains(math2));
        assertEquals(false, coursesTaughtByJames1stSemester.contains(eco1));

        assertEquals(1, coursesTaughtByJames2ndSemester.size());
        assertEquals(false, coursesTaughtByJames2ndSemester.contains(math1));
        assertEquals(false, coursesTaughtByJames2ndSemester.contains(math2));
        assertEquals(true, coursesTaughtByJames2ndSemester.contains(eco1));
    }

}
