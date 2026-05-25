import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Fixed capacity item inventory
 * Provides methods to add, remove, swap, and equip items.
 * Handles inventory full scenarios and weapon equipping
 */
public class Inventory implements Serializable
{
    private static final long serialVersionUID = 1L;

    private List<Item> items;
    private int capacity;

    /**
     * Creates an inventory with the specified capacity
     * 
     * @param capacity the maximum number of items the inventory can hold
     */
    public Inventory(int capacity)
    {
        this.capacity = capacity;
        this.items = new ArrayList<>();
    }

    /**
     * Attempts to add an item to the inventory
     * If the inventory is full, the item is not added and the method returns false
     * 
     * @param item the item to add to the inventory
     * @return true if the item was added successfully, false if the inventory is full
     */
    public boolean addItem(Item item)
    {
        if (items.size() < capacity) { items.add(item); return true; }
        return false;
    }

    /**
     * Removes an item from the inventory at the specified index
     * If the index is valid, the item is removed and returned; otherwise, null is
     * 
     * @param index the index of the item to remove
     * @return  the removed item if the index is valid, or null if the index is out of bounds
     */
    public Item removeItem(int index)
    {
        if (index >= 0 && index < items.size())
        {
            return items.remove(index);
        }
        return null;
    }

    /**
     * Swaps an item in the inventory at the specified index with a new item
     * If the index is valid, the old item is replaced with the new item and returned
     * 
     * @param index the index of the item to swap
     * @param newItem  the new item to place in the inventory
     * @return the old item that was replaced if the index is valid, or null if the index is out of bounds
     */
    public Item swapItem(int index, Item newItem)
    {
        if (index >= 0 && index < items.size())
        {
            Item old = items.get(index);
            items.set(index, newItem);
            return old;
        }
        return null;
    }

    /**
    * Equips a weapon from the inventory at the specified index
    * If the item at the index is a weapon, it is returned; otherwise, null is returned
    *  
    * @param index the index of the item to equip
    * @return the item if it is a weapon and the index is valid, or null if the index is out of bounds or the item is not a weapon
    */
    public Item equipWeapon(int index)
    {
        if (index >= 0 && index < items.size())
        {
            Item item = items.get(index);
            if (item.getType().equals("weapon"))
            {
                return item;
            }
        }
        return null;
    }

    public List<Item> getItems() { return items; }
    public Item getItem(int index)  
    { 
        return (index >= 0 && index < items.size()) ? items.get(index) : null; 
    }
    public boolean isFull()
    { 
        return items.size() >= capacity; 
    }
    public int getSize() { return items.size(); }
    public int getCapacity() { return capacity; }
}