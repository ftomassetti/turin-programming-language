package me.tomassetti.turin;

public class MangaCharacter {
    private int age;
    private String name;

    public MangaCharacter(String name, int age) {
        if (name == null) {
            throw new NullPointerException("name cannot be null");
        }
        if (age < 0) {
            throw new IllegalArgumentException("age should be positive");
        }
        this.name = name;
        this.age = age;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        if (age < 0) {
            throw new IllegalArgumentException("age should be positive");
        }
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null) {
            throw new NullPointerException("name cannot be null");
        }
        this.name = name;
    }
}
