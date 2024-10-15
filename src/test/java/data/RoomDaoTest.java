package data;

import model.*;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

final class RoomDaoTest {

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
    void createRoomDao() throws SQLException {
        roomDao = new RoomDao(dataSource);
        roomDao.initTable();
        reservationDao = new ReservationDao(dataSource);
        reservationDao.initTable();
        try (var connection = dataSource.getConnection();
             var st = connection.createStatement()) {
            st.executeUpdate("DELETE FROM APP.ROOM");
        }
    }

    @AfterEach
    void cleanUp() {
        roomDao.getTableManager().dropTable();
    }

    @Test
    void createRoom() {
        var r = new Room(123, new RoomType(BedType.QUEEN, 10, 1));
        roomDao.create(r);

        assertNotNull(r.getId());
    }

    @Test
    void createRoomWithExistingId() {
        var r = new Room(123, new RoomType(BedType.QUEEN, 10, 1));
        r.setId(123L);

        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> roomDao.create(r))
                .withMessage("Room already has ID: " + r);
    }

    @Test
    void createRoomWithException() {
        var ex = new SQLException();
        RoomDao failingDao = createFailingDao(ex);

        var r1 = new Room(123, new RoomType(BedType.KING, 10, 1));
        assertThatExceptionOfType(DataException.class)
                .isThrownBy(() -> failingDao.create(r1))
                .withMessage("Failed to store room " + r1)
                .withCause(ex);
    }

    @Test
    void findAllEmpty() {
        assertThat(roomDao.findAll()).isEmpty();
    }

    @Test
    void findAll() {
        var r1 = new Room(123, new RoomType(BedType.KING, 10, 1));
        var r2 = new Room(456, new RoomType(BedType.FULL, 20, 1));
        var r3 = new Room(789, new RoomType(BedType.TWIN, 30, 1));

        roomDao.create(r1);
        roomDao.create(r2);
        roomDao.create(r3);

        assertThat(roomDao.findAll())
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(r1, r2, r3);
    }

    @Test
    void findAllWithException() {
        var ex = new SQLException();
        RoomDao failingDao = createFailingDao(ex);

        assertThatExceptionOfType(DataException.class)
                .isThrownBy(failingDao::findAll)
                .withMessage("Failed to load all rooms")
                .withCause(ex);
    }

    @Test
    void delete() {
        var r1 = new Room(123, new RoomType(BedType.KING, 10, 1));
        var r2 = new Room(456, new RoomType(BedType.FULL, 20, 1));

        roomDao.create(r1);
        roomDao.create(r2);

        roomDao.delete(r1);

        assertThat(roomDao.findAll())
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(r2);
    }

    @Test
    void deleteWithNullId() {
        var r1 = new Room(123, new RoomType(BedType.KING, 10, 1));

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> roomDao.delete(r1))
                .withMessage("Room has null ID: " + r1);
    }

    @Test
    void deleteNonExisting() {
        var r1 = new Room(123, new RoomType(BedType.KING, 10, 1));
        r1.setId(123L);

        assertThatExceptionOfType(DataException.class)
                .isThrownBy(() -> roomDao.delete(r1))
                .withMessage("Failed to delete non-existing room: " + r1);
    }

    @Test
    void deleteWithException() {
        var ex = new SQLException();
        RoomDao failingDao = createFailingDao(ex);

        var r1 = new Room(123, new RoomType(BedType.KING, 10, 1));
        r1.setId(123L);

        assertThatExceptionOfType(DataException.class)
                .isThrownBy(() -> failingDao.delete(r1))
                .withMessage("Failed to delete room " + r1)
                .withCause(ex);
    }

    @Test
    void updateWithNullId() {
        var r1 = new Room(123, new RoomType(BedType.KING, 10, 1));

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> roomDao.delete(r1))
                .withMessage("Room has null ID: " + r1);
    }

    @Test
    void updateNonExisting() {
        var r1 = new Room(123, new RoomType(BedType.KING, 10, 1));
        r1.setId(123L);

        assertThatExceptionOfType(DataException.class)
                .isThrownBy(() -> roomDao.update(r1))
                .withMessage("Failed to update non-existing room: " + r1);
    }

    @Test
    void updateWithException() {
        var ex = new SQLException();
        RoomDao failingDao = createFailingDao(ex);

        var r1 = new Room(123, new RoomType(BedType.KING, 10, 1));
        r1.setId(123L);

        assertThatExceptionOfType(DataException.class)
                .isThrownBy(() -> failingDao.update(r1))
                .withMessage("Failed to update room: " + r1)
                .withCause(ex);
    }

    @Test
    void findByNumber() {
        var r1 = new Room(123, new RoomType(BedType.KING, 10, 1));
        var r2 = new Room(456, new RoomType(BedType.FULL, 20, 1));

        roomDao.create(r1);
        roomDao.create(r2);

        assertThat(roomDao.findByNumber(456)).isEqualToComparingFieldByFieldRecursively(r2);
    }

    @Test
    void findByNumberNonExistent() {
        var r1 = new Room(123, new RoomType(BedType.KING, 10, 1));
        roomDao.create(r1);

        assertNull(roomDao.findByNumber(420));
    }

    @Test
    void findByNumberWithException() {
        var ex = new SQLException();
        RoomDao failingDao = createFailingDao(ex);

        var r1 = new Room(123, new RoomType(BedType.KING, 10, 1));

        assertThatExceptionOfType(DataException.class)
                .isThrownBy(() -> failingDao.findByNumber(123))
                .withMessage("Failed to load room number: " + r1.getNumber())
                .withCause(ex);
    }

    @Test
    void findAvailableRooms() {
        RoomType rt1 = new RoomType(BedType.FULL, 20, 2);
        RoomType rt2 = new RoomType(BedType.QUEEN, 10, 1);
        Room room1 = new Room(1, rt1);
        Room room2 = new Room(2, rt2);
        roomDao.create(room1);
        roomDao.create(room2);
        Guest guest = new Guest("Prvni Jmeno", "alik@seznam.cz", "Brno", "", "+420905174925");
        Guest guest2 = new Guest("Druhe Jmeno", "rofl@centrum.lol", "Praha 123", "Jsem vegan nebo co", "123519681");
        Reservation res = new Reservation(LocalDate.of(2020, 8, 12), LocalDate.of(2020, 8, 20), 1, guest);
        Reservation res2 = new Reservation(LocalDate.of(2020, 8, 8), LocalDate.of(2020, 8, 15), 1, guest2);
        res.setRoom(room1);
        res2.setRoom(room2);
        reservationDao.create(res);
        reservationDao.create(res2);

        assertThat(roomDao.findAvailableRooms(LocalDate.of(2020, 8, 16), LocalDate.of(2020, 8, 28)))
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(room2);
    }

    @Test
    void findAvailableRoomsEmpty() {
        RoomType rt1 = new RoomType(BedType.FULL, 20, 2);
        RoomType rt2 = new RoomType(BedType.QUEEN, 10, 1);
        Room room1 = new Room(1, rt1);
        Room room2 = new Room(2, rt2);
        roomDao.create(room1);
        roomDao.create(room2);
        Guest guest = new Guest("Prvni Jmeno", "alik@seznam.cz", "Brno", "", "+420905174925");
        Guest guest2 = new Guest("Druhe Jmeno", "rofl@centrum.lol", "Praha 123", "Jsem vegan nebo co", "123519681");
        Reservation res = new Reservation(LocalDate.of(2020, 8, 12), LocalDate.of(2020, 8, 20), 1, guest);
        Reservation res2 = new Reservation(LocalDate.of(2020, 8, 8), LocalDate.of(2020, 8, 15), 1, guest2);
        res.setRoom(room1);
        res2.setRoom(room2);
        reservationDao.create(res);
        reservationDao.create(res2);

        assertThat(roomDao.findAvailableRooms(LocalDate.of(2020, 8, 9), LocalDate.of(2020, 8, 14))).isEmpty();
    }

    private RoomDao createFailingDao(Throwable exceptionToBeThrown) {
        try {
            var dataSource = mock(DataSource.class);
            when(dataSource.getConnection()).thenAnswer(i -> RoomDaoTest.dataSource.getConnection());
            var roomDao = new RoomDao(dataSource);
            when(dataSource.getConnection()).thenThrow(exceptionToBeThrown);
            return roomDao;
        } catch (SQLException ex) {
            throw new RuntimeException("Mock configuration failed", ex);
        }
    }
}