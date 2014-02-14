package org.fsaravia.jtablemodel;

public interface ExportsTableModel {

    public String[] getTitles();

    public Object getValueAt(int column);

    public void setValueAt(int posicion, Object value);
}
