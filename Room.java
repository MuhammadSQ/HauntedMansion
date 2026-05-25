import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A room in the mansion world
 * Each room has a name, position, size, and can contain an enemy, chest, shrine, or items
 * The room can also be marked as the boss room 
 * Provides methods to check if a point is within the room and to manage the room's contents
 */
public class Room implements Serializable
{
    private static final long serialVersionUID = 2L;

    private String name;
    private double x, y, width, height;
    private Enemy enemy;
    private Chest chest;
    private Shrine shrine;
    private List<Item> items;
    private boolean isBossRoom;
    private boolean isFinalRoom;

    /**
     * Creates a new room with the given name, position, and size
     * 
     * @param name the name of the room
     * @param x the x-coordinate of the room's position
     * @param y the y-coordinate of the room's position
     * @param width the width of the room
     * @param height the height of the room
     */
    public Room(String name, double x, double y, double width, double height)
    {
        this.name = name;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.items = new ArrayList<>();
    }

    public String getName() { return name; }

    /**
     * Sets the name of the room
     * 
     * @param name the name for the room
     */
    public void setName(String name) 
    { 
        this.name = name; 
    }

    // Getters and setters for position, size, contents, and room properties
    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }

    // Getters and setters for contents
    public Enemy getEnemy() { return enemy; }
    public void setEnemy(Enemy e) { enemy = e; }
    public Chest getChest() { return chest; }
    public void setChest(Chest c) { chest = c; }
    public Shrine getShrine() { return shrine; }
    public void setShrine(Shrine s) { shrine = s; }

    // Getters and setters for items
    public List<Item> getItems() { return items; }
    public void addItem(Item item) { items.add(item); }

    // Getters and setters for room properties
    public boolean isBossRoom() { return isBossRoom; }
    public void setBossRoom(boolean b) { isBossRoom = b; }
    public boolean isFinalRoom() { return isFinalRoom; }
    public void setFinalRoom(boolean b) { isFinalRoom = b; }

    /**
     * Checks if a point (px, py) is within the bounds of the room
     * 
     * @param px the x-coordinate of the point to check
     * @param py the y-coordinate of the point to check
     * @return true if the point is within the room, false otherwise
     */
    public boolean contains(double px, double py)
    {
        return px >= x && px <= x + width && py >= y && py <= y + height;
    }
}