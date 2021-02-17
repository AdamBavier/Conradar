package edu.coloradomesa.cs.conradar;

public class Contact {
    private String firstName;
    private String lastName;
    private String email;
    private String message;

    public Contact(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public Contact(String firstName, String lastName, String email, String message) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this. message = message;
    }

    public void show1() {
        System.out.println(firstName);
        System.out.println(lastName);
        System.out.println(email);
    }

    public void show2() {
        System.out.println(firstName);
        System.out.println(lastName);
        System.out.println(email);
        System.out.println(message);
    }
}
