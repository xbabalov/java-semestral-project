package ui;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

class Column<T, E> {

    private final String columnName;
    private final Class<T> columnClass;
    private final Function<E, T> valueGetter;

    private Column(String columnName, Class<T> columnClass, Function<E, T> valueGetter) {
        this.columnName = Objects.requireNonNull(columnName, "columnName");
        this.columnClass = Objects.requireNonNull(columnClass, "columnClass");
        this.valueGetter = Objects.requireNonNull(valueGetter, "valueGetter");
    }

    static <T, E> Column<T, E> editable(String columnName, Class<T> columnClass, Function<E, T> valueGetter, BiConsumer<E, T> valueSetter) {
        return new EditableColumn<>(columnName, columnClass, valueGetter, valueSetter);
    }

    static <T, E> Column<T, E> readOnly(String columnName, Class<T> columnClass, Function<E, T> valueGetter) {
        return new Column<>(columnName, columnClass, valueGetter);
    }

    String getColumnName() {
        return columnName;
    }

    Class<T> getColumnClass() {
        return columnClass;
    }

    Object getValue(E entity) {
        return valueGetter.apply(entity);
    }

    void setValue(Object value, E entity) {
        throw new UnsupportedOperationException("Column '" + columnName + "' is not editable");
    }

    boolean isEditable() {
        return false;
    }

    private static final class EditableColumn<T, E> extends Column<T, E> {

        private final BiConsumer<E, T> valueSetter;

        private EditableColumn(String columnName, Class<T> columnClass, Function<E, T> valueGetter, BiConsumer<E, T> valueSetter) {
            super(columnName, columnClass, valueGetter);
            this.valueSetter = Objects.requireNonNull(valueSetter, "valueSetter");
        }

        @Override
        void setValue(Object value, E entity) {
            valueSetter.accept(entity, super.columnClass.cast(value)); // see Item 33: Consider type-safe heterogeneous containers
        }

        @Override
        boolean isEditable() {
            return true;
        }
    }
}