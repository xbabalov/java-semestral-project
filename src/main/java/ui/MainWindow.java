package ui;

import data.ReservationDao;
import data.RoomDao;

import javax.swing.*;
import java.awt.*;

public class MainWindow {

    private final JFrame frame;
    private final NewReservationTab newReservationTab;
    private final AllReservationTab allReservationTab;
    private static final I18N I18N = new I18N(MainWindow.class);

    public MainWindow(RoomDao roomDao, ReservationDao reservationDao) {
        var reservationTableModel = new ReservationTableModel(reservationDao, roomDao);
        var roomTableModel = new RoomTableModel(roomDao);
        this.newReservationTab = new NewReservationTab(reservationTableModel, roomTableModel);
        this.allReservationTab = new AllReservationTab(reservationTableModel, newReservationTab);
        frame = createFrame();
        var tabbedPane = createTabbedPane();
        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedIndex() == 0) {
                if (newReservationTab.getToEdit() != null)
                    newReservationTab.editReservation();
            }
        });
        frame.add(tabbedPane);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setBackground(new Color(255, 230, 230));
        ImageIcon img = new ImageIcon(MainWindow.class.getResource("all.png"));
        frame.setIconImage(img.getImage());
    }

    private JFrame createFrame() {
        var frame = new JFrame(I18N.getString("title"));
        frame.setPreferredSize(new Dimension(1280, 720));
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        return frame;
    }

    public void show() {
        frame.setVisible(true);
    }

    public JTabbedPane createTabbedPane() {
        var tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(new Color(255, 179, 179));

        newReservationTab.createPanel();
        tabbedPane.add(I18N.getString("newReservationTab"), newReservationTab.getMainPanel());
        tabbedPane.setIconAt(0, resizeIcon("add.png", 32, 29));

        allReservationTab.createPanel();
        tabbedPane.add(I18N.getString("findReservation"), allReservationTab.getMainPanel());
        tabbedPane.setIconAt(1, resizeIcon("find.png", 31, 27));

        return tabbedPane;
    }

    public ImageIcon resizeIcon(String path, int width, int height) {
        ImageIcon icon = new ImageIcon(MainWindow.class.getResource(path));
        Image image = icon.getImage();
        icon = new ImageIcon(image.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH));
        return icon;
    }

}
