package data;

import model.BedType;
import model.Room;
import model.RoomType;

import javax.sql.DataSource;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class RoomDao {

    private final DataSource dataSource;
    private final TableManager tableManager = new TableManager();

    public RoomDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void initTable(){
        tableManager.initTable();
    }

    public void create(Room room) {
        if (room.getId() != null) {
            throw new IllegalArgumentException("Room already has ID: " + room);
        }
        try (var connection = dataSource.getConnection();
             var st = connection.prepareStatement(
                     "INSERT INTO ROOM (ROOM_NUMBER, PRICE, BEDS_AMOUNT, BED_TYPES) VALUES (?, ?, ?, ?)",
                     RETURN_GENERATED_KEYS)) {

            st.setLong(1, room.getNumber());
            st.setDouble(2, room.getType().getPrice());
            st.setInt(3, room.getType().getNumberOfBeds());
            st.setString(4, room.getType().getBedType().name());
            st.executeUpdate();

            try (var rs = st.getGeneratedKeys()) {
                if (rs.next()) {
                    room.setId(rs.getLong(1));
                } else {
                    throw new DataException("Failed to fetch generated key: no key returned for room: " + room);
                }
            }
        } catch (SQLException e) {
            throw new DataException("Failed to store room " + room, e);
        }
    }

    private void createTable() {
        try (var connection = dataSource.getConnection();
             var st = connection.createStatement()) {

            st.executeUpdate("CREATE TABLE APP.ROOM (" +
                    "ID BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY," +
                    "ROOM_NUMBER INT NOT NULL," +
                    "PRICE INT NOT NULL," +
                    "BEDS_AMOUNT INT NOT NULL," +
                    "BED_TYPES VARCHAR(150) NOT NULL" +
                    ")");
        } catch (SQLException e) {
            throw new DataException("Failed to create ROOMS table", e);
        }
    }

    public void delete(Room room) {
        if (room.getId() == null) {
            throw new IllegalArgumentException("Room has null ID: " + room);
        }
        try (var connection = dataSource.getConnection();
             var st = connection.prepareStatement("DELETE FROM ROOM WHERE ID = ?")) {
            st.setLong(1, room.getId());
            if (st.executeUpdate() == 0) {
                throw new DataException("Failed to delete non-existing room: " + room);
            }
        } catch (SQLException x) {
            throw new DataException("Failed to delete room " + room, x);
        }
    }

    public List<Room> findAll() {
        try (var connection = dataSource.getConnection();
             var st = connection.prepareStatement(
                     "SELECT ID, ROOM_NUMBER, PRICE, BEDS_AMOUNT, BED_TYPES" +
                             " FROM ROOM")) {

            List<Room> rooms = new ArrayList<>();
            try (var rs = st.executeQuery()) {
                while (rs.next()) {
                    RoomType roomType = new RoomType(
                            BedType.valueOf(rs.getString("BED_TYPES")),
                            rs.getInt("PRICE"),
                            rs.getInt("BEDS_AMOUNT")
                    );
                    Room room = new Room(
                            rs.getInt("ROOM_NUMBER"),
                            roomType
                    );
                    room.setId(rs.getLong("ID"));
                    rooms.add(room);
                }
            }
            return rooms;
        } catch (SQLException e) {
            throw new DataException("Failed to load all rooms", e);
        }
    }

    public List<Room> findAvailableRooms(LocalDate in, LocalDate out) {
        try (var connection = dataSource.getConnection();
             var st = connection.prepareStatement(
                     "SELECT * FROM APP.ROOM WHERE ID NOT IN " +
                             "(SELECT ROOM_ID FROM APP.RESERVATION WHERE RESERVATION.EXPECTED_CHECK_IN_DATE < ?" +
                             " AND RESERVATION.EXPECTED_CHECK_OUT_DATE > ?" +
                             " AND RESERVATION.CHECK_OUT_DATE IS NULL)")) {
            st.setDate(1, Date.valueOf(out));
            st.setDate(2, Date.valueOf(in));
            List<Room> rooms = new ArrayList<>();
            try (var rs = st.executeQuery()) {
                while (rs.next()) {
                    RoomType roomType = new RoomType(
                            BedType.valueOf(rs.getString("BED_TYPES")),
                            rs.getInt("PRICE"),
                            rs.getInt("BEDS_AMOUNT")
                    );
                    Room room = new Room(
                            rs.getInt("ROOM_NUMBER"),
                            roomType
                    );
                    room.setId(rs.getLong("ID"));
                    rooms.add(room);
                }
            } catch (SQLException x) {
                throw new DataException("Can't check available rooms.", x);
            }
            return rooms;
        } catch (SQLException e) {
            throw new DataException("Can't check available rooms.", e);
        }
    }

    public Room findByNumber(int number) {
        try (var connection = dataSource.getConnection();
             var st = connection.prepareStatement("SELECT ID, ROOM_NUMBER, PRICE, BEDS_AMOUNT, BED_TYPES FROM ROOM WHERE ROOM_NUMBER = ?")) {
            st.setInt(1, number);
            try (var rs = st.executeQuery()) {
                if (rs.next()) {
                    var room = new Room(
                            rs.getInt("ROOM_NUMBER"),
                            new RoomType(BedType.valueOf(rs.getString("BED_TYPES")), rs.getInt("PRICE"), rs.getInt("BEDS_AMOUNT")));
                    room.setId(rs.getLong("ID"));
                    return room;
                }
                return null;
            }
        } catch (SQLException ex) {
            throw new DataException("Failed to load room number: " + number, ex);
        }
    }

    public void update(Room room) {
        if (room.getId() == null) {
            throw new IllegalArgumentException("Room has null ID");
        }
        try (var connection = dataSource.getConnection();
             var st = connection.prepareStatement("UPDATE ROOM SET ROOM_NUMBER = ?, PRICE = ?," +
                     " BEDS_AMOUNT = ?, BED_TYPES = ? WHERE ID = ?")) {

            st.setLong(1, room.getNumber());
            st.setDouble(2, room.getType().getPrice());
            st.setInt(3, room.getType().getNumberOfBeds());
            st.setString(4, room.getType().getBedType().name());
            st.setLong(5, room.getId());
            st.executeUpdate();

            if (st.executeUpdate() == 0) {
                throw new DataException("Failed to update non-existing room: " + room);
            }
        } catch (SQLException x) {
            throw new DataException("Failed to update room: " + room, x);
        }
    }

    public class TableManager {

        public void initTable() {
            if (!tableExits("APP", "ROOM")) {
                createTable();
            }
        }

        private boolean tableExits(String schema, String table) {
            try (var connection = dataSource.getConnection();
                 var rs = connection.getMetaData().getTables(null, schema, table, null)) {
                return rs.next();
            } catch (SQLException ex) {
                throw new DataException("Failed to detect if the table " + schema + "." + table + " exists", ex);
            }
        }

        public void dropTable() {
            try (var connection = dataSource.getConnection();
                 var st = connection.createStatement()) {

                st.executeUpdate("DROP TABLE APP.ROOM");
            } catch (SQLException ex) {
                throw new DataException("Failed to drop ROOMS table", ex);
            }
        }
    }

    public TableManager getTableManager() {
        return tableManager;
    }
}
