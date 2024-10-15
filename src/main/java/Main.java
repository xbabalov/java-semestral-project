import data.ReservationDao;
import data.RoomDao;
import data.TestDataGenerator;
import org.apache.derby.jdbc.EmbeddedDataSource;
import ui.MainWindow;

import javax.sql.DataSource;
import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    public static void main(String[] args) {
        var dataSource = createDataSource();
        var roomDao = new RoomDao(dataSource);
        roomDao.initTable();
        var reservationDao = new ReservationDao(dataSource);
        reservationDao.initTable();
        initNimbusLookAndFeel();
        TestDataGenerator generator = new TestDataGenerator(roomDao, reservationDao);
        generator.createTestData();
        EventQueue.invokeLater(() -> new MainWindow(roomDao, reservationDao).show());
    }

    private static DataSource createDataSource() {
        String dbPath = System.getProperty("user.home") + "/hotel-reservation";
        EmbeddedDataSource dataSource = new EmbeddedDataSource();
        dataSource.setDatabaseName(dbPath);
        dataSource.setCreateDatabase("create");
        return dataSource;
    }

    private static void initNimbusLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Nimbus layout initialization failed", ex);
        }
    }

}
