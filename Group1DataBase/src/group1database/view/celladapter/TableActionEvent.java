package group1database.view.celladapter;

/**
 *
 * @author JuliusDev
 */
public interface TableActionEvent {

    public void onEdit(int row);

    public void onDelete(int row);

    public void onView(int row);
}
