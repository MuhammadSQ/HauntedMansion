import javafx.animation.AnimationTimer;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.*;
import java.util.*;

/**
 * Main game screen: free movement, camera, HUD, interactions.
 * This class handles the core gameplay loop, rendering the world, and managing player interactions with the environment.
 */
public class GameScreen
{
    private Stage stage;
    private Character character;
    private GameWorld world;

    // Game elements
    private Pane gamePane;
    private Rectangle playerRect;
    private Scene scene;
    private List<Rectangle> walls;
    private double savedPlayerX;
    private double savedPlayerY;

    private boolean up, down, left, right;

    private long lastUpdate = 0;
    private AnimationTimer timer;

    // Constants
    private static final double PLAYER_SIZE = 30;
    private static final double PLAYER_SPEED = 250;
    private static final int WIN_W = 1000;
    private static final int WIN_H = 700;
    private static final double MAP_W = 4000;
    private static final double MAP_H = 4000;
    private static final double INTERACT_DIST = 50;
    private static final String SAVE_FOLDER = "saves";

    private static final String CSS = GameScreen.class.getResource("style.css").toExternalForm();

    // HUD elements
    private Label hudName;
    private Label hudLevel;
    private Label hudStones;
    private Label hudWeapon;
    private Label hudRoom;
    private ProgressBar hpBar;
    private ProgressBar staBar;
    private Label hpLabel;
    private Label staLabel;
    private Label interactHint;

    // Nearby objects
    private Enemy nearEnemy;
    private Chest nearChest;
    private Shrine nearShrine;
    private boolean bossDoorNear = false;

    /**
     * Creates a new game screen with a fresh game world
     * 
     * @param stage the stage to display on
     * @param character the player character to use in the game
     */
    public GameScreen(Stage stage, Character character)
    {
        this.stage = stage;
        this.character = character;
        this.world = new GameWorld();
        this.savedPlayerX = character.getX();
        this.savedPlayerY = character.getY();
    }

    /**
     * Creates a game with an existing game world
     * 
     * @param stage the stage to display on
     * @param character the player character 
     * @param world the game world to load
     */
    public GameScreen(Stage stage, Character character, GameWorld world)
    {
        this.stage = stage;
        this.character = character;
        this.world = world;
    }

    // Displays the game screen and starts the game loop
    public void show()
    {
        try 
        {
            gamePane = new Pane();
            gamePane.setPrefSize(MAP_W, MAP_H);
            gamePane.setStyle("-fx-background-color: #2d2d3a;");

            drawWorld();
            buildWalls();

            playerRect = new Rectangle(PLAYER_SIZE, PLAYER_SIZE);
            playerRect.setFill(Color.web(colorHex(character.getColor())));

            if (character.getX() == 0 && character.getY() == 0) 
            {
                Room first = world.getRooms().get(0);
                double sx = clamp(first.getX() + first.getWidth() / 2 - PLAYER_SIZE / 2,
                    first.getX() + 10, first.getX() + first.getWidth() - PLAYER_SIZE - 10);
                double sy = clamp(first.getY() + first.getHeight() / 2 - PLAYER_SIZE / 2,
                    first.getY() + 10, first.getY() + first.getHeight() - PLAYER_SIZE - 10);
                playerRect.setX(sx); playerRect.setY(sy);
                character.setX(sx);  character.setY(sy);
            } 
            else 
            {
                playerRect.setX(character.getX());
                playerRect.setY(character.getY());
            }

            savedPlayerX = playerRect.getX();
            savedPlayerY = playerRect.getY();

            if (!isValidPosition(playerRect.getX(), playerRect.getY())) 
            {
                Room first = world.getRooms().get(0);
                playerRect.setX(first.getX() + 50);
                playerRect.setY(first.getY() + 50);
                character.setX(first.getX() + 50);
                character.setY(first.getY() + 50);
            }

            gamePane.getChildren().add(playerRect);

            VBox hud = buildHUD();
            VBox legend = buildControlsLegend();

            StackPane mapLayer = new StackPane();
            StackPane.setAlignment(legend, Pos.TOP_RIGHT);
            mapLayer.getChildren().addAll(gamePane, legend);

            BorderPane mainLayout = new BorderPane();
            mainLayout.setCenter(mapLayer);
            mainLayout.setBottom(hud);

            scene = new Scene(mainLayout, WIN_W, WIN_H);
            scene.getStylesheets().add(CSS);
            setupInput();
            startGameLoop();

            stage.setScene(scene);
            stage.setTitle("Haunted Mansion Explorer");
            stage.show();
            gamePane.requestFocus();

        } 
        catch (Exception e) 
        {
            e.printStackTrace();
            Label err = new Label("Game failed to start!\n" + e.getMessage());
            err.setTextFill(Color.RED);
            err.setWrapText(true);
            VBox box = new VBox(err);
            box.setAlignment(Pos.CENTER);
            box.getStyleClass().add("screen-root");
            Scene errScene = new Scene(box, 500, 300);
            errScene.getStylesheets().add(CSS);
            stage.setScene(errScene);
            stage.show();
        }
    }

    //Draws the entire game world
    //Background grid, rooms, chests, shrines
    private void drawWorld()
    {
        Rectangle worldBackground = new Rectangle(0, 0, MAP_W, MAP_H);
        worldBackground.setFill(Color.BLACK);
        gamePane.getChildren().add(worldBackground);

        for (Room room : world.getRooms()) 
        {
            Rectangle floor = new Rectangle(room.getX(), room.getY(), room.getWidth(), room.getHeight());
            if (room.isBossRoom()) 
            {
                floor.setFill(Color.color(0.25, 0.05, 0.05));  // Dark red-black for boss room
            } 
            else 
            {
                floor.setFill(Color.color(0.12, 0.12, 0.15)); 
            }
            floor.setStroke(Color.color(0.25, 0.25, 0.3));      
            floor.setStrokeWidth(1);
            gamePane.getChildren().add(floor);

            javafx.scene.text.Text lbl = new javafx.scene.text.Text(room.getX() + 8, room.getY() + 22, room.getName());
            lbl.setFill(Color.color(0.5, 0.5, 0.55));           
            lbl.setFont(Font.font("Arial", 11));
            gamePane.getChildren().add(lbl);

            double doorW = 70;
            double doorX = room.getX() + room.getWidth() / 2 - doorW / 2;
            Rectangle door = new Rectangle(doorX, room.getY() + room.getHeight() - 30, doorW, 35);
            door.setFill(Color.color(0.3, 0.2, 0.1));           
            door.setStroke(Color.color(0.4, 0.3, 0.2));
            door.setStrokeWidth(1);
            gamePane.getChildren().add(door);

            // Door label
            javafx.scene.text.Text doorLbl = new javafx.scene.text.Text(doorX + 10, room.getY() + room.getHeight() - 10, "DOOR");
            doorLbl.setFill(Color.color(0.6, 0.5, 0.2));         
            doorLbl.setFont(Font.font("Arial", 9));
            gamePane.getChildren().add(doorLbl);

            if (room.getEnemy() != null && !room.getEnemy().isDefeated()) 
            {
                Rectangle enemyRect = new Rectangle(26, 26);
                enemyRect.setFill(Color.color(0.6, 0.1, 0.1));   
                enemyRect.setStroke(Color.color(0.8, 0.2, 0.2));
                enemyRect.setStrokeWidth(1);
                enemyRect.setX(room.getEnemy().getX());
                enemyRect.setY(room.getEnemy().getY());
                gamePane.getChildren().add(enemyRect);
            }

            if (room.getChest() != null && !room.getChest().isEmpty()) 
            {
                Rectangle chestRect = new Rectangle(25, 25);
                chestRect.setFill(Color.color(0.5, 0.4, 0.1));    
                chestRect.setStroke(Color.color(0.7, 0.6, 0.2));
                chestRect.setStrokeWidth(1);
                chestRect.setX(room.getChest().getX());
                chestRect.setY(room.getChest().getY());
                gamePane.getChildren().add(chestRect);
            }

            if (room.getShrine() != null && !room.getShrine().isUsed()) 
            {
                Rectangle shrineRect = new Rectangle(25, 25);
                shrineRect.setFill(Color.color(0.3, 0.1, 0.4));    
                shrineRect.setStroke(Color.color(0.5, 0.2, 0.7));
                shrineRect.setStrokeWidth(1);
                shrineRect.setX(room.getShrine().getX());
                shrineRect.setY(room.getShrine().getY());
                gamePane.getChildren().add(shrineRect);
            }

            if (room.isFinalRoom()) 
            {
                Rectangle bossDoor = new Rectangle(50, 70);
                bossDoor.setFill(Color.color(0.3, 0.0, 0.0));      
                bossDoor.setStroke(Color.color(0.7, 0.0, 0.0));
                bossDoor.setStrokeWidth(2);
                bossDoor.setX(room.getX() + room.getWidth() / 2 - 25);
                bossDoor.setY(room.getY() + room.getHeight() / 2 - 35);
                gamePane.getChildren().add(bossDoor);

                javafx.scene.text.Text bossLbl = new javafx.scene.text.Text(
                    room.getX() + room.getWidth() / 2 - 28,
                    room.getY() + room.getHeight() / 2 + 45, 
                    "BOSS DOOR");
                bossLbl.setFill(Color.RED);
                bossLbl.setFont(Font.font("Arial", 9));
                gamePane.getChildren().add(bossLbl);
            }
        }
    }

    //Builds the HUD
    //Displays player name, level, equipped weapon, current room, HP and stamina bars, and interact hints
    private VBox buildHUD()
    {
        VBox hud = new VBox(3);
        hud.getStyleClass().add("hud-root");

        hudName = new Label(character.getName());
        hudName.getStyleClass().add("hud-label");
        hudName.setTextFill(Color.web(colorHex(character.getColor())));

        hudLevel  = makeHudLabel("Lv.1");
        hudStones = makeHudLabel("Stones: 0/5");
        hudWeapon = makeHudLabel("Weapon: none");
        hudRoom   = makeHudLabel("Room: ?");

        interactHint = new Label("");
        interactHint.getStyleClass().add("hud-interact-hint");

        HBox row1 = new HBox(20, hudName, hudLevel, hudStones, hudWeapon, hudRoom);
        row1.getStyleClass().add("hud-row");

        hpLabel  = makeHudLabel("HP");
        staLabel = makeHudLabel("STA");

        hpBar = new ProgressBar(1.0);
        hpBar.getStyleClass().add("hp-bar");

        staBar = new ProgressBar(1.0);
        staBar.getStyleClass().add("sta-bar");

        HBox row2 = new HBox(8, hpLabel, hpBar, staLabel, staBar, interactHint);
        row2.getStyleClass().add("hud-bar-row");

        hud.getChildren().addAll(row1, row2);
        return hud;
    }

    //Builds the controls legend
    private VBox buildControlsLegend()
    {
        VBox legend = new VBox(5);
        legend.getStyleClass().add("controls-legend");
        legend.setMaxWidth(180);

        Label title = new Label("CONTROLS");
        title.getStyleClass().add("controls-title");

        String[] lines = {
            "WASD / Arrows = Move",
            "SPACE = Interact",
            "I = Inventory",
            "K = Abilities",
            "R = Save Game",
            "ESC = Pause Menu"
        };
        legend.getChildren().add(title);
        for (String line : lines) 
        {
            Label l = new Label(line);
            l.getStyleClass().add("controls-item");
            legend.getChildren().add(l);
        }
        return legend;
    }
 
    private Label makeHudLabel(String text)
    {
        Label l = new Label(text);
        l.getStyleClass().add("hud-label");
        return l;
    }

    //Updates the HUD with current player stats and nearby interact hints
    private void updateHUD()
    {
        hudLevel.setText("Lv." + character.getLevel() + "  XP:" + character.getXp() + "/" + (character.getLevel() * 100));
        hudStones.setText("Stones: " + character.getStonesCollected() + "/5");
        Item w = character.getEquippedWeapon();
        hudWeapon.setText("Weapon: " + (w == null ? "none" : w.getName() + " (+" + w.getValue() + ")"));
        hudRoom.setText("Room: " + world.getCurrentRoom().getName());

        double hpRatio = (double) character.getHp() / character.getMaxHp();
        hpLabel.setText("HP " + character.getHp() + "/" + character.getMaxHp());
        hpBar.setProgress(hpRatio);
        String hpAccent = hpRatio > 0.6 ? "#00cc44" : hpRatio > 0.3 ? "#ff8800" : "#cc0000";
        hpBar.setStyle("-fx-accent: " + hpAccent + ";");

        staLabel.setText("STA " + character.getStamina() + "/" + character.getMaxStamina());
        staBar.setProgress((double) character.getStamina() / character.getMaxStamina());

        String hint = "";
        if (nearEnemy  != null) hint = "SPACE Fight " + nearEnemy.getName();
        else if (nearChest  != null) hint = "SPACE Open Chest";
        else if (nearShrine != null) hint = "SPACE Use Shrine";
        else if (bossDoorNear)
        {
            hint = character.hasAllStones() ? "SPACE Enter Boss Chamber" : "Boss Door Locked (need 5 stones)";
        }
        interactHint.setText(hint);
    }

    //Sets up keyboard input handlers for movement, interactions, inventory, and other actions
    private void setupInput()
    {
        scene.setOnKeyPressed(e -> {
            KeyCode k = e.getCode();
            if (k == KeyCode.W || k == KeyCode.UP) up = true;
            if (k == KeyCode.A || k == KeyCode.LEFT) left = true;
            if (k == KeyCode.S || k == KeyCode.DOWN) down = true;
            if (k == KeyCode.D || k == KeyCode.RIGHT) right = true;
            if (k == KeyCode.SPACE) interact();
            if (k == KeyCode.I) showInventory();
            if (k == KeyCode.K) new AbilityBookUI(character).show();
            if (k == KeyCode.R) saveGame();
            if (k == KeyCode.ESCAPE) showPauseMenu();
        });
        scene.setOnKeyReleased(e -> {
            KeyCode k = e.getCode();
            if (k == KeyCode.W || k == KeyCode.UP) up = false;
            if (k == KeyCode.A || k == KeyCode.LEFT) left = false;
            if (k == KeyCode.S || k == KeyCode.DOWN) down = false;
            if (k == KeyCode.D || k == KeyCode.RIGHT) right = false;
        });
    }

    //Starts the game loop which updates the game state and renders at 60 FPS
    public void startGameLoop()
    {
        if (timer != null) timer.stop();
        lastUpdate = 0;
        timer = new AnimationTimer() {
            @Override public void handle(long now) {
                if (lastUpdate == 0) { lastUpdate = now; return; }
                update((now - lastUpdate) / 1_000_000_000.0);
                lastUpdate = now;
            }
        };
        timer.start();
    }

    /**
     * Updates the game state
     * 
     * @param delta Time since last update
     */
    private void update(double delta)
    {
        double dx = 0, dy = 0;
        if (up) dy -= PLAYER_SPEED * delta;
        if (down) dy += PLAYER_SPEED * delta;
        if (left) dx -= PLAYER_SPEED * delta;
        if (right) dx += PLAYER_SPEED * delta;

        double newX = clamp(playerRect.getX() + dx, 0, MAP_W - PLAYER_SIZE);
        double newY = clamp(playerRect.getY() + dy, 0, MAP_H - PLAYER_SIZE);

        Rectangle test = new Rectangle(newX, newY, PLAYER_SIZE, PLAYER_SIZE);
        boolean hitWall = walls.stream().anyMatch(w -> test.getBoundsInParent().intersects(w.getBoundsInParent()));

        if (!hitWall) 
        {
            playerRect.setX(newX); playerRect.setY(newY);
            character.setX(newX);  character.setY(newY);
        }

        double camX = clamp(playerRect.getX() - WIN_W / 2.0 + PLAYER_SIZE / 2.0, 0, MAP_W - WIN_W);
        double camY = clamp(playerRect.getY() - (WIN_H - 110) / 2.0 + PLAYER_SIZE / 2.0, 0, MAP_H - (WIN_H - 110));
        gamePane.setTranslateX(-camX);
        gamePane.setTranslateY(-camY);

        world.updateCurrentRoom(playerRect.getX() + PLAYER_SIZE / 2, playerRect.getY() + PLAYER_SIZE / 2);
        scanNearby(playerRect.getX() + PLAYER_SIZE / 2.0, playerRect.getY() + PLAYER_SIZE / 2.0);
        updateHUD();
    }

    /**
     * Scans for nearby enemies, chests, shrines, and boss door
     * 
     * @param cx the x-coordinate of the player's center position
     * @param cy the y-coordinate of the player's center position
     */
    private void scanNearby(double cx, double cy)
    {
        nearEnemy = null; nearChest = null; nearShrine = null; bossDoorNear = false;
        for (Room room : world.getRooms()) 
        {
            if (room.getEnemy() != null && !room.getEnemy().isDefeated())
            {
                if (dist(cx, cy, room.getEnemy().getX(), room.getEnemy().getY()) < INTERACT_DIST)
                {
                    nearEnemy = room.getEnemy();
                }
            }
            if (room.getChest() != null && !room.getChest().isEmpty())
            {
                if (dist(cx, cy, room.getChest().getX(), room.getChest().getY()) < INTERACT_DIST)
                {
                    nearChest = room.getChest();
                }
            }
            if (room.getShrine() != null && !room.getShrine().isUsed())
            {
                if (dist(cx, cy, room.getShrine().getX(), room.getShrine().getY()) < INTERACT_DIST)
                {
                    nearShrine = room.getShrine();
                }
            }
            if (room.isFinalRoom()) 
            {
                double doorX = room.getX() + room.getWidth() / 2.0;
                double doorY = room.getY() + room.getHeight() / 2.0;
                if (dist(cx, cy, doorX, doorY) < 70) bossDoorNear = true;
            }
        }
    }

    private void savePlayerPosition()
    {
        savedPlayerX = playerRect.getX();
        savedPlayerY = playerRect.getY();
    }

    public void restorePlayerPosition()
    {
        if (playerRect != null) 
        {
            playerRect.setX(savedPlayerX);
            playerRect.setY(savedPlayerY);
            character.setX(savedPlayerX);
            character.setY(savedPlayerY);
            playerRect.setVisible(true);
        }
    }

    //Handles player interactions
    private void interact()
    {
        if (nearEnemy != null) 
        {
            savePlayerPosition(); stopLoop();
            new BattleScreen(stage, character, nearEnemy, this).show();
        } 
        else if (nearChest != null) 
        {
            savePlayerPosition(); stopLoop();
            new ChestScreen(stage, character, nearChest, this).show();
        } 
        else if (nearShrine != null) 
        {
            savePlayerPosition();
            useShrine(nearShrine);
        } 
        else if (bossDoorNear) 
        {
            if (character.hasAllStones()) 
            {
                Room bossRoom = world.getRoomByName("BOSS CHAMBER");
                if (bossRoom != null && bossRoom.getEnemy() != null && !bossRoom.getEnemy().isDefeated()) 
                {
                    savePlayerPosition(); stopLoop();
                    new BattleScreen(stage, character, bossRoom.getEnemy(), this).show();
                } 
                else 
                {
                    showAlert("The Boss has already been defeated!");
                }
            } 
            else 
            {
                showAlert("The door is locked! Collect all 5 colored stones first.\n"
                    + "You have: " + character.getStonesCollected() + "/5");
            }
        }
    }

    /**
     * Grants the player a new ability if they don't already have it
     * 
     * @param shrine the shrine being used
     */
    private void useShrine(Shrine shrine)
    {
        Ability ability = shrine.getAbility();
        boolean alreadyHas = character.getAbilities().stream().anyMatch(a -> a.getName().equals(ability.getName()));
        shrine.setUsed(true);
        redrawWorld();

        if (alreadyHas) 
        {
            showAlert("You already know " + ability.getName() + "! The shrine has no effect.");
            startGameLoop();
            return;
        }
        if (character.isAbilitiesFull()) 
        {
            new AbilityBookUI(character).showSwapDialog(ability, () -> {
                showAlert("Shrine power absorbed!");
                startGameLoop();
            });
        } 
        else 
        {
            character.addAbility(ability);
            showAlert("Shrine grants you: " + ability.getName() + "!\n" + ability.getDescription());
            startGameLoop();
        }
    }

    //Stops the game loop used when entering battles, chests, or pause menu
    public void stopLoop()
    {
        if (timer != null) timer.stop();
    }

    // Displays the inventory UI where players can view, equip, or drop items
    private void showInventory()
    {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Inventory");

        VBox root = new VBox(10);
        root.getStyleClass().add("inventory-root");

        Label title = new Label("Backpack (" + character.getInventory().getSize() + "/" + character.getInventory().getCapacity() + ")");
        title.getStyleClass().add("inventory-title");

        Label equippedLbl = new Label("EQUIPPED: " + character.getWeaponInfo());
        equippedLbl.getStyleClass().add("equipped-label");

        ListView<String> list = new ListView<>();
        list.setPrefHeight(240);
        refreshInvList(list);

        Button equipBtn = new Button("Equip Weapon");
        equipBtn.getStyleClass().add("btn-equip");
        equipBtn.setVisible(false);

        Button dropBtn = new Button("Drop Item");
        dropBtn.getStyleClass().add("btn-drop");
        dropBtn.setVisible(false);

        list.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel == null || sel.equals("(empty)")) 
            {
                equipBtn.setVisible(false); dropBtn.setVisible(false); return;
            }

            int idx = list.getSelectionModel().getSelectedIndex();
            Item item = character.getInventory().getItem(idx);

            if (item != null) 
            {
                dropBtn.setVisible(true);
                equipBtn.setVisible(item.getType().equals("weapon"));
            }
        });

        equipBtn.setOnAction(e -> {
            int idx = list.getSelectionModel().getSelectedIndex();
            if (idx < 0) return;
            Item item = character.getInventory().getItem(idx);

            if (item != null && item.getType().equals("weapon")) 
            {
                if (character.getEquippedWeapon() != null) character.unequipWeapon();
                character.equipWeapon(item);
                showAlert("Equipped " + item.getName() + "! ATK +" + item.getValue());
                equippedLbl.setText("EQUIPPED: " + character.getWeaponInfo());
                refreshInvList(list);
                updateHUD();
                equipBtn.setVisible(false); dropBtn.setVisible(false);
            }
        });

        dropBtn.setOnAction(e -> {
            int idx = list.getSelectionModel().getSelectedIndex();
            if (idx < 0) return;
            Item item = character.getInventory().getItem(idx);
            if (item != null) 
            {
                if (item.isEquipped()) character.unequipWeapon();
                character.getInventory().removeItem(idx);
                refreshInvList(list);
                updateHUD();
                equippedLbl.setText("EQUIPPED: " + character.getWeaponInfo());
                showAlert("Dropped " + item.getName());
                equipBtn.setVisible(false); dropBtn.setVisible(false);
            }
        });

        Button closeBtn = new Button("Close");
        closeBtn.getStyleClass().add("btn-small");
        closeBtn.setOnAction(e -> popup.close());

        HBox btnRow = new HBox(15, equipBtn, dropBtn, closeBtn);
        btnRow.getStyleClass().add("btn-row");

        root.getChildren().addAll(title, equippedLbl, list, btnRow);

        Scene scene = new Scene(root, 550, 420);
        scene.getStylesheets().add(CSS);
        popup.setScene(scene);
        popup.show();
    }

    /**
     * Refreshes the inventory list view with current items and equipped status
     * 
     * @param list the list view
     */
    private void refreshInvList(ListView<String> list)
    {
        list.getItems().clear();
        if (character.getInventory().getItems().isEmpty()) 
        {
            list.getItems().add("(empty)");
        } 
        else 
        {
            for (Item i : character.getInventory().getItems()) 
            {
                String display = i.toString();
                if (character.getEquippedWeapon() == i) display += " [EQUIPPED]";
                list.getItems().add(display);
            }
        }
    }

    // Displays the pause menu
    private void showPauseMenu()
    {
        if (timer != null) timer.stop();
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Paused");

        VBox root = new VBox(15);
        root.getStyleClass().add("pause-root");

        Label title = new Label("PAUSED");
        title.getStyleClass().add("pause-title");

        Button resumeBtn = makeMenuBtn("Resume", () -> { popup.close(); startGameLoop(); });
        Button saveBtn   = makeMenuBtn("Save Game", () -> {
            character.setX(playerRect.getX()); character.setY(playerRect.getY());
            saveGame(); popup.close(); startGameLoop();
        });
        Button menuBtn = makeMenuBtn("Main Menu", () -> {
            saveGame(); popup.close();
            new StartScreen(stage).show();
        });

        root.getChildren().addAll(title, resumeBtn, saveBtn, menuBtn);

        Scene scene = new Scene(root, 400, 320);
        scene.getStylesheets().add(CSS);
        popup.setScene(scene);
        popup.show();
    }

    // Saves the game
    private void saveGame()
    {
        try 
        {
            character.setX(playerRect.getX());
            character.setY(playerRect.getY());
            new File(SAVE_FOLDER).mkdirs();
            ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(new File(SAVE_FOLDER, character.getName() + ".dat")));
            oos.writeObject(character);
            oos.close();
            showAlert("Game saved!");
        } 
        catch (IOException ex) 
        {
            ex.printStackTrace();
            showAlert("Save failed: " + ex.getMessage());
        }
    }

    // Redraws the game world
    public void redrawWorld()
    {
        gamePane.getChildren().clear();
        drawWorld();
        buildWalls();
        playerRect = new Rectangle(PLAYER_SIZE, PLAYER_SIZE);
        playerRect.setFill(Color.web(colorHex(character.getColor())));
        playerRect.setX(character.getX());
        playerRect.setY(character.getY());
        playerRect.setVisible(true);
        playerRect.toFront();
        gamePane.getChildren().add(playerRect);
    }

    // Builds the walls for collision detection based on the rooms in the world
    private void buildWalls()
    {
        walls = new ArrayList<>();
        for (Room room : world.getRooms()) {
            double rx = room.getX(), ry = room.getY(), rw = room.getWidth(), rh = room.getHeight();
            double doorW = 70, doorX = rx + rw / 2 - doorW / 2;

            addWall(rx, ry, rw, 5);                                    // top
            addWall(rx, ry + rh - 5, doorX - rx, 5);                  // bottom-left
            addWall(doorX + doorW, ry + rh - 5, rx + rw - doorX - doorW, 5); // bottom-right
            addWall(rx, ry, 5, rh);                                    // left
            addWall(rx + rw - 5, ry, 5, rh);                          // right
        }
    }

    /**
     * Adds a wall to the game world
     * 
     * @param x the x-coordinate of the wall
     * @param y the y-coordinate of the wall
     * @param w the width of the wall
     * @param h the height of the wall
     */
    private void addWall(double x, double y, double w, double h)
    {
        Rectangle wall = new Rectangle(x, y, w, h);
        wall.setFill(Color.color(0.3, 0.25, 0.2));
        gamePane.getChildren().add(wall);
        walls.add(wall);
    }

    /**
     * Checks if a position is valid
     * A position is valid if it does not intersect with any walls
     * This is used to prevent the player from spawning inside walls or moving through them
     * 
     * @param x the x-coordinate of the position to check
     * @param y the y-coordinate of the position to check
     * @return true if the position is valid, false otherwise
     */
    private boolean isValidPosition(double x, double y)
    {
        Rectangle test = new Rectangle(x, y, PLAYER_SIZE, PLAYER_SIZE);
        return walls.stream().noneMatch(w -> test.getBoundsInParent().intersects(w.getBoundsInParent()));
    }

    /**
     * Calculates the distance between two points
     * Used to determine if the player is close enough to interact with enemies, chests, shrines, or the boss door
     * 
     * @param x1 the x-coordinate of the first point
     * @param y1 the y-coordinate of the first point
     * @param x2 the x-coordinate of the second point
     * @param y2 the y-coordinate of the second point
     * @return the distance between the two points
     */
    private double dist(double x1, double y1, double x2, double y2)
    {
        return Math.sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1));
    }

    /**
     * Clamps a value between a minimum and maximum
     * Used to keep the player within the bounds of the map and to prevent the camera from showing areas outside the map
     * 
     * @param v the value to clamp
     * @param lo the minimum value
     * @param hi the maximum value
     * @return the clamped value
     */
    private double clamp(double v, double lo, double hi)
    {
        return Math.max(lo, Math.min(hi, v));
    }

    /**
     * Converts a color name to a hex code
     * Used to set the player's color based on their character selection
     * 
     * @param name the name of the color to convert
     * @return the hex code for the specified color
     */
    private String colorHex(String name)
    {
        switch (name.toUpperCase()) {
            case "BLUE": return "0066FF";
            case "RED": return "FF3333";
            case "GREEN": return "00DD44";
            case "YELLOW": return "FFEE00";
            case "PURPLE": return "BB00FF";
            case "ORANGE": return "FF8800";
            default: return "0066FF";
        }
    }

    /**
     * Displays an alert message
     * Used for various notifications such as saving the game, using shrines, or interaction hints
     * 
     * @param msg the message to display in the alert
     */
    private void showAlert(String msg)
    {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Notice");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    /**
     * Creates a styled button for the pause menu
     * This method is used to create buttons with consistent styling and behavior for the pause menu options
     * 
     * @param text the text to display on the button
     * @param action the action to perform when the button is clicked
     * @return a styled Button instance with the specified text and action
     */
    private Button makeMenuBtn(String text, Runnable action)
    {
        Button b = new Button(text);
        b.getStyleClass().add("btn-menu");
        b.setOnAction(e -> action.run());
        return b;
    }

    public GameWorld getWorld() { return world; }
}