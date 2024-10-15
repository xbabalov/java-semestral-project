package ui;

import data.RoomDao;
import model.Room;
import model.RoomType;

import javax.swing.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class RoomTableModel extends AbstractEntityTableModel<Room> {

    private static final I18N I18N = new I18N(RoomTableModel.class);
    private List<Room> rooms = new ArrayList<>();
    private final RoomDao roomDao;

    private static final List<Column<?, Room>> COLUMNS = List.of(
            Column.readOnly(I18N.getString("roomNumber"), Integer.class, Room::getNumber),
            Column.readOnly(I18N.getString("roomType"), RoomType.class, Room::getType),
            Column.readOnly(I18N.getString("roomSize"), Integer.class, Room::getSize)
    );

    public RoomTableModel(RoomDao roomDao) {
        super(COLUMNS);
        this.roomDao = roomDao;
    }

    @Override
    protected Room getEntity(int rowIndex) {
        return rooms.get(rowIndex);
    }

    @Override
    protected void updateEntity(Room entity) {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                roomDao.update(entity);
                return null;
            }
            @Override
            protected void done(){
                try {
                    get();
                } catch (InterruptedException | ExecutionException e) {
                    JOptionPane.showMessageDialog(null, I18N.getString("updateFailed"));
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    @Override
    public int getRowCount() {
        return rooms.size();
    }

    public void showAvailableRooms(LocalDate in, LocalDate out, Integer additionalRoomNumberToShow) {
        SwingWorker<List<Room>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Room> doInBackground() {
                List<Room> found = roomDao.findAvailableRooms(in, out);
                if (additionalRoomNumberToShow != null) {
                    Room toAdd = roomDao.findByNumber(additionalRoomNumberToShow);
                    if (!found.contains(toAdd))
                        found.add(toAdd);
                    found.sort(Comparator.comparingInt(Room::getNumber));
                }
                return found;
            }

            @Override
            protected void done() {
                try {
                    rooms = get();
                    if(rooms.size() == 0){
                        JOptionPane.showMessageDialog(null, I18N.getString("showAvailableRoomsFailed"));
                    }
                } catch (InterruptedException | ExecutionException e) {
                    JOptionPane.showMessageDialog(null, I18N.getString("noRoomsAvailable"));
                    e.printStackTrace();
                }
                fireTableDataChanged();
            }
        };
        worker.execute();
    }

    public void hideAll() {
        rooms = new ArrayList<>();
        fireTableDataChanged();
    }

    public Room getSelectedRoom(int number){
        SwingWorker<Room, Void> worker = new SwingWorker<>() {
            @Override
            protected Room doInBackground() {
                return roomDao.findByNumber(number);
            }
        };
        worker.execute();
        try{
            return worker.get();
        }catch(InterruptedException | ExecutionException e){
            JOptionPane.showMessageDialog(null, I18N.getString("getSelectedRoomFailed"));
            e.printStackTrace();
        }
        return null;
    }

}
