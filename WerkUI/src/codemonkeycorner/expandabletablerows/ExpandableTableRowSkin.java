package codemonkeycorner.expandabletablerows;

import java.util.HashSet;
import java.util.Set;

import com.sun.javafx.scene.control.skin.TableRowSkin;

import javafx.scene.Parent;
import javafx.scene.control.TableRow;

/**
 * Table row skin that can expand to show a "details" view.
 * 
 * @author Patricia Bradford, www.CodeMonkeyCorner.com
 */
@SuppressWarnings("restriction")
public class ExpandableTableRowSkin<T extends Object> extends TableRowSkin<T> {
    private IExpandableTableRow expandableRef;
    private Set<Parent> expandedContents = new HashSet<>();

    public ExpandableTableRowSkin(TableRow<T> tableRow) {
        super(tableRow);
        expandableRef = (IExpandableTableRow)tableRow;
    }

    @Override
    protected void layoutChildren(double x, final double y, final double w, final double h) {
        super.layoutChildren(x, y, w, h);
        
        TableRow<T> control = getSkinnable();
        double rowHeight = control.getHeight();
        IExpandableTableRow expandable = expandableRef;
        if (expandable == null)
            return;
        
        Parent content = expandable.getExpandedContent();
        if (expandable.isExpanded() && content != null) {
            rowHeight = expandable.getRowHeight();
            if (!expandedContents.contains(content)) {
            	//Only want to add it once
                for(Parent p : expandedContents)
                    getChildren().remove(p);//Remove any old contents
                expandedContents.clear();
                
                getChildren().add(content);
                content.resize(control.getWidth(), control.getHeight() - rowHeight);
                content.relocate(x, snappedTopInset() + rowHeight);
                content.requestLayout();
                expandedContents.add(content);
            }
        } else if (expandedContents.contains(content)) {
            expandedContents.remove(content);
            getChildren().remove(content);
        }
    }
}
