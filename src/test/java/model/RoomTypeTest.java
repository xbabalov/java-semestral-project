package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RoomTypeTest {

    @Test
    void createRoomType() {
        RoomType r = new RoomType(BedType.FULL, 10, 5);
        assertEquals(BedType.FULL, r.getBedType());
        assertEquals(10, r.getPrice());
        assertEquals(5, r.getNumberOfBeds());
    }

}