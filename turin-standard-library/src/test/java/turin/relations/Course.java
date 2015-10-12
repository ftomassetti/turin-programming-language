package turin.relations;

public class Course {

    String name;
    int code;

    @Override
    public String toString() {
        return "Course{" +
                "name='" + name + '\'' +
                ", code=" + code +
                '}';
    }

    public Course(int code, String name) {
        this.code = code;
        this.name = name;
    }
}
