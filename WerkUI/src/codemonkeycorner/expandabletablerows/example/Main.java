package codemonkeycorner.expandabletablerows.example;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Callback;

public class Main extends Application {
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(final Stage stage) {
		stage.setTitle("People");

		//Create a table.
		TableView<Person> table = createTable();

		stage.setScene(new Scene(table));
		stage.show();
	}

    @SuppressWarnings("unchecked")
	public TableView<Person> createTable() {
        TableView<Person> table = new TableView<Person>();
        //Create expandable row
        table.setRowFactory(createRowFactory());
        
        //Create the expansion column
        final TableColumn<Person, Boolean> expandCol = new TableColumn<>();
        
        expandCol.setCellFactory(new Callback<TableColumn<Person, Boolean>, TableCell<Person, Boolean>>() {
            public TableCell<Person, Boolean> call(TableColumn<Person, Boolean> col) {
                return createExpanderCell();
            }
        });
        
        //Create some other columns
        TableColumn<Person, String> firstNameCol = new TableColumn<>("First Name");
        firstNameCol.setMinWidth(100);
        firstNameCol.setCellValueFactory(new PropertyValueFactory<Person, String>("firstName"));
        
        TableColumn<Person, String> lastNameCol = new TableColumn<>("Last Name");
        lastNameCol.setMinWidth(100);
        lastNameCol.setCellValueFactory(new PropertyValueFactory<Person, String>("lastName"));
        
        TableColumn<Person, String> emailCol = new TableColumn<>("Email");
        emailCol.setMinWidth(200);
        emailCol.setCellValueFactory(new PropertyValueFactory<Person, String>("email"));
        
        table.getColumns().addAll(expandCol, firstNameCol, lastNameCol, emailCol);
        
        ObservableList<Person> data =
            FXCollections.observableArrayList(
                new Person("Jacob", "Smith", "jacob.smith@example.com", "I do some "
                        + "stuff. I do some stuff. I do some stuff"),
                
                new Person("Patricia", "Bradford", "myemail@codemonkey.com", 
                        "I have a blog where I like to show others things I learn."
                                + "I enjoy coding. I love my family. Blah, Blah"),
                
                new Person("Ethan", "Williams", "ethan.williams@example.com", "I do"
                        + " some stuff. I do some stuff. I do some stuff"),
                
                new Person("Emma", "Jones", "emma.jones@example.com", "Emma Jones\n"
                        + "Emma Jones\nEmma Jones\nEmma Jones\nEmma Jones\nEmma Jones\n"),
                
                new Person("Michael", "Brown", "michael.brown@example.com", "Michael Brown\n"
                        + "Michael Brown\nMichael Brown\nMichael Brown\nMichael Brown\nMichael Brown\n")
            );
        
        table.setItems(data);
        return table;
    }

    private Callback<TableView<Person>, TableRow<Person>> createRowFactory() {
        return new Callback<TableView<Person>, TableRow<Person>>() {
            @Override
            public TableRow<Person> call(TableView<Person> p) {
                return new PersonRow();
            }
        };
    }
    
    private TableCell<Person, Boolean> createExpanderCell() {
        final TableCell<Person, Boolean> cell = new TableCell<>();
        final ToggleButton expandButton = new ToggleButton("+");
        
        cell.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        cell.graphicProperty().bind(Bindings.when(cell.emptyProperty()).
                then((Node)null).otherwise(expandButton));
        
        //Add or remove current item from expandedRows on button press
        expandButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
            	Person person = (Person) cell.getTableRow().getItem();
	            person.setExpanded(expandButton.isSelected());
	            if (expandButton.isSelected())
	                expandButton.setText("-");
	            else
	                expandButton.setText("+");
            }
        });

        return cell ;
    }
}