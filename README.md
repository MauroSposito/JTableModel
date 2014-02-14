JTableModel
======

## Description
JTableModel is a Java library that allows to easily populate JTables.  
It Features:
  * Loading a Collection of custom objects
  * Sorting the table by clicking on columns headers
  * Filter the data of an already populated table
  * Print the data of the table
  * Auto resizing of columns width according to the contained data

## Instalation
JTableModel has no external dependencies, just add (or compile) the .jar file to your project and you are good to go

## Usage
Every class that will be able to display it's data must implement the `ExportsTableModel` interface, and implements the following methods:

`````java
  public String[] getTitles(); // The titles to be displayed on the table columns

  public Object getValueAt(int column); // The value of each cell for a given object

  public void setValueAt(int posicion, Object value); // A mechanism to update an existing value if you wish to provide a massive update method
`````

# Example
`````java
public class ExampleClass implements ExportsTableModel{
  private String name;
  private String email;

  public String[] getTitles(){
    return new String[]{
      "Name",
      "Email"
    }
  }

  public Object getValueAt(int column){
    switch(column){
      case 0:
        return name;
      case 1:
        return email;
      default:
        throw new IllegalArgumentException("Invalid column " + column)
    }
  }

  public void setValueAt(int posicion, Object value){
    throw new RuntimeException("Updating not allowed") //More on this later
  }
}
`````

Once your class has implemented the `ExportsTableModel` interface, you just need to do the following:

`````java
  JTableModel.createTableModel(examplesList, jTable1) //Where examplesList is an instance of List<ExampleClass> and jTable1 a JTable
`````

And that's all you need! You get a full featured table, with an embedded column sorter.  
If you don't need the sorter or it doesn't apply to your usecase you can call
`````java
  JTableModel.createTableModelWithoutSorter(examplesList, jTable1) //Where examplesList is an instance of List<ExampleClass> and jTable1 a JTable
`````
And that's it!

If you want to, you can access the table data by calling `jTable1.getModel()`, cast it to `TableSorter` or `JTableModel` (according to wether you created a table model with sorter or not), and use the `getData()` or `getDataAt(row)` methods to retrieve your data`

## About filters
Both table models can be filtered to display only the needed data, just call:
`````java
  JTableModel.filter(filterText, columnNumber, jTable) //Displays only the data that matches the filterText on the selected column

  JTableModel.filter(filterArray, columnArray, jTable) //Apply multiple filters at once!
`````

Just call the same method with empty strings to reset the filter

## Cell editions
The `isCellEditable()` method of `TableModel` checks if a certain value can be updated using the `setValueAt()` method. If you want your objects to be editable from the table just implement the correct behaviour, otherwise throw any instance of `RuntimeException` and the model will just know that you don't want that cell to be editable.  
You can just throw Exceptions for certain columns and allow other columns to be edited

## Printing the table
Use the `JTablePrinter` class:
`````java
  JTablePrinter.print(jTable) // Print the content with the default values
  JTablePrinter.print(jTable, printMode, headerFormat, footerFormat, orientation) //Customize the printing
`````
## About TableSorter
The original code for this class can be found at [Oracle's docs](http://docs.oracle.com/javase/tutorial/uiswing/examples/components/TableSorterDemoProject/src/components/TableSorter.java) and has been slightly modified to fit this project. At the moment of writing this README, the license on the linked file allowed me to use it and redistribute it

## License
See the `UNLICENSE` file included in this project.