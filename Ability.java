import java.io.Serializable;

//Abilities the player can use in battle
public class Ability implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String name;
    private String description;
    private int damage; // bonus damage on top of base attack
    private int staminaCost;
    private int healAmount;

    /**
     * Creates an ability
     *
     * @param name ability name
     * @param description short description
     * @param damage bonus damage dealt
     * @param staminaCost stamina consumed
     * @param healAmount HP restored
     */
    public Ability(String name, String description, int damage, int staminaCost, int healAmount)
    {
        this.name = name;
        this.description = description;
        this.damage = damage;
        this.staminaCost = staminaCost;
        this.healAmount = healAmount;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getDamage() { return damage; }
    public int getStaminaCost() { return staminaCost; }
    public int getHealAmount() { return healAmount; }

    //For display purposes in the ability book and enemy drops
    @Override
    public String toString()
    {
        if (healAmount > 0)
        { 
            return name + " [Heal " + healAmount + " HP | " + staminaCost + " STA]";
        }

        return name + " [+" + damage + " DMG | " + staminaCost + " STA]";
    }

    // Factory helpers so every class uses consistent ability definitions
    public static Ability create(String name)
    {
        switch (name)
        {
            //The abilities
            case "Slash": 
                return new Ability("Slash", "A quick sword slash", 8, 10, 0);

            case "Block": 
                return new Ability("Block", "Reduces next hit by 5", 0,  8,  0);
            
            case "Quick Strike": 
                return new Ability("Quick Strike", "Quick Jabs", 12, 15, 0);

            case "Power Strike": 
                return new Ability("Power Strike", "A Right Hook", 20, 25, 0);

            case "Heal": 
                return new Ability("Heal", "Restore 30 HP", 0, 20, 40);

            case "Shield": 
                return new Ability("Shield", "Halves incoming damage", 0, 18, 0);

            case "Fireball": 
                return new Ability("Fireball", "Magical fire burst", 25, 30, 0);

            case "Ice Shard": 
                return new Ability("Ice Shard", "Piercing ice attack", 22, 28, 0);

            case "Vampiric Hit":
                return new Ability("Vampiric Hit", "Deals damage and heals you",  15, 22, 15);

            case "Whirlwind": 
                return new Ability("Whirlwind", "Spinning blade attack", 18, 20, 0);
                
            default: 
                return new Ability(name, "Unknown ability", 10, 15, 0);
        }
    }
}