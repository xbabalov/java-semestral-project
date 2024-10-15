package ui;

import data.ReservationDao;
import data.RoomDao;
import model.Reservation;
import model.Room;

import javax.swing.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ReservationTableModel extends AbstractEntityTableModel<Reservation> {

    private List<Reservation> reservations = new ArrayList<>();
    private final ReservationDao reservationDao;
    private final RoomDao roomDao;
    private static final I18N I18N = new I18N(ReservationTableModel.class);

    private static final List<Column<?, Reservation>> COLUMNS = List.of(
            Column.readOnly(I18N.getString("name"), String.class, Reservation::getGuestName),
            Column.readOnly(I18N.getString("expectedCheckIn"), LocalDate.class, Reservation::getExpectedCheckInDate),
            Column.readOnly(I18N.getString("expectedCheckOut"), LocalDate.class, Reservation::getExpectedCheckOutDate),
            Column.readOnly(I18N.getString("checkIn"), LocalDate.class, Reservation::getCheckInDate),
            Column.readOnly(I18N.getString("checkOut"), LocalDate.class, Reservation::getCheckOutDate),
            Column.readOnly(I18N.getString("roomNumber"), Room.class, Reservation::getRoom),
            Column.readOnly(I18N.getString("guestCount"), Integer.class, Reservation::getNumGuests),
            Column.readOnly(I18N.getString("email"), String.class, Reservation::getGuestEmail),
            Column.readOnly(I18N.getString("phone"), String.class, Reservation::getGuestPhone),
            Column.readOnly(I18N.getString("address"), String.class, Reservation::getGuestAddress),
            Column.readOnly(I18N.getString("details"), String.class, Reservation::getGuestDetails)
    );

    public ReservationTableModel(ReservationDao reservationDao, RoomDao roomDao) {
        super(COLUMNS);
        this.reservationDao = reservationDao;
        this.roomDao = roomDao;
        SwingWorker<List<Reservation>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Reservation> doInBackground() {
                return reservationDao.findAll();
            }

            @Override
            protected void done() {
                try {
                    reservations.addAll(get());
                } catch (InterruptedException | ExecutionException e) {
                    JOptionPane.showMessageDialog(null, I18N.getString("loadingFailed"));
                    e.printStackTrace();
                }
                fireTableDataChanged();
            }
        };
        worker.execute();
    }

    @Override
    public Reservation getEntity(int rowIndex) {
        return reservations.get(rowIndex);
    }

    @Override
    protected void updateEntity(Reservation entity) {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                reservationDao.update(entity);
                roomDao.update(entity.getRoom());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (InterruptedException | ExecutionException e) {
                    JOptionPane.showMessageDialog(null, I18N.getString("updatingFailed"));
                    e.printStackTrace();
                }
                int rowIndex = reservations.indexOf(entity);
                reservations.set(rowIndex, entity);
                fireTableRowsUpdated(rowIndex, rowIndex);
            }
        };
        worker.execute();
    }

    @Override
    public int getRowCount() {
        return reservations.size();
    }

    public void updateRow(Reservation reservation) {
        updateEntity(reservation);
    }

    public void deleteRow(int rowIndex) {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                reservationDao.delete(reservations.get(rowIndex));
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (InterruptedException | ExecutionException e) {
                    JOptionPane.showMessageDialog(null, I18N.getString("deletingFailed"));
                    e.printStackTrace();
                }
                reservations.remove(rowIndex);
                fireTableRowsDeleted(rowIndex, rowIndex);
            }
        };
        worker.execute();
    }

    public void addRow(Reservation reservation) {
        int newRowIndex = reservations.size();
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                reservationDao.create(reservation);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (InterruptedException | ExecutionException e) {
                    JOptionPane.showMessageDialog(null, I18N.getString("additionFailed"));
                    e.printStackTrace();
                }
                reservations.add(reservation);
                fireTableRowsInserted(newRowIndex, newRowIndex);
            }
        };
        worker.execute();
    }

    public void filterReservations(String name, String number) {
        SwingWorker<List<Reservation>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Reservation> doInBackground() {
                return reservationDao.filterReservations(name, number);
            }

            @Override
            protected void done() {
                try {
                    reservations = get();
                } catch (InterruptedException | ExecutionException e) {
                    JOptionPane.showMessageDialog(null, I18N.getString("sortingFailed"));
                    e.printStackTrace();
                }
                fireTableDataChanged();
            }
        };
        worker.execute();
    }

    public void showAll() {
        SwingWorker<List<Reservation>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Reservation> doInBackground() {
                return reservationDao.findAll();
            }

            @Override
            protected void done() {
                try {
                    reservations = get();
                } catch (InterruptedException | ExecutionException e) {
                    JOptionPane.showMessageDialog(null, I18N.getString("showAllFailed"));
                    e.printStackTrace();
                }
                fireTableDataChanged();
            }
        };
        worker.execute();
    }

}
