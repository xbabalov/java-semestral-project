package ui;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public abstract class AbstractEntityTableModel<E> extends AbstractTableModel {

    private final List<Column<?, E>> columns;

    protected AbstractEntityTableModel(List<Column<?, E>> columns) {
        this.columns = columns;
    }

    @Override
    public int getColumnCount() {
        return columns.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return getColumn(columnIndex).getValue(getEntity(rowIndex));
    }

    protected abstract E getEntity(int rowIndex);

    protected void updateEntity(E entity) {
    }

    @Override
    public String getColumnName(int columnIndex) {
        return getColumn(columnIndex).getColumnName();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return getColumn(columnIndex).getColumnClass();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return getColumn(columnIndex).isEditable();
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        E entity = getEntity(rowIndex);
        getColumn(columnIndex).setValue(value, entity);
        updateEntity(entity);
    }

    private Column<?, E> getColumn(int columnIndex) {
        return columns.get(columnIndex);
    }
}
