package turin.relations;

public class Professor {
    String name;

    @Override
    public String toString() {
        return "Professor{" +
                "name='" + name + '\'' +
                '}';
    }

    public String getName() {
        return name;
    }

    public Professor(String name) {
        this.name = name;
    }
}
