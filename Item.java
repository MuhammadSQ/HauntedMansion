import java.io.Serializable;

/**
 * Represents an item in the game.
 * Types: weapon, potion, stone, ability
 */
public class Item implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String name;
    private String type;
    private int value;   // damage bonus for weapon, heal amount for potion
    private String color;   // for stones
    private boolean equipped; // for weapons

    /**
     * Constructor
     * 
     * @param name the name of the item
     * @param type the type of the item 
     * @param value the value of the item 
     * @param color the color of the stones
     */
    public Item(String name, String type, int value, String color)
    {
        this.name = name;
        this.type = type;
        this.value = value;
        this.color = color;
        this.equipped = false;
    }

    public String getName() { return name; }
    public String getType() { return type; }
    public int getValue() { return value; }
    public String getColor() { return color; }
    public boolean isEquipped() { return equipped; }
    public void setEquipped(boolean equipped) { this.equipped = equipped; }

    /* 
     * Returns a string representation of the item, including its name, type, and relevant stats or properties based on its type
     * For example, a weapon will show its attack bonus, a potion will show its heal amount, and a stone will show its color
     */
    @Override
    public String toString()
    {
        switch (type)
        {
            case "stone": 
                return name + " (" + color + " Stone)";
            case "potion": 
                return name + " (+" + value + " HP)";
            case "weapon": 
                return name + " (+" + value + " ATK)" + (equipped ? " [E]" : "");
            case "ability": 
                return name + " (Ability Scroll)";
            default: 
                return name;
        }
    }
}