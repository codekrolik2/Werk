package codemonkeycorner.expandabletablerows;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.Skin;
import javafx.scene.control.TableRow;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/**
 * Expandable table row. You will need to provide the expanded content. You will
 * also likely want to change the expanded state via some listener to the data.
 * The rows are reused for different data objects so you cannot keep the expanded
 * state data in the row only.
 * 
 * @see #getExpandedContent() 
 * @author Patricia Bradford, www.CodeMonkeyCorner.com
 */
public abstract class AbstractExpandableTableRow<T> extends TableRow<T> implements IExpandableTableRow {
    private boolean isExpanded;
    private double rowHeight = -1.0;
    
	public AbstractExpandableTableRow() {
    	this.isExpanded = false;
    	
        itemProperty().addListener(
        	new ChangeListener<T>() {
	            @Override
	            public void changed(ObservableValue<? extends T> ov, T t, T t1) {
	            	onItemUpdate(t, t1);
	            }
	        }
        );
    }
    
    @Override
    public abstract Region getExpandedContent();

    @Override
    public double getRowHeight() {
        return rowHeight;
    }

    @Override
    public boolean isExpanded() {
        return isExpanded;
    }

    @Override
    public void setExpanded(boolean isExpanded) {
    	this.isExpanded = isExpanded;
    	onExpandChange();
    }
    
	protected void onItemUpdate(T t, T t1) {
		updateSize();
	}
	
    @SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
    protected Skin<?> createDefaultSkin() {
        return new ExpandableTableRowSkin(this);
    }

    protected void onExpandChange() {
        //Figure out what new row height is for the table
        if (rowHeight < 1)
            rowHeight = getHeight();
        updateSize();
    }

    /**
     * Updates the size for the row cell
     */
    protected void updateSize() {
        if (rowHeight < 1)
            return;//Item set, but not expanded yet (shown)
            
        int height = isExpanded() ? 
        		(int)rowHeight + getContentHeight()
                : 
                (int)rowHeight;
        setStyle("-fx-cell-size: " + height + "px;");
    }

    private int getContentHeight() {
        Region content = getExpandedContent();
        if (content == null)
            return 0;
        
        int height = (int) content.getHeight();
        if (height < 1 && content.getParent() == null)
            height = computeHeight(content);
        return height;
    }

    private int computeHeight(Region node) {
        //We want to limit width to width of row, but not limit height
        //in case the node needs to adjust due to row width
        StackPane dummyP = new StackPane();
       	new Scene(dummyP, this.getWidth(), Integer.MAX_VALUE);
       	
        dummyP.getChildren().add(node);
        dummyP.applyCss();
        dummyP.layout();
        
        int height = (int) node.getHeight();
        dummyP.getChildren().remove(node);
        
        return height;
    }
}
