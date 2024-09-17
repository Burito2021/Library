package net.library.dto;

public class AddUser {

    private String username;
    private String surname;
    private String name;
    private String email;
    private String phoneNumber;
    private String address;

    public String getUsername() {
        return username;
    }

    public AddUser setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getSurname() {
        return surname;
    }

    public AddUser setSurname(String surname) {
        this.surname = surname;
        return this;
    }

    public String getName() {
        return name;
    }

    public AddUser setName(String name) {
        this.name = name;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public AddUser setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public AddUser setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public AddUser setAddress(String address) {
        this.address = address;
        return this;
    }

    @Override
    public String toString() {
        return "AddUser{" +
                "username='" + username + '\'' +
                ", surname='" + surname + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
