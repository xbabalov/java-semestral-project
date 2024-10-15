package ui;

import model.Reservation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.time.temporal.ChronoUnit.DAYS;

public class AllReservationTab {

    private final ReservationTableModel reservationTableModel;

    private JPanel mainPanel, controlPanel, tablePanel;
    private final JTextField guestNameTextField = new JTextField(0);
    private final JTextField roomNumTextField = new JTextField(0);
    private JTable rTable = new JTable();
    private final JLabel endLabel = new JLabel("");
    private final ReservationEditor reservationEditor;

    private static final I18N I18N = new I18N(AllReservationTab.class);

    public AllReservationTab(ReservationTableModel reservationTableModel, ReservationEditor reservationEditor) {
        this.reservationTableModel = reservationTableModel;
        this.reservationEditor = reservationEditor;
    }

    public void createPanel() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        CustomizationTools.customizePanel(mainPanel, false);
        fillMainPanel();
    }

    public void fillMainPanel() {
        createAdditionalPanels();
        fillTablePanel();
        fillControlPanel();
        assignPanels();
    }

    public void addSearchField(String key, JTextField field) {
        JLabel name = new JLabel(I18N.getString(key));
        CustomizationTools.customizeLabelBold(name);
        controlPanel.add(name, constrains(0, new Insets(10, 10, -10, 10), 1, 1));
        controlPanel.add(field, constrains(1, new Insets(10, 10, 10, 10), 1, 1));
    }

    public JButton addButton(String key, ActionListener listener, int grid) {
        JButton button = new JButton(I18N.getString(key));
        CustomizationTools.customizeButton(button);
        controlPanel.add(button, constrains(grid, new Insets(10, 10, 10, 10), 1, 1));
        button.addActionListener(listener);
        return button;
    }

    public void fillControlPanel() {
        var defaultButtonSize = new Dimension(10, 28);

        addSearchField("guestName", guestNameTextField);
        addSearchField("roomNumber", roomNumTextField);

        addButton("search", this::searchForReservations, 1);
        addButton("showAll", this::showAllReservations, 1);

        addButton("checkIn", this::checkInReservation, 2)
                .setPreferredSize(defaultButtonSize);
        addButton("checkOut", this::checkOutReservation, 2)
                .setPreferredSize(defaultButtonSize);
        addButton("editReservation", this::editReservation, 2)
                .setPreferredSize(defaultButtonSize);
        addButton("deleteReservation", this::deleteReservation, 2)
                .setPreferredSize(defaultButtonSize);

        GridBagConstraints a = constrains(3, new Insets(0, 10, 10, 10), 1, 1);
        a.gridwidth = 4;
        controlPanel.add(endLabel, a);
        CustomizationTools.customizeLabelBold(endLabel);
    }

    public void fillTablePanel() {
        rTable = createTable();
        tablePanel.add(new JScrollPane(rTable), constrains(2, new Insets(10, 10, 10, 10), 1, 1));
    }

    public void createAdditionalPanels() {
        controlPanel = new JPanel(new GridBagLayout());
        tablePanel = new JPanel(new GridBagLayout());
        CustomizationTools.customizePanel(controlPanel, true);
        CustomizationTools.customizePanel(tablePanel, false);
    }

    public void assignPanels() {
        mainPanel.add(controlPanel, constrains(0, new Insets(20, 10, 0, 10), 0, 0.5));
        mainPanel.add(tablePanel, constrains(1, new Insets(10, 10, 0, 10), 1, 1));
    }

    public JTable createTable() {
        var table = new JTable(reservationTableModel);

        CustomizationTools.customizeTable(table);
        table.setRowHeight(30);

        table.getTableHeader().setReorderingAllowed(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        ReservationTableCellRenderer renderer = new ReservationTableCellRenderer();
        table.setDefaultRenderer(Object.class, renderer);
        table.setDefaultRenderer(Integer.class, renderer);

        return table;
    }

    public void searchForReservations(ActionEvent actionEvent) {
        String guestName = guestNameTextField.getText();
        String roomNumber = roomNumTextField.getText();
        editEndLabel("");

        if ((guestName == null || roomNumber == null) || (guestName.trim().length() <= 0 && roomNumber.trim().length() <= 0)) {
            editEndLabel("invalidArguments");
        } else {
            reservationTableModel.filterReservations(guestName, roomNumber);
        }
    }

    public void showAllReservations(ActionEvent actionEvent){
        reservationTableModel.showAll();
        clearTextFields();
    }

    public void editReservation(ActionEvent actionEvent){
        int[] selectedRows = rTable.getSelectedRows();
        if (selectedRows.length != 1) {
            editEndLabel("noReservationSelected");
        } else {
            ReservationTableModel model = (ReservationTableModel) rTable.getModel();
            Reservation toEdit = model.getEntity(selectedRows[0]);
            if (toEdit.getCheckOutDate() != null) {
               editEndLabel("alreadyCheckOut");
            } else {
                reservationEditor.edit(toEdit);
            }
        }
    }

    public Reservation pickReservation() {
        int selectedRow = rTable.getSelectedRow();
        editEndLabel("");
        if (selectedRow > -1) {
            return reservationTableModel.getEntity(selectedRow);
        } else {
            endLabel.setText(I18N.getString("noReservationSelected"));
            return null;
        }
    }

    private double calculatePrice(Reservation reservation) {
        LocalDate enteringDate, leavingDate;
        if (reservation.getCheckInDate().compareTo(reservation.getExpectedCheckInDate()) > 0) {
            enteringDate = reservation.getExpectedCheckInDate();
        } else {
            enteringDate = reservation.getCheckInDate();
        }
        if (reservation.getCheckOutDate().compareTo(reservation.getExpectedCheckOutDate()) > 0) {
            leavingDate = reservation.getCheckOutDate();
        } else {
            leavingDate = reservation.getExpectedCheckOutDate();
        }
        return reservation.getRoom().getType().getPrice() * (DAYS.between(enteringDate, leavingDate));
    }

    public void checkOutReservation(ActionEvent actionEvent) {
        Reservation reservation = pickReservation();
        if (reservation == null) {
            return;
        } else if (reservation.getCheckInDate() == null) {
            editEndLabel("cantCheckOutNotCheckIn");
            return;
        } else if (reservation.getCheckOutDate() != null) {
            editEndLabel("cantCheckOutCheckedOut");
            return;
        }
        reservation.setCheckOutDate(LocalDateTime.now().toLocalDate());
        reservationTableModel.updateRow(reservation);
        double result = calculatePrice(reservation);
        endLabel.setText(I18N.getString("pay") + " " + result + "\u20ac");
    }

    public void checkInReservation(ActionEvent actionEvent) {
        Reservation reservation = pickReservation();
        if (reservation == null) {
            return;
        } else if (reservation.getCheckInDate() != null) {
            endLabel.setText(I18N.getString("cantCheckInAlreadyCheckIn"));
            return;
        } else if (reservation.getCheckOutDate() != null) {
            endLabel.setText(I18N.getString("cantCheckInAlreadyCheckOut"));
            return;
        }
        reservation.setCheckInDate(LocalDateTime.now().toLocalDate());
        reservationTableModel.updateRow(reservation);
    }

    public void clearTextFields() {
        guestNameTextField.setText("");
        roomNumTextField.setText("");
    }

    private GridBagConstraints constrains(int gridY, Insets insets, double weightY, double weightX) {
        GridBagConstraints g = new GridBagConstraints();
        g.weighty = weightY;
        g.weightx = weightX;
        g.fill = GridBagConstraints.BOTH;
        if (insets != null)
            g.insets = insets;
        g.gridwidth = 1;
        g.gridy = gridY;
        return g;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public JTable getTable() {
        return rTable;
    }

    public void editEndLabel(String text) {
        if (text.isBlank()) {
            endLabel.setText("");
        } else {
            endLabel.setText(I18N.getString(text));
        }
    }

    public void deleteReservation(ActionEvent actionEvent) {
        reservationTableModel.deleteRow(rTable.getSelectedRow());
        editEndLabel("");
    }
}
