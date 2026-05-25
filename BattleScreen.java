import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.Random;

//Turn-based battle screen.
//Options are Attack, Ability, Use Item, Run
public class BattleScreen
{
    private Stage stage;
    private Character character;
    private Enemy enemy;
    private GameScreen gameScreen;
    private Random rng;

    private Label battleLog;
    private Label playerStatsLabel;
    private Label enemyStatsLabel;
    private ProgressBar playerHpBar;
    private ProgressBar enemyHpBar;
    private boolean playerShielded = false;

    private static final String CSS = BattleScreen.class.getResource("style.css").toExternalForm();

    /**
     * Constructor for the battle screen
     * @param stage the main stage to display on
     * @param character the player character
     * @param enemy the enemy
     * @param gameScreen the game screen to return to after battle
     */
    public BattleScreen(Stage stage, Character character, Enemy enemy, GameScreen gameScreen)
    {
        this.stage = stage;
        this.character = character;
        this.enemy = enemy;
        this.gameScreen = gameScreen;
        this.rng = new Random();
    }

    // Displays the battle screen and initializes all UI components
    public void show()
    {
        VBox root = new VBox(12);
        root.getStyleClass().add("battle-root");

        Label title = new Label("  Battle: " + enemy.getName());
        title.getStyleClass().add("battle-title");

        playerStatsLabel = new Label();
        playerStatsLabel.getStyleClass().add("battle-player-stats");

        playerHpBar = new ProgressBar();
        playerHpBar.getStyleClass().addAll("player-hp-bar");
        updatePlayerStats();

        enemyStatsLabel = new Label();
        enemyStatsLabel.getStyleClass().add("battle-enemy-stats");

        enemyHpBar = new ProgressBar();
        enemyHpBar.getStyleClass().add("enemy-hp-bar");
        updateEnemyStats();

        battleLog = new Label("Battle started! Defeat " + enemy.getName() + "!");
        battleLog.getStyleClass().add("battle-log");
        battleLog.setWrapText(true);
        battleLog.setPrefWidth(450);
        battleLog.setPrefHeight(90);

        // Action buttons for the player's turn
        Button attackBtn = makeBtn("Attack", () -> performAttack());
        Button abilityBtn = makeBtn("Ability", () -> showAbilityMenu());
        Button itemBtn = makeBtn("Use Item", () -> showItemMenu());
        Button runBtn = makeBtn("Run", () -> tryRun());

        attackBtn.getStyleClass().add("btn-action");
        abilityBtn.getStyleClass().add("btn-action");
        itemBtn.getStyleClass().add("btn-action");
        runBtn.getStyleClass().add("btn-action");

        HBox actions = new HBox(10, attackBtn, abilityBtn, itemBtn, runBtn);
        actions.getStyleClass().add("action-row");

        root.getChildren().addAll(title, playerStatsLabel, playerHpBar, enemyStatsLabel, enemyHpBar, battleLog, actions);

        Scene scene = new Scene(root, 800, 520);
        scene.getStylesheets().add(CSS);
        stage.setScene(scene);
    }

    // Attacks the enemy with the player's attack stat and updates the UI accordingly
    private void performAttack()
    {
        int dmg = calcDamage(character.getAttack(), enemy.getDefense());
        enemy.setHp(enemy.getHp() - dmg);
        log("You attacked for " + dmg + " damage!");
        updateEnemyStats();
        if (!checkEnemyDefeated()) enemyTurn();
    }

    // Displays the player's ability menu
    private void showAbilityMenu()
    {
        if (character.getAbilities().isEmpty()) 
        { 
            log("You have no abilities!"); return; 
        }

        Stage popup = new Stage();
        popup.setTitle("Choose Ability");
        VBox root = new VBox(8);
        root.getStyleClass().add("dark-popup");

        Label lbl = new Label("Choose an ability:");
        lbl.getStyleClass().add("popup-label");

        ListView<String> list = new ListView<>();
        list.setPrefHeight(180);
        for (Ability a : character.getAbilities())
        {
            list.getItems().add(a.toString() + (character.getStamina() < a.getStaminaCost() ? " [NOT ENOUGH STA]" : ""));
        }

        Button useBtn = new Button("Use");
        useBtn.getStyleClass().add("btn-small");
        useBtn.setOnAction(e -> {
            int idx = list.getSelectionModel().getSelectedIndex();
            if (idx < 0) return;
            Ability chosen = character.getAbilities().get(idx);

            if (character.getStamina() < chosen.getStaminaCost()) 
            {
                log("Not enough stamina for " + chosen.getName() + "!");
                popup.close();
                return;
            }

            character.setStamina(character.getStamina() - chosen.getStaminaCost());

            if (chosen.getHealAmount() > 0) 
            {
                character.setHp(character.getHp() + chosen.getHealAmount());
                log(chosen.getName() + ": healed " + chosen.getHealAmount() + " HP!");
                updatePlayerStats();
                popup.close();
                enemyTurn();
            } 
            else if (chosen.getName().equals("Block") || chosen.getName().equals("Shield")) 
            {
                playerShielded = true;
                log(chosen.getName() + ": you brace for the next hit!");
                popup.close();
                enemyTurn();
            } 
            else 
            {
                int dmg = calcDamage(character.getAttack() + chosen.getDamage(), enemy.getDefense());
                enemy.setHp(enemy.getHp() - dmg);
                log(chosen.getName() + " dealt " + dmg + " damage!");
                updateEnemyStats();
                popup.close();
                if (!checkEnemyDefeated()) enemyTurn();
            }
        });

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("btn-small");
        cancelBtn.setOnAction(e -> popup.close());

        HBox row = new HBox(10, useBtn, cancelBtn);
        row.getStyleClass().add("btn-row");
        root.getChildren().addAll(lbl, list, row);

        Scene scene = new Scene(root, 420, 300);
        scene.getStylesheets().add(CSS);
        popup.setScene(scene);
        popup.show();
    }

    //Displays the player's item menu
    private void showItemMenu()
    {
        boolean hasItem = false;
        for (Item it : character.getInventory().getItems())
        {
            if (it.getType().equals("potion") || it.getType().equals("stamina")) 
            { 
                hasItem = true; break; 
            }
        }

        if (!hasItem) 
        { 
            log("You have no items!"); 
            return; 
        }

        Stage popup = new Stage();
        popup.setTitle("Use Item");
        VBox root = new VBox(8);
        root.getStyleClass().add("dark-popup");

        Label lbl = new Label("Choose an item:");
        lbl.getStyleClass().add("popup-label");

        // List of available items to use in battle
        ListView<String> list = new ListView<>();
        list.setPrefHeight(160);
        for (int i = 0; i < character.getInventory().getSize(); i++) 
        {
            Item it = character.getInventory().getItem(i);
            if (it.getType().equals("potion") || it.getType().equals("stamina"))
            {
                list.getItems().add(it.toString());
            }
        }

        // Use button will apply the selected item and remove it from inventory
        Button useBtn = new Button("Use");
        useBtn.getStyleClass().add("btn-small");
        useBtn.setOnAction(e -> {
            int listIdx = list.getSelectionModel().getSelectedIndex();
            if (listIdx < 0) return;

            int found = 0;

            for (int i = 0; i < character.getInventory().getSize(); i++) 
            {
                Item it = character.getInventory().getItem(i);

                if (it.getType().equals("potion") || it.getType().equals("stamina")) 
                {
                    if (found == listIdx) 
                    {
                        if (it.getType().equals("stamina")) 
                        {
                            character.setStamina(character.getStamina() + it.getValue());
                            log("Used " + it.getName() + ", restored " + it.getValue() + " Stamina!");
                        } 
                        else 
                        {
                            character.setHp(character.getHp() + it.getValue());
                            log("Used " + it.getName() + ", healed " + it.getValue() + " HP!");
                        }
                        character.getInventory().removeItem(i);
                        updatePlayerStats();
                        popup.close();
                        enemyTurn();
                        return;
                    }
                    found++;
                }
            }
        });

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("btn-small");
        cancelBtn.setOnAction(e -> popup.close());

        HBox row = new HBox(10, useBtn, cancelBtn);
        row.getStyleClass().add("btn-row");
        root.getChildren().addAll(lbl, list, row);

        Scene scene = new Scene(root, 380, 280);
        scene.getStylesheets().add(CSS);
        popup.setScene(scene);
        popup.show();
    }

    //To escape the current battle
    //Chance depends on stamina, the higher the better
    private void tryRun()
    {
        int chance = 50 + rng.nextInt(31);
        if (rng.nextInt(100) < chance)
        {
            log("You escaped successfully!");
            delay(() -> {
                gameScreen.restorePlayerPosition();
                gameScreen.startGameLoop();
                gameScreen.show();
            });
        } 
        else
        {
            log("Couldn't escape!");
            enemyTurn();
        }
    }

    //Attacks the player with the enemy's attack stat
    private void enemyTurn()
    {
        int dmg = calcDamage(enemy.getAttack(), character.getDefense());

        if (playerShielded) 
        { 
            dmg = Math.max(1, dmg / 2); playerShielded = false; 
        }

        character.setHp(character.getHp() - dmg);
        log(enemy.getName() + " hit you for " + dmg + " damage!");
        updatePlayerStats();

        if (character.getHp() <= 0) 
        {
            log("You were defeated...");
            delay(() -> {
                character.setHp(character.getMaxHp() / 2);
                character.restoreStamina(30);
                gameScreen.restorePlayerPosition();
                gameScreen.startGameLoop();
                gameScreen.show();
            });
        }
    }

    //Checks if the enemy has been defeated
    private boolean checkEnemyDefeated()
    {
        if (!enemy.isDefeated()) return false;

        boolean levelled = character.addXp(enemy.getXpReward());
        final String msg = "Victory! +" + enemy.getXpReward() + " XP" + (levelled ? "  *** LEVEL UP! Now Lv." + character.getLevel() + " ***" : "");
        log(msg);

        character.setHp(character.getMaxHp());
        character.setStamina(character.getMaxStamina());
        log("After battle, your HP and Stamina are fully restored!");

        if (enemy.getDropItem() != null) 
        {
            Item drop = enemy.getDropItem();
            if (character.getInventory().addItem(drop)) log("Obtained: " + drop);

            else log("Inventory full – dropped " + drop.getName());
        }

        // Handle ability drops with special logic since they can't just be added to inventory
        if (enemy.getDropAbility() != null) 
        {
            Ability drop = enemy.getDropAbility();
            boolean alreadyHas = character.getAbilities().stream().anyMatch(a -> a.getName().equals(drop.getName()));

            if (alreadyHas) 
            {
                log("You already know " + drop.getName() + "!");
            } 
            else 
            {
                log("Learned ability: " + drop.getName() + "!");

                if (character.isAbilitiesFull()) 
                {
                    javafx.application.Platform.runLater(() ->
                        new AbilityBookUI(character).showSwapDialog(drop, () -> {
                            gameScreen.restorePlayerPosition();
                            gameScreen.startGameLoop();
                            gameScreen.show();
                        })
                    );
                    return true;
                } 
                else 
                {
                    character.addAbility(drop);
                }
            }
        }

        if (enemy.isBoss()) 
        {
            showBossVictory(msg);
        } 
        else 
        {
            showRegularVictory(msg);
        }
        return true;
    }

    //Displays a victory message for a boss battle
    private void showBossVictory(String msg)
    {
        javafx.application.Platform.runLater(() -> {
            VBox root = (VBox) battleLog.getParent();
            root.getChildren().removeIf(n -> n instanceof HBox && n != battleLog);

            Label bossMsg = new Label("YOU DEFEATED THE SHADOW KING!");
            bossMsg.getStyleClass().add("victory-label");

            Button continueBtn = new Button("CONTINUE EXPLORING");
            continueBtn.getStyleClass().add("btn-victory");
            continueBtn.setOnAction(e -> {
                character.setBossDefeated(true);
                gameScreen.restorePlayerPosition();
                gameScreen.startGameLoop();
                gameScreen.show();
            });

            Button endBtn = new Button("END GAME");
            endBtn.getStyleClass().add("btn-end");
            endBtn.setOnAction(e -> new StartScreen(stage).show());

            HBox row = new HBox(20, continueBtn, endBtn);
            row.getStyleClass().add("btn-row");
            root.getChildren().addAll(bossMsg, row);
            battleLog.setText(msg + "\n\n" + battleLog.getText());
        });
    }

    //Displays a victory message for a regular battle
    private void showRegularVictory(String msg)
    {
        javafx.application.Platform.runLater(() -> {
            VBox root = (VBox) battleLog.getParent();
            root.getChildren().removeIf(n -> n instanceof HBox && n != battleLog);

            Button continueBtn = new Button("CONTINUE EXPLORING");
            continueBtn.getStyleClass().add("btn-victory");
            continueBtn.setOnAction(e -> {
                gameScreen.restorePlayerPosition();
                gameScreen.startGameLoop();
                gameScreen.show();
            });

            HBox row = new HBox(continueBtn);
            row.getStyleClass().add("btn-row");
            row.setPadding(new Insets(20));
            root.getChildren().add(row);
            battleLog.setText(msg + "\n\n" + battleLog.getText());
        });
    }

    //Updates the player and enemy stats
    private void updatePlayerStats()
    {
        playerStatsLabel.setText(character.getName() + "  HP: " + character.getHp() + "/" + character.getMaxHp() + "  STA: " + character.getStamina() + "/" + character.getMaxStamina() + "  Lv." + character.getLevel());
        double ratio = (double) character.getHp() / character.getMaxHp();
        playerHpBar.setProgress(ratio);

        // Dynamically update HP bar colour via inline style only for the colour value
        String accent = ratio > 0.6 ? "#00cc44" : ratio > 0.3 ? "#ff8800" : "#cc0000";
        playerHpBar.setStyle("-fx-accent: " + accent + ";");
    }

    //Updates the enemy stats
    private void updateEnemyStats()
    {
        enemyStatsLabel.setText(enemy.getName() + "  HP: " + enemy.getHp() + "/" + enemy.getMaxHp());
        enemyHpBar.setProgress((double) enemy.getHp() / enemy.getMaxHp());
    }

    private int calcDamage(int atk, int def)
    {
        return Math.max(1, (atk - def / 2) + rng.nextInt(5) - 2);
    }

    private void log(String msg)
    {
        battleLog.setText(msg + "\n" + battleLog.getText());
    }

    private void delay(Runnable action)
    {
        new Thread(() -> {
            try { Thread.sleep(1800); } catch (InterruptedException ignored) {}
            javafx.application.Platform.runLater(action);
        }).start();
    }

    private Button makeBtn(String text, Runnable action)
    {
        Button b = new Button(text);
        b.setOnAction(e -> action.run());
        return b;
    }
}