import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A chest on the map that holds items
 * These items can consist of
 * weapons
 * stones
 * potions
 * abilities
 */
public class Chest implements Serializable
{
    private static final long serialVersionUID = 1L;

    private double x, y;
    private List<Item> items;
    private boolean opened;

    /**
     * Creates a chest at the given position.
     * 
     * @param x the x-coordinate of the chest's position
     * @param y the y-coordinate of the chest's position
     */
    public Chest(double x, double y)
    {
        this.x = x;
        this.y = y;
        this.items = new ArrayList<>();
        this.opened = false;
    }

    public void addItem(Item item) { items.add(item); }

    // Gets the list of items in the chest
    public List<Item> getItems() { return items; }
    public Item getItem(int i) 
    { 
        return (i >= 0 && i < items.size()) ? items.get(i) : null; 
    }
    public Item takeItem(int i) 
    { 
        return (i >= 0 && i < items.size()) ? items.remove(i) : null; 
    }

    public double getX() { return x; }
    public double getY() { return y; }

    public boolean isOpened() { return opened; }
    public void setOpened(boolean o) { opened = o; }

    public boolean isEmpty() 
    { 
        return items.isEmpty(); 
    }
}