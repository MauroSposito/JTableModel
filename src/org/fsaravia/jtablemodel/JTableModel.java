package org.fsaravia.jtablemodel;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.table.*;

public final class JTableModel<T extends ExportsTableModel> extends AbstractTableModel {

    private List<T> data;
    private List<T> allData;
    private String[] titles = new String[]{};

    private JTableModel(List<T> data) {
        loadData(data);
    }

    private void loadData(List<T> data) {
        if (data != null && !data.isEmpty()) {
            this.titles = data.get(0).getTitles();
            this.data = data;
            this.allData = data;
        } else {
            this.titles = new String[]{};
            this.data = new ArrayList<>();
            this.allData = this.data;
        }
    }

    /**
     * Creates a new TableSorter object with the given data and sets it to the
     * JTable
     *
     * @param <T>
     * @param data
     * @param table
     */
    public static <T extends ExportsTableModel> void createTableModel(List<T> data, JTable table) {
        if (table == null) {
            throw new NullPointerException("The given jTable has not been initialized");
        } else if (table.getModel() != null && (table.getModel() instanceof TableSorter || table.getModel() instanceof JTableModel)) {
            JTableModel model;
            if (table.getModel() instanceof TableSorter) {
                TableSorter sorter = (TableSorter) table.getModel();
                model = sorter.getTableModel();
            } else {
                model = (JTableModel) table.getModel();
            }
            int currentColumnCount = table.getColumnCount();
            ListSelectionModel lsm = table.getSelectionModel();
            int firstSelectedRow = lsm.getMinSelectionIndex();
            int lastSelectedRow = lsm.getMaxSelectionIndex();

            model.loadData(data);
            int newColumnCount = model.getColumnCount();
            if (currentColumnCount != newColumnCount) {
                model.fireTableStructureChanged();
            } else {
                model.fireTableDataChanged();
                if (!model.data.isEmpty()) {
                    if (lastSelectedRow < model.data.size()) {
                        lsm.setSelectionInterval(firstSelectedRow, lastSelectedRow);
                    } else {
                        lsm.setSelectionInterval(model.data.size() - 1, model.data.size() - 1);
                    }
                }
            }
            if (table.getColumnCount() > 1) {
                autoResizeColWidth(table);
            } else {
                table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            }

        } else {
            JTableModel<T> m = new JTableModel<>(data);
            TableSorter tableSorter = new TableSorter(m, table.getTableHeader());
            table.setModel(tableSorter);
            if (table.getColumnCount() > 1) {
                autoResizeColWidth(table);
            } else {
                table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            }
        }
    }

    /**
     * Creates a new TableModel (no sorter) object with the given data and sets
     * it to the JTable
     *
     * @param <T>
     * @param data
     * @param table
     */
    public static <T extends ExportsTableModel> void createTableModelWithoutSorter(List<T> data, JTable table) {
        if (table == null) {
            throw new IllegalArgumentException("The given jTable has not been initialized");
        } else if (table.getModel() != null && table.getModel() instanceof JTableModel) {
            createTableModel(data, table); //Update existent model
        } else {
            JTableModel<T> m = new JTableModel<>(data);
            table.setModel(m);
        }
        if (table.getColumnCount() > 1) {
            autoResizeColWidth(table);
        } else {
            table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        }
    }

    /**
     * Automatically adjusts the table's colums size to the data lenght
     *
     *
     * @param table
     */
    public static void autoResizeColWidth(JTable table) {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        Component parent = table.getParent();
        double parentWidhtWorkAround = 0;
        if (parent.getParent() != null) {
            parent = parent.getParent();
            parentWidhtWorkAround = -2;
        }
        if (parent instanceof JScrollPane) {
            JScrollPane pane = (JScrollPane) parent;
            Border border = pane.getBorder();
            if (border != null && border instanceof TitledBorder) {
                parentWidhtWorkAround = -10;
            }
        }

        double tableWidth = parentWidhtWorkAround + Math.max(parent.getPreferredSize().getWidth(), parent.getWidth());
        double usedWidth = 0;
        double margin = 5;
        DefaultTableColumnModel colModel = (DefaultTableColumnModel) table.getColumnModel();
        for (int columnIndex = 0; columnIndex < table.getColumnCount(); columnIndex++) {
            TableColumn column = colModel.getColumn(columnIndex);
            double width;
            TableCellRenderer renderer = column.getHeaderRenderer();
            if (renderer == null) {
                renderer = table.getTableHeader().getDefaultRenderer();
            }
            Component comp = renderer.getTableCellRendererComponent(table, column.getHeaderValue(), false, false, 0, 0);
            width = comp.getPreferredSize().getWidth();
            // Get maximum width of column data
            for (int r = 0; r < Math.min(table.getRowCount(), 10); r++) {//Iterate through the first 10 rows only if the row count is too big
                renderer = table.getCellRenderer(r, columnIndex);
                comp = renderer.getTableCellRendererComponent(table, table.getValueAt(r, columnIndex), false, false, r, columnIndex);
                width = Math.max(width, comp.getPreferredSize().getWidth());
            }
            width += 2 * margin;
            column.setPreferredWidth((int) width);
            usedWidth += width;
        }
        double subtract = 0;
        if (parent.getPreferredSize().getHeight() < (table.getRowCount() * table.getRowHeight())) {
            subtract = 20;
        }
        int restante = (int) ((tableWidth - usedWidth - subtract) / table.getColumnCount());
        if (restante > 0) {
            for (int i = 0; i < table.getColumnCount(); i++) {
                TableColumn col = colModel.getColumn(i);

                col.setPreferredWidth(col.getPreferredWidth() + restante);
                usedWidth += restante;
            }
        }
    }

    @Override
    public int getRowCount() {
        if (data == null) {
            return 0;
        } else {
            return data.size();
        }
    }

    @Override
    public int getColumnCount() {
        return titles.length;
    }

    @Override
    public void setValueAt(Object valor, int row, int col) {
        this.data.get(row).setValueAt(col, valor);
        fireTableCellUpdated(row, col);
    }

    @Override
    public Object getValueAt(int row, int column) {
        return data.get(row).getValueAt(column);
    }

    public ExportsTableModel getDataAt(int row) {
        return data.get(row);
    }

    @Override
    public Class getColumnClass(int c) {
        Object o = getValueAt(0, c);
        if (o == null) {
            return String.class;
        } else {
            return o.getClass();
        }
    }

    @Override
    public String getColumnName(int col) {
        return titles[col];
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        try {
            Object dato = getValueAt(row, col);
            this.data.get(row).setValueAt(col, dato);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    /**
     * Modifies the original model to display only the values that matches the
     * filter (Does not affect the data)
     *
     * @param filter The text to use as filter
     * @param column The column to check for the condition
     * @param table
     */
    public static void filterData(String filter, int column, JTable table) {
        String[] filters = new String[]{filter};
        int[] columns = new int[]{column};
        filterData(filters, columns, table);
    }

    /**
     * Modifies the original model to display only the values that matches more
     * than one filter (Does not affect the data)
     *
     * @param filters An array of text to use as filter
     * @param columns An array of columns to check for the condition
     * @param table
     */
    public static void filterData(String[] filters, int[] columns, JTable table) {
        JTableModel model;
        if (table.getModel() instanceof TableSorter) {
            TableSorter tableSorter = (TableSorter) table.getModel();
            model = tableSorter.getTableModel();
        } else {
            model = (JTableModel) table.getModel();
        }
        if (filters == null || filters.length == 0) {
            removeFilters(model);
        } else {
            if (filters.length != columns.length) {
                throw new IllegalArgumentException("Filters and columns vectors have different sizes");
            }
            model.data = new ArrayList<>();
            for (Iterator<ExportsTableModel> it = model.allData.iterator(); it.hasNext();) {
                ExportsTableModel row = it.next();
                boolean includeRow = true;
                String[] data = new String[columns.length];
                for (int columnIndex = 0; columnIndex < columns.length; columnIndex++) {
                    int columnNumber = columns[columnIndex];
                    data[columnIndex] = String.valueOf(row.getValueAt(columnNumber)).toLowerCase();
                    filters[columnIndex] = filters[columnIndex].toLowerCase();
                    if (filters[columnIndex] != null && !filters[columnIndex].isEmpty() && !data[columnIndex].contains(filters[columnIndex])) {
                        includeRow = false;
                    }
                }
                if (includeRow) {
                    model.data.add(row);
                }
            }
            model.fireTableDataChanged();
        }
    }

    /**
     * Removes the filter and displays the original data
     *
     * @param model
     */
    private static void removeFilters(JTableModel model) {
        model.data = model.allData;
        model.fireTableDataChanged();
    }
}
