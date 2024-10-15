package ui;

import com.github.lgooddatepicker.components.DatePicker;

import javax.swing.*;
import java.awt.*;

public class CustomizationTools {

    public static void customizeButton(JButton button) {
        button.setFont(new Font("Dialog", Font.BOLD, 13));
        button.setForeground(new Color(153, 51, 51));
        button.setBackground(new Color(255, 204, 204));
        button.setBorder(BorderFactory.createLineBorder(new Color(255, 153, 153)));
    }

    public static void customizeTable(JTable table) {
        table.getTableHeader().setBackground(new Color(255, 204, 204));
        table.getTableHeader().setFont(new Font("Dialog", Font.BOLD, 12));
        table.getTableHeader().setForeground(new Color(153, 51, 51));
        table.getTableHeader().setBorder(BorderFactory.createLineBorder(new Color(255, 153, 153)));
        table.setGridColor(new Color(255, 153, 153));
        table.setBorder(BorderFactory.createLineBorder(new Color(255, 153, 153)));
        table.setBackground(Color.WHITE);
    }

    public static void customizePanel(JPanel panel, Boolean border) {
        if (border) {
            panel.setBorder(BorderFactory.createLineBorder(new Color(255, 153, 153)));
        }
        panel.setBackground(new Color(255, 230, 230));
    }

    public static void customizeLabelBold(JLabel label) {
        label.setFont(new Font("Dialog", Font.BOLD, 13));
        label.setForeground(new Color(153, 51, 51));
    }

    public static void customizeLabelItalic(JLabel label) {
        label.setFont(new Font("Dialog", Font.ITALIC, 13));
        label.setForeground(new Color(153, 51, 51));
    }

    public static void customizeDatePicker(DatePicker picker) {
        picker.setBackground(new Color(255, 230, 230));
    }

}
