package ui;

import model.Reservation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class ReservationTableCellRenderer extends DefaultTableCellRenderer {

    private static final TableCellRenderer RENDERER = new DefaultTableCellRenderer();

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component cellComponent = RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        ((JLabel) cellComponent).setHorizontalAlignment(JLabel.LEFT);
        ((JLabel) cellComponent).setBorder(new EmptyBorder(1, 1, 1, 1));
        Reservation reservation = ((ReservationTableModel) table.getModel()).getEntity(table.convertRowIndexToModel(row));
        boolean checkedIn = reservation.getCheckInDate() != null;
        boolean checkedOut = reservation.getCheckOutDate() != null;
        if (isSelected) {
            cellComponent.setBackground(table.getSelectionBackground());
        } else {
            if (checkedOut) {
                cellComponent.setBackground(new Color(221, 221, 238));
            } else if (checkedIn) {
                cellComponent.setBackground(new Color(230, 243, 217));
            } else {
                cellComponent.setBackground(Color.WHITE);
            }
        }
        return cellComponent;
    }

}
