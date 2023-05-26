package group1database.view.celladapter;

import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author JuliusDev
 */
public class TableActionCellRender extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable jtable, Object o, boolean isSelected, boolean bln1, int row, int column) {
        Component com = super.getTableCellRendererComponent(jtable, o, isSelected, bln1, row, column);
        PanelAction action = new PanelAction();
        
        return action;
    }
}
