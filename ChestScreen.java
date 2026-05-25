import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Screen for interacting with a chest
 * Displays the contents and enables the player to take the items
 */
public class ChestScreen
{
    private Stage stage;
    private Character character;
    private Chest chest;
    private GameScreen gameScreen;
    private Label invStatus;

    private static final String CSS = ChestScreen.class.getResource("style.css").toExternalForm();

    /**
     * A new chest interaction screen
     * 
     * @param stage the stage to display on
     * @param character the player character
     * @param chest the chest being interacted with
     * @param gameScreen the game screen to return to after closing the chest
     */
    public ChestScreen(Stage stage, Character character, Chest chest, GameScreen gameScreen)
    {
        this.stage = stage;
        this.character = character;
        this.chest = chest;
        this.gameScreen = gameScreen;
    }

    //Displays the chest contents and allows the player to take items or close the chest
    public void show()
    {
        VBox root = new VBox(12);
        root.getStyleClass().add("dark-popup");

        Label title = new Label("Chest Contents");
        title.getStyleClass().add("chest-title");

        invStatus = new Label();
        invStatus.getStyleClass().add("status-label");
        refreshStatus();

        Label chestLbl = new Label("Items in chest:");
        chestLbl.getStyleClass().add("section-label");

        ListView<String> chestList = new ListView<>();
        chestList.setPrefHeight(180);
        chest.getItems().forEach(i -> chestList.getItems().add(i.toString()));

        // Take button to take the selected item from the chest
        Button takeBtn = new Button("Take Selected");
        takeBtn.getStyleClass().add("btn-medium");
        takeBtn.setOnAction(e -> {
            int idx = chestList.getSelectionModel().getSelectedIndex();

            if (idx < 0) return;
            Item item = chest.getItem(idx);

            if (item == null) return;

            if (character.getInventory().isFull()) showSwapDialog(item);

            else takeItem(item, idx, chestList);
        });

        Button closeBtn = new Button("Close");
        closeBtn.getStyleClass().add("btn-medium");
        closeBtn.setOnAction(e -> {
            gameScreen.restorePlayerPosition();
            gameScreen.startGameLoop();
            gameScreen.show();
        });

        HBox btns = new HBox(15, takeBtn, closeBtn);
        btns.getStyleClass().add("btn-row");

        root.getChildren().addAll(title, invStatus, chestLbl, chestList, btns);

        Scene scene = new Scene(root, 800, 520);
        scene.getStylesheets().add(CSS);
        stage.setScene(scene);
    }

    /**
     * Takes an item from the chest and adds it into the player's inventory
     * While handling the special cases of full inventory and ability items
     * 
     * @param item the item to take
     * @param idx the index of the item in the chest 
     * @param chestList the list of items in the chest
     */
    private void takeItem(Item item, int idx, ListView<String> chestList)
    {
        chest.takeItem(idx);

        if (item.getType().equals("ability")) 
        {
            Ability a = Ability.create(item.getName());
            boolean alreadyHas = character.getAbilities().stream().anyMatch(ex -> ex.getName().equals(a.getName()));

            if (alreadyHas) 
            {
                showAlert("You already know " + a.getName() + "!");
                chest.addItem(item);
                refreshStatus();
                return;
            }
            if (character.isAbilitiesFull()) new AbilityBookUI(character).showSwapDialog(a, this::show);

            else character.addAbility(a);
        } 
        else 
        {
            character.getInventory().addItem(item);

            if (item.getType().equals("stone")) character.addStone();
        }

        chestList.getItems().remove(idx);
        refreshStatus();
    }

    /**
     * Displays a dialog to select an item to drop if the player's inventory is full
     * The dropped item is added into the chest and the new item is added to the player's inventory
     * 
     * @param newItem the item the player wants to take that triggered the full inventory condition
     */
    private void showSwapDialog(Item newItem)
    {
        Stage popup = new Stage();
        popup.setTitle("Inventory Full - Swap?");
        VBox root = new VBox(10);
        root.getStyleClass().add("dark-popup");

        Label lbl = new Label("Inventory full. Select item to drop:");
        lbl.getStyleClass().add("section-label");

        Label newLbl = new Label("New: " + newItem);
        newLbl.getStyleClass().add("new-item-label");

        ListView<String> invList = new ListView<>();
        invList.setPrefHeight(180);
        character.getInventory().getItems().forEach(i -> invList.getItems().add(i.toString()));

        // Swap button to swap the selected item with the new item
        Button swapBtn = new Button("Swap");
        swapBtn.getStyleClass().add("btn-small");
        swapBtn.setOnAction(e -> {
            int idx = invList.getSelectionModel().getSelectedIndex();
            if (idx < 0) return;

            // Remove new item from chest first
            for (int i = 0; i < chest.getItems().size(); i++)
            {
                if (chest.getItem(i) == newItem) 
                { 
                    chest.takeItem(i); break; 
                }
            }

            Item dropped = character.getInventory().swapItem(idx, newItem);
            chest.addItem(dropped);
            if (newItem.getType().equals("stone")) character.addStone();

            if (newItem.getType().equals("ability")) 
            {
                Ability a = Ability.create(newItem.getName());
                if (character.isAbilitiesFull()) 
                { 
                    new AbilityBookUI(character).showSwapDialog(a, this::show);
                }
                else character.addAbility(a);
            }
            popup.close();
            show();
        });

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("btn-small");
        cancelBtn.setOnAction(e -> popup.close());

        HBox row = new HBox(10, swapBtn, cancelBtn);
        row.getStyleClass().add("btn-row");
        root.getChildren().addAll(lbl, newLbl, invList, row);

        Scene scene = new Scene(root, 420, 330);
        scene.getStylesheets().add(CSS);
        popup.setScene(scene);
        popup.show();
    }

    private void refreshStatus()
    {
        invStatus.setText("Inventory: " + character.getInventory().getSize()
            + "/" + character.getInventory().getCapacity());
    }

    private void showAlert(String msg)
    {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setContentText(msg);
        a.showAndWait();
    }
}