package edu.coloradomesa.cs.conradar;

public class Contact {
    private String firstName;
    private String lastName;
    private String email;
    private String message;
    private boolean cellPhone;

    public Contact(String firstName, String lastName, String email, String message, boolean cellPhone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.message = message;
        this.cellPhone = cellPhone;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isCellPhone() {
        return cellPhone;
    }

    public void setCellPhone(boolean cellPhone) {
        this.cellPhone = cellPhone;
    }
}
