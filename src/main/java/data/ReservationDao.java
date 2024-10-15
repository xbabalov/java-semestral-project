package data;

import model.*;

import javax.sql.DataSource;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class ReservationDao {

    private final DataSource dataSource;
    private final TableManager tableManager = new TableManager();

    public ReservationDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void initTable(){
        tableManager.initTable();
    }


    public void create(Reservation reservation) {
        if (reservation.getId() != null) {
            throw new IllegalArgumentException("Reservation already has ID: " + reservation);
        }
        try (var connection = dataSource.getConnection();
             var st = connection.prepareStatement(
                     "INSERT INTO RESERVATION (GUEST_NAME, EMAIL, ADDRESS, PHONE, DETAILS, EXPECTED_CHECK_IN_DATE, EXPECTED_CHECK_OUT_DATE, CHECK_IN_DATE, CHECK_OUT_DATE, GUESTS_NUMBER, ROOM_ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                     RETURN_GENERATED_KEYS)) {

            st.setString(1, reservation.getGuest().getName());
            st.setString(2, reservation.getGuest().getEmail());
            st.setString(3, reservation.getGuest().getAddress());
            st.setString(4, reservation.getGuest().getPhone());
            st.setString(5, reservation.getGuest().getDetails());
            st.setDate(6, Date.valueOf(reservation.getExpectedCheckInDate()));
            st.setDate(7, Date.valueOf(reservation.getExpectedCheckOutDate()));
            if (reservation.getCheckInDate() == null) {
                st.setNull(8, Types.DATE);
            } else {
                st.setDate(8, Date.valueOf(reservation.getCheckInDate()));
            }
            if (reservation.getCheckOutDate() == null) {
                st.setNull(9, Types.DATE);
            } else {
                st.setDate(9, Date.valueOf(reservation.getCheckOutDate()));
            }
            st.setInt(10, reservation.getNumGuests());
            st.setLong(11, reservation.getRoom().getId());
            st.executeUpdate();

            try (var rs = st.getGeneratedKeys()) {
                if (rs.next()) {
                    reservation.setId(rs.getLong(1));
                } else {
                    throw new DataException("Failed to fetch generated key: no key returned for reservation: " + reservation);
                }
            }
        } catch (SQLException e) {
            throw new DataException("Failed to store reservation: " + reservation, e);
        }
    }

    private void createTable() {
        try (var connection = dataSource.getConnection();
             var st = connection.createStatement()) {

            st.executeUpdate("CREATE TABLE APP.RESERVATION (" +
                    "ID BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY," +
                    "GUEST_NAME VARCHAR(100) NOT NULL," +
                    "EMAIL VARCHAR(100) NOT NULL," +
                    "ADDRESS VARCHAR(100) NOT NULL," +
                    "PHONE VARCHAR(20) NOT NULL," +
                    "DETAILS VARCHAR(1000) NOT NULL," +
                    "EXPECTED_CHECK_IN_DATE DATE NOT NULL," +
                    "EXPECTED_CHECK_OUT_DATE DATE NOT NULL," +
                    "CHECK_IN_DATE DATE," +
                    "CHECK_OUT_DATE DATE," +
                    "GUESTS_NUMBER INT NOT NULL," +
                    "ROOM_ID INT NOT NULL" +
                    ")");
        } catch (SQLException e) {
            throw new DataException("Failed to create RESERVATION table", e);
        }
    }

    public void delete(Reservation reservation) {
        if (reservation.getId() == null) {
            throw new IllegalArgumentException("Reservation has null ID: " + reservation);
        }
        try (var connection = dataSource.getConnection();
             var st = connection.prepareStatement("DELETE FROM RESERVATION WHERE ID = ?")) {
            st.setLong(1, reservation.getId());
            if (st.executeUpdate() == 0) {
                throw new DataException("Failed to delete non-existing reservation: " + reservation);
            }
        } catch (SQLException x) {
            throw new DataException("Failed to delete reservation " + reservation, x);
        }
    }

    public List<Reservation> findAll() {
        try (var connection = dataSource.getConnection();
             var st = connection.prepareStatement(
                     "SELECT RESERVATION.ID, GUEST_NAME, EMAIL, ADDRESS, PHONE, DETAILS, EXPECTED_CHECK_IN_DATE, EXPECTED_CHECK_OUT_DATE, CHECK_IN_DATE, CHECK_OUT_DATE, GUESTS_NUMBER, ROOM_ID" +
                             ", \"ROOM_NUMBER\", \"PRICE\", \"BEDS_AMOUNT\", \"BED_TYPES\" FROM RESERVATION LEFT OUTER JOIN ROOM ON ROOM.ID = RESERVATION.ROOM_ID")) {

            List<Reservation> reservations = new ArrayList<>();
            try (var rs = st.executeQuery()) {
                while (rs.next()) {
                    Guest guest = new Guest(
                            rs.getString("GUEST_NAME"),
                            rs.getString("EMAIL"),
                            rs.getString("ADDRESS"),
                            rs.getString("DETAILS"),
                            rs.getString("PHONE")
                    );
                    Reservation reservation = new Reservation(
                            rs.getDate("EXPECTED_CHECK_IN_DATE").toLocalDate(),
                            rs.getDate("EXPECTED_CHECK_OUT_DATE").toLocalDate(),
                            rs.getInt("GUESTS_NUMBER"),
                            guest
                    );

                    var roomId = rs.getLong("ROOM_ID");
                    if (!rs.wasNull()) {
                        RoomType roomType = new RoomType(
                                BedType.valueOf(rs.getString("BED_TYPES")),
                                rs.getInt("PRICE"),
                                rs.getInt("BEDS_AMOUNT")
                        );
                        var room = new Room(rs.getInt("ROOM_NUMBER"),
                                roomType);
                        room.setId(roomId);
                        reservation.setRoom(room);
                    }
                    var date = rs.getDate("CHECK_IN_DATE");
                    if (!rs.wasNull()) {
                        reservation.setCheckInDate(date.toLocalDate());
                    } else {
                        reservation.setCheckInDate(null);
                    }
                    date = rs.getDate("CHECK_OUT_DATE");
                    if (!rs.wasNull()) {
                        reservation.setCheckOutDate(date.toLocalDate());
                    } else {
                        reservation.setCheckOutDate(null);
                    }

                    reservation.setId(rs.getLong("ID"));
                    reservations.add(reservation);
                }
            }
            return reservations;
        } catch (SQLException e) {
            throw new DataException("Failed to load all reservations", e);
        }
    }

    public List<Reservation> filterReservations(String name, String number) {
        try (var connection = dataSource.getConnection();
             var st = connection.prepareStatement((!name.isBlank() && !number.isBlank()) ?
                     ("SELECT * FROM APP.RESERVATION LEFT OUTER JOIN ROOM ON ROOM.ID = RESERVATION.ROOM_ID WHERE LOWER(GUEST_NAME) LIKE ? " + "AND ROOM_NUMBER = ?") :
                     ("SELECT * FROM APP.RESERVATION LEFT OUTER JOIN ROOM ON ROOM.ID = RESERVATION.ROOM_ID WHERE LOWER(GUEST_NAME) LIKE ? " + "OR ROOM_NUMBER = ?")
             )) {
            if (name.isBlank()) {
                st.setNull(1, Types.VARCHAR);
            } else {
                st.setString(1, "%" + name.toLowerCase() + "%");
            }
            if (number.isBlank()) {
                st.setNull(2, Types.INTEGER);
            } else {
                st.setInt(2, Integer.parseInt(number));
            }
            List<Reservation> reservations = new ArrayList<>();
            try (var rs = st.executeQuery()) {
                while (rs.next()) {
                    Guest guest = new Guest(
                            rs.getString("GUEST_NAME"),
                            rs.getString("EMAIL"),
                            rs.getString("ADDRESS"),
                            rs.getString("DETAILS"),
                            rs.getString("PHONE")
                    );
                    Reservation reservation = new Reservation(
                            rs.getDate("EXPECTED_CHECK_IN_DATE").toLocalDate(),
                            rs.getDate("EXPECTED_CHECK_OUT_DATE").toLocalDate(),
                            rs.getInt("GUESTS_NUMBER"),
                            guest
                    );

                    var roomId = rs.getLong("ROOM_ID");
                    if (!rs.wasNull()) {
                        RoomType roomType = new RoomType(
                                BedType.valueOf(rs.getString("BED_TYPES")),
                                rs.getInt("PRICE"),
                                rs.getInt("BEDS_AMOUNT")
                        );
                        var room = new Room(rs.getInt("ROOM_NUMBER"),
                                roomType);
                        room.setId(roomId);
                        reservation.setRoom(room);
                    }
                    var date = rs.getDate("CHECK_IN_DATE");
                    if (!rs.wasNull()) {
                        reservation.setCheckInDate(date.toLocalDate());
                    } else {
                        reservation.setCheckInDate(null);
                    }
                    date = rs.getDate("CHECK_OUT_DATE");
                    if (!rs.wasNull()) {
                        reservation.setCheckOutDate(date.toLocalDate());
                    } else {
                        reservation.setCheckOutDate(null);
                    }

                    reservation.setId(rs.getLong("ID"));
                    reservations.add(reservation);
                }
                return reservations;
            } catch (SQLException x) {
                throw new DataException("Can't sort reservations", x);
            }
        } catch (SQLException e) {
            throw new DataException("Can't sort reservations", e);
        }
    }

    public void update(Reservation reservation) {
        if (reservation.getId() == null) {
            throw new IllegalArgumentException("Reservation has null ID: " + reservation);
        }
        try (var connection = dataSource.getConnection();
             var st = connection.prepareStatement("UPDATE RESERVATION SET GUEST_NAME = ?, EMAIL = ?," +
                     " ADDRESS = ?, PHONE = ?, DETAILS = ?, EXPECTED_CHECK_IN_DATE = ?, EXPECTED_CHECK_OUT_DATE = ?, CHECK_IN_DATE = ?, CHECK_OUT_DATE = ?" +
                     ", GUESTS_NUMBER = ?, ROOM_ID = ? WHERE ID = ?")) {

            st.setString(1, reservation.getGuest().getName());
            st.setString(2, reservation.getGuest().getEmail());
            st.setString(3, reservation.getGuest().getAddress());
            st.setString(4, reservation.getGuest().getPhone());
            st.setString(5, reservation.getGuest().getDetails());
            st.setDate(6, Date.valueOf(reservation.getExpectedCheckInDate()));
            st.setDate(7, Date.valueOf(reservation.getExpectedCheckOutDate()));
            if (reservation.getCheckInDate() != null) {
                st.setDate(8, Date.valueOf(reservation.getCheckInDate()));
            } else {
                st.setNull(8, Types.DATE);
            }
            if (reservation.getCheckOutDate() != null) {
                st.setDate(9, Date.valueOf(reservation.getCheckOutDate()));
            } else {
                st.setNull(9, Types.DATE);
            }
            st.setInt(10, reservation.getNumGuests());
            st.setLong(11, reservation.getRoom().getId());
            st.setLong(12, reservation.getId());
            st.executeUpdate();

            if (st.executeUpdate() == 0) {
                throw new DataException("Failed to update non-existing reservation: " + reservation);
            }
        } catch (SQLException x) {
            throw new DataException("Failed to store reservation: " + reservation, x);
        }
    }

    public class TableManager {

        private void initTable() {
            if (!tableExits("APP", "RESERVATION")) {
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

                st.executeUpdate("DROP TABLE APP.RESERVATION");
            } catch (SQLException ex) {
                throw new DataException("Failed to drop RESERVATION table", ex);
            }
        }

    }

    public TableManager getTableManager() {
        return tableManager;
    }
}