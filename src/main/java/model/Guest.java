package model;

public class Guest {

    private final String name;
    private final String email;
    private final String address;
    private final String details;
    private final String phone;

    public Guest(String name, String email, String address, String details, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.details = details;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }

    public String getDetails() {
        return details;
    }

    public String getPhone() {
        return phone;
    }

}
