package codemonkeycorner.expandabletablerows;

import javafx.scene.layout.Region;

/**
 * Interface for an expandable table row
 * @author Patricia Bradford, www.CodeMonkeyCorner.com
 */
public interface IExpandableTableRow {
    /**
     * Returns the current state of this expandable item
     */
    public boolean isExpanded();
    /**
     * Sets the current state of this expandable item
     */
    public void setExpanded(boolean expanded);
    /**
     * Gets the height of this row. This does not include the "expanded" item height.
     * @return 
     */
    public double getRowHeight();
    /**
     * Gets the content that should display when this row is expanded
     * @return 
     */
    public Region getExpandedContent();
}
