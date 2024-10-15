package model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ReservationTest {

    @Test
    void createReservation() {
        Reservation r = new Reservation(LocalDate.of(1000, 10, 10), LocalDate.of(2000, 10, 10), 5, new Guest("A", "B", "C", "D", "E"));
        assertEquals(LocalDate.of(1000, 10, 10), r.getExpectedCheckInDate());
        assertEquals(LocalDate.of(2000, 10, 10), r.getExpectedCheckOutDate());
    }

    @Test
    void setRoom() {
        Reservation r = new Reservation(LocalDate.of(1000, 10, 10), LocalDate.of(2000, 10, 10), 5, new Guest("A", "B", "C", "D", "E"));
        Room room = new Room(5, new RoomType(BedType.FULL, 5, 5));
        r.setRoom(room);
        assertEquals(5, r.getRoom().getNumber());
    }

    @Test
    void checkOut() {
        Reservation r = new Reservation(LocalDate.of(1000, 10, 10), LocalDate.of(2000, 10, 10), 5, new Guest("A", "B", "C", "D", "E"));
        r.setCheckOutDate(LocalDate.now());
        assertNotNull(r.getCheckOutDate());
    }

    @Test
    void checkIn() {
        Reservation r = new Reservation(LocalDate.of(1000, 10, 10), LocalDate.of(2000, 10, 10), 5, new Guest("A", "B", "C", "D", "E"));
        r.setCheckInDate(LocalDate.now());
        assertNotNull(r.getCheckInDate());
    }

}