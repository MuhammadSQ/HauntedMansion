import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

//Represents the player character with all stats, inventory, abilities, and progression
public class Character implements Serializable
{
    private static final long serialVersionUID = 2L;

    //Basic character info and stats
    private String name;
    private String color;
    private int hp;
    private int maxHp;
    private int stamina;
    private int maxStamina;
    private int baseAttack;
    private int defense;
    private int level;
    private int xp;
    private double x;
    private double y;
    private Inventory inventory;
    private List<Ability> abilities;
    private int stonesCollected;
    private boolean defeatedBoss;
    private Item equippedWeapon;

    /**
     * A new character with the given name and color
     * 
     * @param name the character's name
     * @param color the character's color
     */
    public Character(String name, String color)
    {
        //Set character stats and starting position
        this.name = name;
        this.color = color;
        this.maxHp = 100;
        this.hp = maxHp;
        this.maxStamina = 167;
        this.stamina = maxStamina;
        this.baseAttack = 15;
        this.defense = 5;
        this.level = 1;
        this.xp = 0;
        this.x = 0;
        this.y = 0;
        this.inventory = new Inventory(8);
        this.abilities = new ArrayList<>();
        this.stonesCollected = 0;
        this.defeatedBoss = false;
        this.equippedWeapon = null;
    }

    public int getHp() { return hp; }

    public int getMaxHp() { return maxHp; }

    public void setHp(int hp) 
    { 
        this.hp = Math.max(0, Math.min(hp, maxHp)); 
    }

    public int getStamina() { return stamina; }

    public int getMaxStamina() { return maxStamina; }

    public void setStamina(int s) 
    { 
        this.stamina = Math.max(0, Math.min(s, maxStamina)); 
    }

    public int getBaseAttack() { return baseAttack; }

    //Total attack includes weapon bonus if equipped
    public int getAttack()
    {
        int bonus = (equippedWeapon != null) ? equippedWeapon.getValue() : 0;
        System.out.println("Base Attack: " + baseAttack + ", Weapon Bonus: " + bonus + ", Total: " + (baseAttack + bonus));
        return baseAttack + bonus;
    }

    public String getWeaponInfo()
    {
        if (equippedWeapon != null) 
        {
            return equippedWeapon.getName() + " (+" + equippedWeapon.getValue() + " ATK)";
        }
        return "None (fists)";
    }

    public int getDefense() { return defense; }

    public int getLevel() { return level; }
    public int getXp() { return xp; }

    /**
     * Adds XP and handles level-ups
     * @param amount XP to add
     * @return true if the character levelled up
     */
    public boolean addXp(int amount)
    {
        xp += amount;
        int needed = level * 100;
        if (xp >= needed)
        {
            xp -= needed;
            level++;
            maxHp += 10;
            hp = Math.min(hp + 10, maxHp);
            maxStamina += 10;
            stamina = Math.min(stamina + 10, maxStamina);
            baseAttack += 3;
            return true;
        }
        return false;
    }

    //The position of the character in the world
    public double getX() { return x; }
    public double getY() { return y; }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }

    public Inventory getInventory() { return inventory; }

    public Item getEquippedWeapon() { return equippedWeapon; }

    /**
     * Equips a weapon, applying its attack bonus 
     * If another weapon is already equipped it will be unequipped first
     * 
     * @param weapon the weapon to equip
     */
    public void equipWeapon(Item weapon)
    {
        // Unequip current weapon first
        if (equippedWeapon != null) 
        {
            equippedWeapon.setEquipped(false);

            // Put current weapon back in inventory
            if (!inventory.addItem(equippedWeapon)) 
            {
                System.out.println("Warning: Could not return unequipped weapon to inventory");
            }
        }
        
        equippedWeapon = weapon;
        if (weapon != null) 
        {
            weapon.setEquipped(true);

            // Remove the weapon from inventory since it's now equipped
            for (int i = 0; i < inventory.getSize(); i++) 
            {
                if (inventory.getItem(i) == weapon) 
                {
                    inventory.removeItem(i);
                    break;
                }
            }
        }
        
        System.out.println("Equipped: " + (weapon != null ? weapon.getName() : "nothing"));
    }

    //Unequips the current weapon
    public void unequipWeapon()
    {
        if (equippedWeapon != null) 
        {
            equippedWeapon.setEquipped(false);
            // Put weapon back in inventory if there's space
            if (!inventory.addItem(equippedWeapon)) 
            {
                System.out.println("Inventory full - couldn't unequip weapon");
            } 
            else 
            {
                equippedWeapon = null;
            }
        }
    }

    //The list of learned abilities
    public List<Ability> getAbilities() { return abilities; }

    /**
     * Adds an ability 
     * Max 6
     * 
     * @param ability the ability to add
     * @return true if added, false if already at max
     */
    public boolean addAbility(Ability ability)
    {
        // Check if ability already exists
        for (Ability existing : abilities) 
        {
            if (existing.getName().equals(ability.getName())) 
            {
                System.out.println("Already have ability: " + ability.getName());
                return false;
            }
        }
        
        if (abilities.size() < 6)
        {
            abilities.add(ability);
            return true;
        }
        return false;
    }

    /**
     * Replaces an ability
     * Prevents any duplicated
     * 
     * @param index the index of the ability to replace
     * @param newAbility the new ability
     */
    public void replaceAbility(int index, Ability newAbility)
    {
        //Check if the player already has this ability
        for (Ability existing : abilities) {
            if (existing.getName().equals(newAbility.getName())) {
                System.out.println("Already have ability: " + newAbility.getName() + ", cannot learn again");
                return;
            }
        }
        
        if (index >= 0 && index < abilities.size())
            abilities.set(index, newAbility);
    }

    public boolean isAbilitiesFull() { return abilities.size() >= 6; }

    public int getStonesCollected() { return stonesCollected; }
    public void addStone() { stonesCollected++; }
    public boolean hasAllStones() { return stonesCollected >= 5; }

    public boolean isBossDefeated() { return defeatedBoss; }
    public void setBossDefeated(boolean d) { this.defeatedBoss = d; }

    public String getName() { return name; }
    public String getColor() { return color; }

    // Restores stamina by amount
    public void restoreStamina(int amount) 
    { 
        setStamina(stamina + amount); 
    }
}