package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GuestTest {

    @Test
    void createGuest() {
        Guest g = new Guest("Nový Host", "email@aaa.sk", "TadyBydlím 256", "Nothing to see", "123456789");
        assertEquals("Nový Host", g.getName());
        assertEquals("email@aaa.sk", g.getEmail());
        assertEquals("TadyBydlím 256", g.getAddress());
        assertEquals("Nothing to see", g.getDetails());
        assertEquals("123456789", g.getPhone());
    }

}