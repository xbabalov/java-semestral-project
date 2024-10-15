package model;

import java.time.LocalDate;

public class Reservation {

    private Long id;
    private LocalDate expectedCheckInDate;
    private LocalDate expectedCheckOutDate;
    private LocalDate checkInDate = null;
    private LocalDate checkOutDate = null;
    private int numGuests;
    private Guest guest;
    private Room room;

    public Reservation(LocalDate expectedCheckInDate, LocalDate expectedCheckOutDate, int numGuests, Guest guest) {
        this.expectedCheckInDate = expectedCheckInDate;
        this.expectedCheckOutDate = expectedCheckOutDate;
        this.numGuests = numGuests;
        this.guest = guest;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reservation that = (Reservation) o;
        return id != null && id.equals(that.id);
    }

    public String toString() {
        return "Reservation: " + room.toString() + " " + expectedCheckInDate.toString() + " " + expectedCheckOutDate.toString() + " " + numGuests + " Guest: " + guest.toString();
    }


    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getExpectedCheckInDate() {
        return expectedCheckInDate;
    }

    public LocalDate getExpectedCheckOutDate() {
        return expectedCheckOutDate;
    }

    public Integer getNumGuests() {
        return numGuests;
    }

    public Guest getGuest() {
        return guest;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public String getGuestName() {
        return guest.getName();
    }

    public String getGuestEmail() {
        return guest.getEmail();
    }

    public String getGuestPhone() {
        return guest.getPhone();
    }

    public String getGuestAddress() {
        return guest.getAddress();
    }

    public String getGuestDetails() {
        return guest.getDetails();
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckInDate(LocalDate date) {
        this.checkInDate = date;
    }

    public void setCheckOutDate(LocalDate date) {
        this.checkOutDate = date;
    }

    public void setExpectedCheckInDate(LocalDate expectedCheckInDate) {
        this.expectedCheckInDate = expectedCheckInDate;
    }

    public void setExpectedCheckOutDate(LocalDate expectedCheckOutDate) {
        this.expectedCheckOutDate = expectedCheckOutDate;
    }

    public void setNumGuests(int numGuests) {
        this.numGuests = numGuests;
    }

    public void setGuest(Guest guest) {
        this.guest = guest;
    }
}