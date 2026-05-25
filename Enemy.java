import java.io.Serializable;
import java.util.Random;

/**
 * Represents an enemy in the game world
 * Has the enemy's stats, position, and drop information
 * Boss enemies have higher stats and guaranteed drops while regular enemies have randomized drops
 */
public class Enemy implements Serializable
{
    private static final long serialVersionUID = 2L;

    // Instance variables for enemy properties
    private String name;
    private int hp;
    private int maxHp;
    private int attack;
    private int defense;
    private double x;
    private double y;
    private boolean isBoss;
    private int xpReward;
    private Item dropItem;
    private Ability dropAbility; // may be null

    /**
     * Creates a new enemy with the given stats and position
     * 
     * @param name the name of the enemy
     * @param maxHp the maximum health of the enemy
     * @param attack the attack power of the enemy
     * @param defense the defense power of the enemy
     * @param x the x-coordinate of the enemy's position
     * @param y the y-coordinate of the enemy's position
     * @param isBoss whether this enemy is a boss 
     */
    public Enemy(String name, int maxHp, int attack, int defense,
                 double x, double y, boolean isBoss)
    {
        // Initialize instance variables and generate drops
        this.name = name;
        this.maxHp = maxHp;
        this.hp = maxHp;
        this.attack = attack;
        this.defense = defense;
        this.x = x;
        this.y = y;
        this.isBoss = isBoss;
        this.xpReward = isBoss ? 200 : (maxHp / 2);
        generateDrop();
    }

    // Generates a random drop for this enemy based on predefined probabilities
    private void generateDrop()
    {
        Random rng = new Random();
        int roll = rng.nextInt(10); // 0-9
        if (roll < 3)       // 30% potion
        {
            dropItem = new Item("Health Potion", "potion", 20 + rng.nextInt(31), "");
        }
        else if (roll < 6)  // 30% weapon
        {
            dropItem = new Item("Rusty Sword", "weapon", 3 + rng.nextInt(8), "");
        }
        else if (roll < 9)  // 30% ability
        {
            String[] pool = {"Power Strike","Heal","Shield","Fireball","Ice Shard","Vampiric Hit","Whirlwind"};
            dropAbility = Ability.create(pool[rng.nextInt(pool.length)]);
        }
        // 10% chance: nothing
    }

    // Getters and setters for enemy properties
    public String getName() { return name; }
    public int getHp() { return hp; }
    public int getMaxHp() { return maxHp; }
    public int getAttack() { return attack; }
    public int getDefense() { return defense; }
    public double getX() { return x; }
    public double getY() { return y; }
    public boolean isBoss() { return isBoss; }
    public int getXpReward() { return xpReward; }
    public Item getDropItem() { return dropItem; }
    public Ability getDropAbility() { return dropAbility; }

    public void setHp(int hp)    
    { 
        this.hp = Math.max(0, hp); 
    }
    public boolean isDefeated()  
    { 
        return hp <= 0; 
    }
}