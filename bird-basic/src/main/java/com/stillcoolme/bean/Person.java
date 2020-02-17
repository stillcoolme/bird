package com.stillcoolme.bean;

import java.util.Objects;

public class Person{
    private String name;
    private int age ;
    private char sex ;

    public Person() {
        super ();
        System.out.println("haha");
    }

    public Person(String name) {
        super ();
        this.name = name;
    }

    public Person(String name, int age, char sex) {
        super ();
        this .name = name;
        this .age = age;
        this .sex = sex;
    }

    public String getName() {
        return name ;
    }

    public void setName(String name) {
        this .name = name;
    }

    public int getAge() {
        return age ;
    }

    public void setAge(int age) {
        this .age = age;
    }

    public char getSex() {
        return sex ;
    }

    public void setSex(char sex) {
        this .sex = sex;
    }
    public void eat()
    {
        System. out .println("吃了" );
    }

    @Override
    public String toString() {
        return "Person [name=" + name + ", age=" + age + ", sex=" + sex + "]" ;
    }

    public String sayHello(String name, int age, char sex) {
        // TODO Auto-generated method stub
        return "姓名:" + name + "年龄："+ age + "性别:" + sex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return age == person.age &&
                sex == person.sex &&
                name.equals(person.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, age, sex);
    }
}
