import java.io.Serializable;

/**
 * Represents a shrine on the map that teaches the player a new ability
 * Each shrine can only be used once, and grants a specific ability when interacted with
 * Shrines are placed in certain rooms and can be found by the player as they explore the mansion
 * The ability granted by the shrine can be used in combat or exploration, and is a key part of the player's progression through the game
 */
public class Shrine implements Serializable
{
    private static final long serialVersionUID = 1L;

    private double x;
    private double y;
    private Ability ability;
    private boolean used;

    /**
     * Creates a shrine at the given position that grants the specified ability.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param ability the ability this shrine teaches
     */
    public Shrine(double x, double y, Ability ability)
    {
        this.x = x;
        this.y = y;
        this.ability = ability;
        this.used = false;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public Ability getAbility() { return ability; }
    public boolean isUsed() { return used; }
    public void setUsed(boolean used) { this.used = used; }
}