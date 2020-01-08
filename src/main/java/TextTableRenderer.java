import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class TextTableRenderer extends JTextArea implements TableCellRenderer {

    public TextTableRenderer() {
        setOpaque(true);
        setLineWrap(true);
        setWrapStyleWord(true);
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column) {

        if (isSelected) {
            setBackground(Color.lightGray);
        } else {
            setBackground(table.getBackground());
        }

        setText((value == null) ? "" : value.toString());

        int width = table.getColumnModel().getColumn(column).getWidth();
        int n_lines = 0;
        if (value != null) {
            n_lines = value.toString().length() * 6 / width + 1;
            if (value.toString().contains("\n"))
                n_lines++;
        }

        // System.out.println("Row: " + row + " Column: " + column + " Num_lines: " +
        // n_lines);
        int height = n_lines * 19;
        if (height > table.getRowHeight(row))
            table.setRowHeight(row, height);

        return this;
    }

}
