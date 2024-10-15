package data;

import model.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class TestDataGenerator {

    private final RoomDao roomDao;
    private final ReservationDao reservationDao;

    public TestDataGenerator(RoomDao roomDao, ReservationDao reservationDao) {
        this.roomDao = roomDao;
        this.reservationDao = reservationDao;
    }

    public void createTestData() {
        if(roomDao.findAll().size() == 0){
            List<RoomType> types = new ArrayList<>(Arrays.asList(
                    new RoomType(BedType.QUEEN, 18, 2),
                    new RoomType(BedType.TWINXL, 10, 1),
                    new RoomType(BedType.FULL, 20, 5),
                    new RoomType(BedType.KING, 25, 2),
                    new RoomType(BedType.TWIN, 12, 1),
                    new RoomType(BedType.FULL, 20, 4)));

            for (int x = 1; x < 6; x++) {
                Room room = new Room(x, types.get(ThreadLocalRandom.current().nextInt(0, types.size())));
                roomDao.create(room);
            }

            Guest guest = new Guest("Honza Pepega", "alik@seznam.cz", "Brno", "no details", "+420905174925");
            Guest guest2 = new Guest("Michal Nový", "rofl@centrum.lol", "Praha 123", "Jsem vegan nebo co", "123519681");

            Reservation res = new Reservation(LocalDate.of(2021, 1, 12), LocalDate.of(2021, 1, 20), 1, guest);
            res.setCheckInDate(LocalDate.of(2021, 1, 12));
            Reservation res2 = new Reservation(LocalDate.of(2021, 2, 1), LocalDate.of(2021, 2, 20), 2, guest2);
            Reservation res3 = new Reservation(LocalDate.of(2020, 12, 25), LocalDate.of(2021, 1, 1), 2, guest2);
            res3.setCheckInDate(LocalDate.of(2020, 12, 25));
            res3.setCheckOutDate(LocalDate.of(2021, 1, 1));

            Room room1 = roomDao.findByNumber(1);
            Room room5 = roomDao.findByNumber(5);
            res.setRoom(room1);
            res2.setRoom(room5);
            res3.setRoom(room5);
            reservationDao.create(res);
            reservationDao.create(res2);
            reservationDao.create(res3);
        }
    }

}
