package data;

import model.*;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ReservationDaoTest {

    private static EmbeddedDataSource dataSource;
    private ReservationDao reservationDao;
    private RoomDao roomDao;

    @BeforeAll
    static void initTestDataSource() {
        dataSource = new EmbeddedDataSource();
        dataSource.setDatabaseName("memory:hotel-reservation-test");
        dataSource.setCreateDatabase("create");
    }

    @BeforeEach
    void createReservationDao() throws SQLException {
        roomDao = new RoomDao(dataSource);
        roomDao.initTable();
        reservationDao = new ReservationDao(dataSource);
        reservationDao.initTable();
        try (var connection = dataSource.getConnection();
             var st = connection.createStatement()) {
            st.executeUpdate("DELETE FROM APP.RESERVATION");
        }
    }

    @AfterEach
    void cleanUp() {
        roomDao.getTableManager().dropTable();
        reservationDao.getTableManager().dropTable();
    }

    @Test
    void createReservation() {
        Room room1 = new Room(1, new RoomType(BedType.FULL, 20, 2));
        roomDao.create(room1);
        Guest guest = new Guest("Gu Est", "abc@gmail.com", "Brno", "", "123456789");
        Reservation res = new Reservation(LocalDate.of(2018, 4, 12), LocalDate.of(2018, 5, 7), 1, guest);
        res.setRoom(room1);
        reservationDao.create(res);

        assertNotNull(res);
    }


    @Test
    void createReservationWithExistingId() {
        Room room1 = new Room(1, new RoomType(BedType.FULL, 20, 2));
        roomDao.create(room1);
        Guest guest = new Guest("Gu Est", "abc@gmail.com", "Brno", "", "123456789");
        Reservation res = new Reservation(LocalDate.of(2018, 4, 12), LocalDate.of(2018, 5, 7), 1, guest);
        res.setRoom(room1);
        res.setId(420L);

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> reservationDao.create(res))
                .withMessage("Reservation already has ID: " + res);
    }


    @Test
    void findAllEmpty() {
        assertThat(reservationDao.findAll()).isEmpty();
    }

    @Test
    void findAll() {
        RoomType rt1 = new RoomType(BedType.FULL, 20, 2);
        RoomType rt2 = new RoomType(BedType.QUEEN, 10, 1);
        RoomType rt3 = new RoomType(BedType.KING, 30, 2);
        Room room1 = new Room(1, rt1);
        Room room2 = new Room(2, rt2);
        Room room3 = new Room(3, rt3);
        roomDao.create(room1);
        roomDao.create(room2);
        roomDao.create(room3);

        Guest guest = new Guest("Ho Pep", "alik@seznam.cz", "Brno", "", "+420905174925");
        Guest guest2 = new Guest("Mic Nov", "rofl@centrum.lol", "Praha 123", "Jsem vegan nebo co", "123519681");
        Guest guest3 = new Guest("David Cb", "mail@mail.cz", "HK", "", "12456");

        Reservation res = new Reservation(LocalDate.of(2018, 4, 12), LocalDate.of(2018, 5, 7), 1, guest);
        Reservation res2 = new Reservation(LocalDate.of(2017, 2, 4), LocalDate.now(), 1, guest2);
        Reservation res3 = new Reservation(LocalDate.of(2020, 1, 4), LocalDate.of(2020, 2, 7), 1, guest3);
        res.setRoom(room1);
        res2.setRoom(room2);
        res3.setRoom(room3);
        reservationDao.create(res);
        reservationDao.create(res2);
        reservationDao.create(res3);

        assertThat(reservationDao.findAll())
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(res, res2, res3);
    }

    @Test
    void delete() {
        RoomType rt1 = new RoomType(BedType.FULL, 20, 2);
        RoomType rt2 = new RoomType(BedType.QUEEN, 10, 1);
        Room room1 = new Room(1, rt1);
        Room room2 = new Room(2, rt2);
        roomDao.create(room1);
        roomDao.create(room2);
        Guest guest = new Guest("Ho Pep", "alik@seznam.cz", "Brno", "", "+420905174925");
        Guest guest2 = new Guest("Mic Nov", "rofl@centrum.lol", "Praha 123", "Jsem vegan nebo co", "123519681");
        Reservation res = new Reservation(LocalDate.of(2018, 4, 12), LocalDate.of(2018, 5, 7), 1, guest);
        Reservation res2 = new Reservation(LocalDate.of(2017, 2, 4), LocalDate.now(), 1, guest2);
        res.setRoom(room1);
        res2.setRoom(room2);
        reservationDao.create(res);
        reservationDao.create(res2);

        reservationDao.delete(res);

        assertThat(reservationDao.findAll())
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(res2);
    }

    @Test
    void deleteWithNullId() {
        Room room = new Room(1, new RoomType(BedType.FULL, 20, 2));
        Guest guest = new Guest("Ho Pep", "alik@seznam.cz", "Brno", "", "+420905174925");
        Reservation res = new Reservation(LocalDate.of(2018, 4, 12), LocalDate.of(2018, 5, 7), 1, guest);
        res.setRoom(room);

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> reservationDao.delete(res))
                .withMessage("Reservation has null ID: " + res);
    }

    @Test
    void deleteNonExisting() {
        Room room = new Room(1, new RoomType(BedType.FULL, 20, 2));
        roomDao.create(room);
        Guest guest = new Guest("Ho Pep", "alik@seznam.cz", "Brno", "", "+420905174925");
        Reservation res = new Reservation(LocalDate.of(2018, 4, 12), LocalDate.of(2018, 5, 7), 1, guest);
        res.setRoom(room);
        res.setId(123L);

        assertThatExceptionOfType(DataException.class)
                .isThrownBy(() -> reservationDao.delete(res))
                .withMessage("Failed to delete non-existing reservation: " + res);
    }

    @Test
    void update() {
        Room room1 = new Room(1, new RoomType(BedType.FULL, 20, 2));
        roomDao.create(room1);
        Guest guest = new Guest("Gu Est", "abc@gmail.com", "Brno", "", "123456789");
        Reservation res1 = new Reservation(LocalDate.of(2018, 4, 12), LocalDate.of(2018, 5, 7), 1, guest);
        res1.setRoom(room1);
        reservationDao.create(res1);

        Room room2 = new Room(2, new RoomType(BedType.QUEEN, 10, 1));
        roomDao.create(room2);
        res1.setRoom(room2);

        reservationDao.update(res1);

        assertThat(reservationDao.findAll())
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(res1);
    }

    @Test
    void updateWithNullId() {
        Room room = new Room(1, new RoomType(BedType.FULL, 20, 2));
        roomDao.create(room);
        Guest guest = new Guest("Ho Pep", "alik@seznam.cz", "Brno", "", "+420905174925");
        Reservation res = new Reservation(LocalDate.of(2018, 4, 12), LocalDate.of(2018, 5, 7), 1, guest);
        res.setRoom(room);

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> reservationDao.update(res))
                .withMessage("Reservation has null ID: " + res);
    }

    @Test
    void updateNonExisting() {
        Room room = new Room(1, new RoomType(BedType.FULL, 20, 2));
        roomDao.create(room);
        Guest guest = new Guest("Ho Pep", "alik@seznam.cz", "Brno", "", "+420905174925");
        Reservation res = new Reservation(LocalDate.of(2018, 4, 12), LocalDate.of(2018, 5, 7), 1, guest);
        res.setRoom(room);
        res.setId(123L);

        assertThatExceptionOfType(DataException.class)
                .isThrownBy(() -> reservationDao.update(res))
                .withMessage("Failed to update non-existing reservation: " + res);

    }

    @Test
    void filterReservationsByName() {
        RoomType rt1 = new RoomType(BedType.FULL, 20, 2);
        RoomType rt2 = new RoomType(BedType.QUEEN, 10, 1);
        Room room1 = new Room(1, rt1);
        Room room2 = new Room(2, rt2);
        roomDao.create(room1);
        roomDao.create(room2);
        Guest guest = new Guest("Prvni Jmeno", "alik@seznam.cz", "Brno", "", "+420905174925");
        Guest guest2 = new Guest("Druhe Jmeno", "rofl@centrum.lol", "Praha 123", "Jsem vegan nebo co", "123519681");
        Reservation res = new Reservation(LocalDate.of(2018, 4, 12), LocalDate.of(2018, 5, 7), 1, guest);
        Reservation res2 = new Reservation(LocalDate.of(2017, 2, 4), LocalDate.now(), 1, guest2);
        res.setRoom(room1);
        res2.setRoom(room2);
        reservationDao.create(res);
        reservationDao.create(res2);

        assertThat(reservationDao.filterReservations("Druhe", ""))
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(res2);
    }

    @Test
    void filterReservationsByInvalidName() {
        RoomType rt1 = new RoomType(BedType.FULL, 20, 2);
        RoomType rt2 = new RoomType(BedType.QUEEN, 10, 1);
        Room room1 = new Room(1, rt1);
        Room room2 = new Room(2, rt2);
        roomDao.create(room1);
        roomDao.create(room2);
        Guest guest = new Guest("Prvni Jmeno", "alik@seznam.cz", "Brno", "", "+420905174925");
        Guest guest2 = new Guest("Druhe Jmeno", "rofl@centrum.lol", "Praha 123", "Jsem vegan nebo co", "123519681");
        Reservation res = new Reservation(LocalDate.of(2018, 4, 12), LocalDate.of(2018, 5, 7), 1, guest);
        Reservation res2 = new Reservation(LocalDate.of(2017, 2, 4), LocalDate.now(), 1, guest2);
        res.setRoom(room1);
        res2.setRoom(room2);
        reservationDao.create(res);
        reservationDao.create(res2);

        assertThat(reservationDao.filterReservations("Michal", "")).isEmpty();
    }

    @Test
    void filterReservationsByNumber() {
        RoomType rt1 = new RoomType(BedType.FULL, 20, 2);
        RoomType rt2 = new RoomType(BedType.QUEEN, 10, 1);
        Room room1 = new Room(1, rt1);
        Room room2 = new Room(2, rt2);
        roomDao.create(room1);
        roomDao.create(room2);
        Guest guest = new Guest("Prvni Jmeno", "alik@seznam.cz", "Brno", "", "+420905174925");
        Guest guest2 = new Guest("Druhe Jmeno", "rofl@centrum.lol", "Praha 123", "Jsem vegan nebo co", "123519681");
        Reservation res = new Reservation(LocalDate.of(2018, 4, 12), LocalDate.of(2018, 5, 7), 1, guest);
        Reservation res2 = new Reservation(LocalDate.of(2017, 2, 4), LocalDate.now(), 1, guest2);
        res.setRoom(room1);
        res2.setRoom(room2);
        reservationDao.create(res);
        reservationDao.create(res2);

        assertThat(reservationDao.filterReservations("", "2"))
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(res2);
    }


    @Test
    void filterReservationsByInvalidNumber() {
        RoomType rt1 = new RoomType(BedType.FULL, 20, 2);
        RoomType rt2 = new RoomType(BedType.QUEEN, 10, 1);
        Room room1 = new Room(1, rt1);
        Room room2 = new Room(2, rt2);
        roomDao.create(room1);
        roomDao.create(room2);
        Guest guest = new Guest("Prvni Jmeno", "alik@seznam.cz", "Brno", "", "+420905174925");
        Guest guest2 = new Guest("Druhe Jmeno", "rofl@centrum.lol", "Praha 123", "Jsem vegan nebo co", "123519681");
        Reservation res = new Reservation(LocalDate.of(2018, 4, 12), LocalDate.of(2018, 5, 7), 1, guest);
        Reservation res2 = new Reservation(LocalDate.of(2017, 2, 4), LocalDate.now(), 1, guest2);
        res.setRoom(room1);
        res2.setRoom(room2);
        reservationDao.create(res);
        reservationDao.create(res2);

        assertThat(reservationDao.filterReservations("", "7")).isEmpty();
    }

    @Test
    void filterReservationsByNumberAndName() {
        RoomType rt1 = new RoomType(BedType.FULL, 20, 2);
        RoomType rt2 = new RoomType(BedType.QUEEN, 10, 1);
        Room room1 = new Room(1, rt1);
        Room room2 = new Room(2, rt2);
        roomDao.create(room1);
        roomDao.create(room2);
        Guest guest = new Guest("Prvni Jmeno", "alik@seznam.cz", "Brno", "", "+420905174925");
        Guest guest2 = new Guest("Druhe Jmeno", "rofl@centrum.lol", "Praha 123", "Jsem vegan nebo co", "123519681");
        Reservation res = new Reservation(LocalDate.of(2018, 4, 12), LocalDate.of(2018, 5, 7), 1, guest);
        Reservation res2 = new Reservation(LocalDate.of(2017, 2, 4), LocalDate.now(), 1, guest2);
        res.setRoom(room1);
        res2.setRoom(room2);
        reservationDao.create(res);
        reservationDao.create(res2);

        assertThat(reservationDao.filterReservations("Druhe", "2"))
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(res2);
    }

    @Test
    void filterReservationsEmpty() {
        assertThat(reservationDao.filterReservations("Michal", "7")).isEmpty();
    }
}