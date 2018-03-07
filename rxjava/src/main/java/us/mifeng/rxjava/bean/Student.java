package us.mifeng.rxjava.bean;

/**
 * Created by 黑夜之火 on 2018/3/6.
 */

public class Student {
    private String name;
    private Course[] course;

    public Student(String name, Course[] course) {
        this.name = name;
        this.course = course;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Course[] getCourse() {
        return course;
    }

    public void setCourse(Course[] course) {
        this.course = course;
    }
}
