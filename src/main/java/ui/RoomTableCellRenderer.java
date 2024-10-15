package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class RoomTableCellRenderer extends DefaultTableCellRenderer {

    private static final TableCellRenderer RENDERER = new DefaultTableCellRenderer();

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component cellComponent = RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        ((JLabel) cellComponent).setHorizontalAlignment(JLabel.LEFT);
        ((JLabel) cellComponent).setBorder(new EmptyBorder(1, 1, 1, 1));
        return cellComponent;
    }

}
