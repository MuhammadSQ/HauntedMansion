import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The game world containing all rooms, shrines, and the current room state.
 * Handles world generation, room population, and tracking the player's current location in the world.
 */
public class GameWorld implements Serializable
{
    private static final long serialVersionUID = 2L;

    private List<Room>   rooms;
    private List<Shrine> shrines;
    private Room         currentRoom;
    private transient Random rng;  // transient to avoid serialization issues

    private static final double ROOM_SIZE = 400;
    private static final double ROOM_SPACING = 600;

    private static final String[] STONE_COLORS = {"Red", "Blue", "Green", "Yellow", "Purple"};

    // Initializes the game world by generating rooms, placing the boss, and populating the rooms with enemies, chests, and shrines
    public GameWorld()
    {
        System.out.println("GameWorld constructor started");
        rooms = new ArrayList<>();  // Make sure this is initialized FIRST
        shrines = new ArrayList<>();
        rng = new Random();
        System.out.println("About to generate world...");
        generateWorld();
        System.out.println("GameWorld constructor finished. Rooms count: " + (rooms != null ? rooms.size() : "null"));
    }

    // Generates the rooms and shrines for the game world
    private void generateWorld()
    {
        System.out.println("generateWorld() started");

        if (rooms == null) {
        System.out.println("rooms was null, creating new ArrayList");
        rooms = new ArrayList<>();
        }
        
        String[][] grid = {
            {"Entrance", "Dining Room", "Library", "Conservatory", "Gallery", "Study"},
            {"Living Room", "Kitchen", "Pantry", "Wine Cellar", "Ballroom", "Music Room"},
            {"Guest Bedroom", "Master Bedroom", "Bathroom", "Walk-in Closet", "Nursery", "Playroom"},
            {"Hallway", "Grand Hall", "Chapel", "Courtyard", "Greenhouse", "Garden"},
            {"Armory", "Training Room", "Trophy Room", "Observatory", "Laboratory", "Library Annex"},
            {"Throne Room", "Treasure Vault", "Secret Passage", "Dungeon", "Crypt", "Standard Room"}
        };

        double startX = 200, startY = 200;

        // First create all rooms
        for (int row = 0; row < grid.length; row++)
        {
            for (int col = 0; col < grid[row].length; col++)
            {
                double rx = startX + col * ROOM_SPACING;
                double ry = startY + row * ROOM_SPACING;
                Room room = new Room(grid[row][col], rx, ry, ROOM_SIZE, ROOM_SIZE);
                rooms.add(room);
            }
        }
        
        System.out.println("Created " + rooms.size() + " rooms");
        
        // Pick a RANDOM room for the boss chamber (not the first room)
        Random rand = new Random();
        int bossIndex = rand.nextInt(rooms.size() - 1) + 1;
        Room bossRoom = rooms.get(bossIndex);
        bossRoom.setName("BOSS CHAMBER");
        bossRoom.setBossRoom(true);
        bossRoom.setFinalRoom(true);
        
        // Update the old "Standard Room" name if it wasn't chosen as boss
        for (Room r : rooms) 
        {
            if (r.getName().equals("Standard Room") && r != bossRoom) 
            {
                r.setName("Storage Room");
            }
        }

        System.out.println("Boss room set at index: " + bossIndex);
        populateRooms();
        currentRoom = rooms.get(0);
        System.out.println("Current room set to: " + currentRoom.getName());
    }

    // Populates the rooms with enemies, chests, and shrines
    private void populateRooms()
    {
        int stoneIdx = 0;
        int shrineCount = 0;
        String[] shrineAbilities = {
            "Power Strike", "Heal", "Shield", "Fireball", "Ice Shard",
            "Vampiric Hit", "Whirlwind", "Quick Strike", "Thunder Strike", "Poison Blade"
        };

        for (Room room : rooms)
        {
            if (room.isBossRoom())
            {
                Enemy boss = new Enemy("Shadow King", 250, 28, 12,
                    room.getX() + 180, room.getY() + 180, true);
                room.setEnemy(boss);
                continue;
            }

            double roll = rng.nextDouble();

            if (roll < 0.45)
            {
                room.setEnemy(randomEnemy(room));
            }
            else if (roll < 0.70)
            {
                Chest chest = new Chest(room.getX() + 160, room.getY() + 160);
                fillChest(chest, stoneIdx < STONE_COLORS.length ? STONE_COLORS[stoneIdx++] : null);
                room.setChest(chest);
            }
            else if (roll < 0.85 && shrineCount < shrineAbilities.length)
            {
                Shrine shrine = new Shrine(
                    room.getX() + 170, room.getY() + 170,
                    Ability.create(shrineAbilities[shrineCount++])
                );
                room.setShrine(shrine);
                shrines.add(shrine);
            }
        }

        // Make sure all 5 stones exist
        while (stoneIdx < STONE_COLORS.length)
        {
            for (Room r : rooms) 
            {
                if (!r.isBossRoom() && r.getChest() == null && rng.nextBoolean()) 
                {
                    Chest c = new Chest(r.getX() + 80, r.getY() + 80);
                    c.addItem(new Item("Stone of " + STONE_COLORS[stoneIdx], "stone", 0, STONE_COLORS[stoneIdx]));
                    r.setChest(c);
                    stoneIdx++;
                    break;
                }
            }
        }
    }

    /**
     * Creates a random enemy
     * The type of enemy is determined randomly, and its position is set within the given room's bounds
     * 
     * @param room the room in which to place the enemy
     * @return the created enemy
     */
    private Enemy randomEnemy(Room room)
    {
        int t = rng.nextInt(4);
        double ex = room.getX() + 100 + rng.nextInt(200);
        double ey = room.getY() + 100 + rng.nextInt(200);
        if (t == 0) return new Enemy("Ghost", 35, 12, 3, ex, ey, false);
        if (t == 1) return new Enemy("Skeleton", 45, 14, 5, ex, ey, false);
        if (t == 2) return new Enemy("Specter", 55, 18, 7, ex, ey, false);
        return       new Enemy("Vampire Bat", 30, 10, 2, ex, ey, false);
    }

    /**
     * Fills the given chest with a random item
     * If a stone color is provided, it adds the corresponding stone to the chest first, then adds a random item based on predefined probabilities
     * 
     * @param chest the chest to fill with items
     * @param stoneColor the color of the stone to add to the chest, or null if no stone should be added
     */
    private void fillChest(Chest chest, String stoneColor)
    {
        if (stoneColor != null) 
        {
            chest.addItem(new Item("Stone of " + stoneColor, "stone", 0, stoneColor));
        }

        int roll = rng.nextInt(4);

        if (roll == 0)
        {
            chest.addItem(new Item("Health Potion", "potion", 25 + rng.nextInt(36), ""));
        }
        else if (roll == 1)
        {
            chest.addItem(new Item("Iron Sword", "weapon", 4 + rng.nextInt(9), ""));
        }
        else if (roll == 2)
        {
            chest.addItem(new Item("Steel Axe", "weapon", 6 + rng.nextInt(10), ""));
        }
        else
        {
            chest.addItem(new Item("Stamina Potion", "stamina", 40, ""));
        }
    }

    public List<Room> getRooms() { return rooms; }
    public List<Shrine> getShrines() { return shrines; }
    public Room getCurrentRoom() { return currentRoom; }

    /**
     * Updates the current room based on the player's position
     * Checks which room contains the given coordinates and sets that room as the current room
     * 
     * @param px the x-coordinate of the player's position
     * @param py the y-coordinate of the player's position
     */
    public void updateCurrentRoom(double px, double py)
    {
        if (rooms == null) return;
        for (Room r : rooms)
        {
            if (r.contains(px, py)) 
            { 
                currentRoom = r; return; 
            }
        }
    }

    /**
     * Finds a room by its name
     * Iterates through the list of rooms and returns the one that matches the given name, or null if no such room exists
     * 
     * @param name the name of the room to find
     * @return the Room object with the matching name, or null if not found
     */
    public Room getRoomByName(String name)
    {
        if (rooms == null) return null;
        for (Room r : rooms) 
        {
            if (r.getName().equals(name)) return r;
        }
        return null;
    }
}