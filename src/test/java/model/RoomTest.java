package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RoomTest {

    @Test
    void createRoom() {
        Room r = new Room(5, new RoomType(BedType.FULL, 10, 2));
        assertEquals(5, r.getNumber());
    }

}