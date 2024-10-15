package ui;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import model.Guest;
import model.Reservation;
import model.Room;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;

public class NewReservationTab implements ReservationEditor {

    private JPanel mainPanel, leftPanel, rightPanel, controlPanel, detailsPanel, tablePanel;
    private JComboBox<Integer> numOfGuestsComboBox;
    private JTable roomsTable;
    private JTextField nameTextField, phoneTextField, emailTextField, addressTextField;
    private JTextArea detailsTextArea;
    private DatePicker checkInDatePicker, checkOutDatePicker;
    private Component previousTabComponent;
    private final ReservationTableModel reservationTableModel;
    private final RoomTableModel roomTableModel;
    private final JLabel warningLabel = new JLabel("");
    private static final I18N I18N = new I18N(NewReservationTab.class);

    private Reservation newReservation;

    private volatile Reservation toEdit;

    public NewReservationTab(ReservationTableModel reservationTableModel, RoomTableModel roomTableModel) {
        this.reservationTableModel = reservationTableModel;
        this.roomTableModel = roomTableModel;
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
        fillDetailsPanel();
        createButtons();
        assignPanels();
    }

    public void createAdditionalPanels() {
        leftPanel = new JPanel(new GridBagLayout());
        rightPanel = new JPanel(new GridBagLayout());
        controlPanel = new JPanel(new GridBagLayout());
        detailsPanel = new JPanel(new GridBagLayout());
        tablePanel = new JPanel(new BorderLayout());
        CustomizationTools.customizePanel(leftPanel, false);
        CustomizationTools.customizePanel(rightPanel, false);
        CustomizationTools.customizePanel(controlPanel, true);
        CustomizationTools.customizePanel(detailsPanel, false);
        CustomizationTools.customizePanel(tablePanel, false);
    }

    public void assignPanels() {
        leftPanel.add(controlPanel, constrains(0, new Insets(15, 0, 0, 0), 0, 0.5));
        leftPanel.add(tablePanel, constrains(1, new Insets(10, 10, 10, 10), 1, 1));
        rightPanel.add(detailsPanel, constrains(1, new Insets(0, 0, 0, 0), 1, 0.5));
        mainPanel.add(leftPanel, constrains(0, new Insets(0, 10, 0, 0), 1, 1));
        mainPanel.add(rightPanel, constrains(0, new Insets(0, 20, 20, 10), 1, 1));
    }

    public void fillControlPanel() {
        JLabel checkIn = new JLabel(I18N.getString("checkInDate"));
        CustomizationTools.customizeLabelBold(checkIn);
        JLabel checkOut = new JLabel(I18N.getString("checkOutDate"));
        CustomizationTools.customizeLabelBold(checkOut);
        JLabel guests = new JLabel(I18N.getString("guestCount"));
        CustomizationTools.customizeLabelBold(guests);

        controlPanel.add(checkIn, constrains(0, new Insets(10, 10, 5, 10), 1, 1));
        controlPanel.add(checkOut, constrains(0, new Insets(10, 10, 5, 10), 1, 1));
        controlPanel.add(guests, constrains(0, new Insets(10, 10, 5, 10), 1, 1));

        checkInDatePicker = createDataPicker();
        CustomizationTools.customizeDatePicker(checkInDatePicker);
        controlPanel.add(checkInDatePicker, constrains(1, new Insets(0, 10, 10, 10), 1, 1));

        checkOutDatePicker = createDataPicker();
        CustomizationTools.customizeDatePicker(checkOutDatePicker);
        controlPanel.add(checkOutDatePicker, constrains(1, new Insets(0, 10, 10, 10), 1, 1));

        numOfGuestsComboBox = createGuestsComboBox();
        controlPanel.add(numOfGuestsComboBox, constrains(1, new Insets(0, 10, 10, 10), 1, 1));

        addButton(controlPanel,"search", this::showAvailableRooms, 1, new Insets(0, 10, 10, 10));

        leftPanel.add(controlPanel, constrains(0, new Insets(0, 0, 0, 0), 1, 1));
    }

    public void fillTablePanel() {
        roomsTable = createTable();
        tablePanel.add(new JScrollPane(roomsTable), BorderLayout.CENTER);
    }

    public void fillDetailsPanel() {
        JLabel personalInfoLabel = new JLabel(I18N.getString("personalInfo"));
        JLabel nameLabel = new JLabel(I18N.getString("name"));
        JLabel phoneLabel = new JLabel(I18N.getString("phone"));
        JLabel emailLabel = new JLabel(I18N.getString("email"));
        JLabel addressLabel = new JLabel(I18N.getString("address"));
        JLabel additionalDetailsLabel = new JLabel(I18N.getString("details"));
        CustomizationTools.customizeLabelBold(personalInfoLabel);
        CustomizationTools.customizeLabelItalic(nameLabel);
        CustomizationTools.customizeLabelItalic(phoneLabel);
        CustomizationTools.customizeLabelItalic(emailLabel);
        CustomizationTools.customizeLabelItalic(addressLabel);
        CustomizationTools.customizeLabelItalic(additionalDetailsLabel);

        rightPanel.add(personalInfoLabel, constrains(0, null, 0.1, 0.5));

        detailsPanel.add(nameLabel, constrains(0, new Insets(10, 10, 10, 10), 0.1, 0.5));
        detailsPanel.add(phoneLabel, constrains(1, new Insets(10, 10, 10, 10), 0.1, 0.5));
        detailsPanel.add(emailLabel, constrains(2, new Insets(10, 10, 10, 10), 0.1, 0.5));
        detailsPanel.add(addressLabel, constrains(3, new Insets(10, 10, 10, 10), 0.1, 0.5));
        detailsPanel.add(additionalDetailsLabel, constrains(4, new Insets(10, 10, 10, 10), 0.1, 0.5));

        nameTextField = new JTextField(15);
        detailsPanel.add(nameTextField, constrains(0, new Insets(10, 0, 10, 10), 0.1, 1));
        phoneTextField = new JTextField(15);
        detailsPanel.add(phoneTextField, constrains(1, new Insets(10, 0, 10, 10), 0.1, 1));
        emailTextField = new JTextField(15);
        detailsPanel.add(emailTextField, constrains(2, new Insets(10, 0, 10, 10), 0.1, 1));
        addressTextField = new JTextField(15);
        detailsPanel.add(addressTextField, constrains(3, new Insets(10, 0, 10, 10), 0.1, 1));
        detailsTextArea = new JTextArea(6, 15);
        detailsPanel.add(new JScrollPane(detailsTextArea), constrains(4, new Insets(10, 0, 10, 10), 0.1, 1));
    }

    public JButton addButton(JPanel panel, String key, ActionListener listener, int grid, Insets insets) {
        JButton button = new JButton(I18N.getString(key));
        CustomizationTools.customizeButton(button);
        panel.add(button, constrains(grid, insets, 0, 1));
        button.addActionListener(listener);
        return button;
    }

    public void createButtons() {
        var defaultButtonSize = new Dimension(100, 50);
        var defaultInsets = new Insets(5,10, 5, 10);

        addButton(rightPanel,"clear", this::clearAll, 4, defaultInsets)
                .setPreferredSize(defaultButtonSize);
        addButton(rightPanel,"checkIn", this::checkIn, 5, defaultInsets)
                .setPreferredSize(defaultButtonSize);
        addButton(rightPanel,"confirm", this::createNewReservation, 6, defaultInsets)
                .setPreferredSize(defaultButtonSize);

        CustomizationTools.customizeLabelBold(warningLabel);
        GridBagConstraints c = constrains(7, new Insets(5, 10, 5, 10), 0, 1);
        c.gridwidth = 2;
        rightPanel.add(warningLabel, c);
    }

    public JComboBox<Integer> createGuestsComboBox() {
        var comboBox = new JComboBox<Integer>();
        Integer[] myNum = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
        DefaultComboBoxModel<Integer> comboBoxModel = new DefaultComboBoxModel<>(myNum);
        comboBox.setModel(comboBoxModel);
        return comboBox;
    }

    public DatePicker createDataPicker() {
        return new DatePicker(new DatePickerSettings());
    }

    private JTable createTable() {
        var table = new JTable(roomTableModel);
        CustomizationTools.customizeTable(table);
        table.setRowHeight(20);
        table.getTableHeader().setReorderingAllowed(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        RoomTableCellRenderer renderer = new RoomTableCellRenderer();
        table.setDefaultRenderer(Object.class, renderer);
        table.setDefaultRenderer(Integer.class, renderer);
        return table;
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

    public void clearAll(ActionEvent actionEvent) {
        detailsTextArea.setText("");
        emailTextField.setText("");
        phoneTextField.setText("");
        nameTextField.setText("");
        addressTextField.setText("");
        numOfGuestsComboBox.setSelectedIndex(0);
        checkInDatePicker.setDate(null);
        checkOutDatePicker.setDate(null);
        roomsTable.clearSelection();
        roomTableModel.hideAll();
    }

    public void editReservation() {
        if (toEdit != null) {
            detailsTextArea.setText(toEdit.getGuest().getDetails());
            emailTextField.setText(toEdit.getGuest().getEmail());
            phoneTextField.setText(toEdit.getGuest().getPhone());
            nameTextField.setText(toEdit.getGuest().getName());
            addressTextField.setText(toEdit.getGuest().getAddress());
            numOfGuestsComboBox.setSelectedIndex(toEdit.getNumGuests());
            checkInDatePicker.setDate(toEdit.getExpectedCheckInDate());
            checkOutDatePicker.setDate(toEdit.getExpectedCheckOutDate());
            int roomNumber = toEdit.getRoom().getNumber();
            showAvailableRooms(null);
            for (int x = 0; x < roomsTable.getRowCount(); x++) {
                if(roomTableModel.getEntity(x).getNumber() == roomNumber){
                    roomsTable.setRowSelectionInterval(x, x);
                    break;
                }
            }
        }
    }

    public void createNewReservation(ActionEvent actionEvent) {
        createReservation(false);
    }

    public void checkIn(ActionEvent actionEvent) {
        createReservation(true);
    }

    private boolean checkIfInfoComplete() {
        return roomsTable.getSelectedRowCount() >= 1 &&
                checkInDatePicker.getDate() != null &&
                checkOutDatePicker.getDate() != null &&
                checkInDatePicker.getDate().compareTo(checkOutDatePicker.getDate()) < 0 &&
                nameTextField.getText() != null && nameTextField.getText().trim().length() > 0 &&
                emailTextField.getText() != null && emailTextField.getText().trim().length() > 0 &&
                addressTextField.getText() != null && addressTextField.getText().trim().length() > 0 &&
                phoneTextField.getText() != null && phoneTextField.getText().trim().length() > 0;
    }

    public void createReservation(boolean checkIn) {
        warningLabel.setText("");
        if (!checkIfInfoComplete()) {
            warningLabel.setText(I18N.getString("enterAllInfo"));
            return;
        }

        var checkInDate = checkInDatePicker.getDate();
        var checkOutDate = checkOutDatePicker.getDate();
        var guest = new Guest(nameTextField.getText(), emailTextField.getText(), addressTextField.getText(), detailsTextArea.getText(), phoneTextField.getText());
        var numOfGuests = (int) numOfGuestsComboBox.getModel().getSelectedItem();
        var roomNumber = (int) roomsTable.getValueAt(roomsTable.getSelectedRow(), 0);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {

                Room room = roomTableModel.getSelectedRoom(roomNumber);

                if (toEdit == null) {
                    newReservation = new Reservation(checkInDate, checkOutDate, numOfGuests, guest);
                } else {
                    newReservation = toEdit;
                    newReservation.setExpectedCheckInDate(checkInDate);
                    newReservation.setExpectedCheckOutDate(checkOutDate);
                    newReservation.setNumGuests(numOfGuests);
                    newReservation.setGuest(guest);
                }
                newReservation.setRoom(room);

                if (checkIn) {
                    newReservation.setCheckInDate(LocalDateTime.now().toLocalDate());
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (InterruptedException | ExecutionException e) {
                    JOptionPane.showMessageDialog(null, I18N.getString("creatingFailed"));
                    e.printStackTrace();
                }
                if (toEdit != null) {
                    reservationTableModel.updateEntity(newReservation);
                    toEdit = null;
                    switchToPreviousTab();
                }else{
                    reservationTableModel.addRow(newReservation);
                }
                roomTableModel.hideAll();
                clearAll(null);
            }
        };
        worker.execute();
    }

    public void showAvailableRooms(ActionEvent actionEvent) {
        var checkInDate = checkInDatePicker.getDate();
        var checkOutDate = checkOutDatePicker.getDate();

        if (checkInDate == null || checkOutDate == null || checkInDate.compareTo(checkOutDate) >= 0) {
            showDialog("selectValDate");
        } else if (numOfGuestsComboBox.getSelectedItem() != null && (Integer) numOfGuestsComboBox.getSelectedItem() == 0) {
            showDialog("zeroGuests");
        } else {
            roomTableModel.showAvailableRooms(checkInDate, checkOutDate, (toEdit != null) ? toEdit.getRoom().getNumber() : null);
        }
    }

    private void switchToThisTab() {
        JTabbedPane tabbedPane = (JTabbedPane) mainPanel.getParent();
        previousTabComponent = tabbedPane.getSelectedComponent();
        tabbedPane.setSelectedComponent(mainPanel);
    }

    private void switchToPreviousTab() {
        if(previousTabComponent != null){
            JTabbedPane tabbedPane = (JTabbedPane) mainPanel.getParent();
            tabbedPane.setSelectedComponent(previousTabComponent);
            previousTabComponent = null;
        }
    }

    public void showDialog(String text) {
        JOptionPane.showMessageDialog(mainPanel, I18N.getString(text));
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    @Override
    public void edit(Reservation reservation) {
        this.toEdit = reservation;
        switchToThisTab();
    }

    public Reservation getToEdit() {
        return toEdit;
    }
}