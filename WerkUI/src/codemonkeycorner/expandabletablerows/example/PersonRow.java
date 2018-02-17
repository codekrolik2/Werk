package codemonkeycorner.expandabletablerows.example;

import codemonkeycorner.expandabletablerows.AbstractExpandableTableRow;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;

public class PersonRow extends AbstractExpandableTableRow<Person> {
    protected ChangeListener<Boolean> expandListener;
    protected Region content;
    
    public PersonRow() {
        expandListener = new ChangeListener<Boolean>() {
            @Override
            public void changed(@SuppressWarnings("rawtypes") ObservableValue ov, 
            		Boolean oldValue, Boolean isExpanded) {
                setExpanded(isExpanded);
            }
        };
    }
    
    @Override
    public Region getExpandedContent() {
        if (content == null) {
            if (getItem() != null && getItem().getBio() != null) {
                Label label = new Label(getItem().getBio());
                label.setWrapText(true);
                label.setStyle("-fx-background-color:darkgrey;-fx-text-fill:white");
                content = label;
            }
        }
        return content;
    }
    
    protected void onItemUpdate(Person oldValue, Person newValue) {
    	content = null;
        if(oldValue != null)
            oldValue.expandedProperty().removeListener(expandListener);
        if(newValue != null) {
            newValue.expandedProperty().addListener(expandListener);
            setExpanded(newValue.isExpanded());
        }
        super.onItemUpdate(oldValue, newValue);
    }
}
