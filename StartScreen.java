import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.*;
import java.util.Arrays;
import java.util.List;

//Start screen: new game or load game
public class StartScreen
{
    private Stage stage;
    private Character character;
    private static final String SAVE_FOLDER = "saves";
    private static final String CSS = StartScreen.class.getResource("style.css").toExternalForm();

    /**
     * Constructor
     * 
     * @param stage Stage to display the start screen on
     */
    public StartScreen(Stage stage)
    {
        this.stage = stage;
        new File(SAVE_FOLDER).mkdirs();
    }

    // Displays the main start screen with options to start a new game or load an existing game
    public void show()
    {
        VBox root = new VBox(20);
        root.getStyleClass().add("screen-root");

        Label title = new Label("Haunted Mansion Explorer");
        title.getStyleClass().add("title-label");

        Label sub = new Label("Explore this haunted mansion, collect 5 stones, and defeat the Shadow King.");
        sub.getStyleClass().add("subtitle-label");

        Button newBtn  = makeBtn("New Game",  () -> showCharacterCreation());
        Button loadBtn = makeBtn("Load Game", () -> showLoadScreen());
        newBtn.getStyleClass().add("btn-large");
        loadBtn.getStyleClass().add("btn-large");

        root.getChildren().addAll(title, sub, newBtn, loadBtn);

        Scene scene = new Scene(root, 800, 500);
        scene.getStylesheets().add(CSS);
        stage.setScene(scene);
        stage.setTitle("Haunted Mansion Explorer");
        stage.show();
    }

    // Shows the character creation screen where the player can enter their name, choose a color, and select starting abilities
    private void showCharacterCreation()
    {
        VBox root = new VBox(14);
        root.getStyleClass().add("screen-root-top");

        Label title = new Label("Create Your Character");
        title.getStyleClass().add("screen-title");

        Label nameLbl = makeSectionLabel("Character Name:");
        TextField nameField = new TextField();
        nameField.setPromptText("Enter name...");
        nameField.setMaxWidth(300);

        Label colorLbl = makeSectionLabel("Player Color:");
        ComboBox<String> colorBox = new ComboBox<>();
        colorBox.getItems().addAll("BLUE", "RED", "GREEN", "YELLOW", "PURPLE", "ORANGE");
        colorBox.setValue("BLUE");
        colorBox.setMaxWidth(300);

        Label abilityLbl = makeSectionLabel("Choose 3 Starting Abilities:");

        List<String> allNames = Arrays.asList(
            "Slash", "Block", "Quick Strike", "Power Strike",
            "Heal", "Shield", "Fireball", "Ice Shard"
        );

        ListView<String> availableList = new ListView<>();
        availableList.getItems().addAll(allNames);
        availableList.setPrefHeight(180);
        availableList.setMaxWidth(250);

        ListView<String> selectedList = new ListView<>();
        selectedList.setPrefHeight(180);
        selectedList.setMaxWidth(250);

        // Buttons to add/remove abilities from the selected list
        Button addBtn = makeBtn("→ Add", () -> {});
        addBtn.getStyleClass().add("btn-small");
        addBtn.setOnAction(e -> {
            String sel = availableList.getSelectionModel().getSelectedItem();
            if (sel != null && selectedList.getItems().size() < 3) {
                selectedList.getItems().add(sel);
                availableList.getItems().remove(sel);
            }
        });

        // Remove button to move abilities back to the available list
        Button removeBtn = makeBtn("← Remove", () -> {});
        removeBtn.getStyleClass().add("btn-small");
        removeBtn.setOnAction(e -> {
            String sel = selectedList.getSelectionModel().getSelectedItem();
            if (sel != null) {
                selectedList.getItems().remove(sel);
                availableList.getItems().add(sel);
                availableList.getItems().sort(String::compareTo);
            }
        });

        VBox btnCol = new VBox(10, addBtn, removeBtn);
        btnCol.setAlignment(Pos.CENTER);

        HBox abilityBox = new HBox(15, availableList, btnCol, selectedList);
        abilityBox.setAlignment(Pos.CENTER);

        Label countLbl = new Label("Selected: 0/3 abilities");
        countLbl.getStyleClass().add("hint-label");
        selectedList.getItems().addListener(
            (javafx.collections.ListChangeListener<String>) c -> {
                int n = selectedList.getItems().size();
                countLbl.setText("Selected: " + n + "/3 abilities");
            }
        );

        // Create button will validate input and start the game if everything is valid
        Button createBtn = makeBtn("Create & Play", () -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) 
            { 
                alert("Please enter a name."); return; 
            }
            if (selectedList.getItems().size() != 3) 
            {
                alert("Select exactly 3 abilities. You have: " + selectedList.getItems().size());
                return;
            }
            character = new Character(name, colorBox.getValue());
            for (String a : selectedList.getItems()) character.addAbility(Ability.create(a));
            saveCharacter(character);
            launchGame();
        });

        Button backBtn = makeBtn("Back", this::show);
        createBtn.getStyleClass().add("btn-medium");
        backBtn.getStyleClass().add("btn-medium");

        HBox btns = new HBox(15, createBtn, backBtn);
        btns.getStyleClass().add("btn-row");

        root.getChildren().addAll(title, nameLbl, nameField, colorLbl, colorBox,
            abilityLbl, abilityBox, countLbl, btns);

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        Scene scene = new Scene(scroll, 800, 650);
        scene.getStylesheets().add(CSS);
        stage.setScene(scene);
    }

    // Shows the load game screen where the player can select a saved game
    private void showLoadScreen()
    {
        VBox root = new VBox(14);
        root.getStyleClass().add("screen-root-top");

        Label title = new Label("Load Game");
        title.getStyleClass().add("screen-title");

        File folder = new File(SAVE_FOLDER);
        File[] files = folder.listFiles((d, n) -> n.endsWith(".dat"));
        if (files != null)
        {
            Arrays.sort(files, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
        }

        ListView<String> savesList = new ListView<>();
        savesList.setPrefHeight(280);
        if (files != null)
        { 
            for (File f : files)
            {
                String date = new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm").format(new java.util.Date(f.lastModified()));
                savesList.getItems().add(f.getName().replace(".dat", "") + "  (" + date + ")");
            }
        }
        if (savesList.getItems().isEmpty())
        { 
            savesList.getItems().add("(no saved games found)"); 
        }

        Button loadBtn = makeBtn("Load Selected", () -> {
            String sel = savesList.getSelectionModel().getSelectedItem();
            if (sel == null || sel.startsWith("(")) 
            { 
                alert("Select a save file."); return; 
            }
            character = loadCharacter(sel.split("  \\(")[0]);
            if (character != null) launchGame();
        });

        // Delete button to delete the selected save file after confirmation
        Button deleteBtn = makeBtn("Delete Selected", () -> {
            String sel = savesList.getSelectionModel().getSelectedItem();

            if (sel == null || sel.startsWith("(")) 
            { 
                alert("Select a save file to delete."); return; 
            }
            
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Delete Save");
            confirm.setContentText("Delete \"" + sel + "\"? This cannot be undone.");

            ButtonType yes = new ButtonType("Yes, Delete");
            ButtonType no  = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            confirm.getButtonTypes().setAll(yes, no);
            confirm.showAndWait().ifPresent(r -> {
                if (r == yes) 
                {
                    new File(SAVE_FOLDER, sel.split("  \\(")[0] + ".dat").delete();
                    showLoadScreen();
                }
            });
        });

        Button backBtn = makeBtn("Back", this::show);
        loadBtn.getStyleClass().add("btn-medium");
        deleteBtn.getStyleClass().add("btn-medium");
        backBtn.getStyleClass().add("btn-medium");

        HBox btns = new HBox(15, loadBtn, deleteBtn, backBtn);
        btns.getStyleClass().add("btn-row");

        root.getChildren().addAll(title, savesList, btns);

        Scene scene = new Scene(root, 800, 500);
        scene.getStylesheets().add(CSS);
        stage.setScene(scene);
    }

    // Saves the character to a file in the saves folder using Java serialization
    private void saveCharacter(Character c)
    {
        try 
        {
            ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(new File(SAVE_FOLDER, c.getName() + ".dat")));
            oos.writeObject(c);
            oos.close();
        } 
        catch (IOException ex) { ex.printStackTrace(); }
    }

    /**
     * Loads a character from a saved file
     * 
     * @param name The character's name
     * @return The loaded Character object, or null if loading failed
     */
    private Character loadCharacter(String name)
    {
        try 
        {
            ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(new File(SAVE_FOLDER, name + ".dat")));
            Character c = (Character) ois.readObject();
            ois.close();
            return c;
        } 
        catch (Exception ex) 
        {
            ex.printStackTrace();
            alert("Failed to load: " + ex.getMessage());
            return null;
        }
    }

    // Launches the game
    private void launchGame()
    {
        try 
        {
            new GameScreen(stage, character).show();
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
            alert("Error starting game: " + e.getMessage());
        }
    }

    // Helper methods for creating styled UI components and showing alerts
    private Label makeSectionLabel(String text)
    {
        Label l = new Label(text);
        l.getStyleClass().add("section-label");
        return l;
    }

    /**
     * Creates a button with the given text and action
     * 
     * @param text the text to display on the button
     * @param action the Runnable to execute when the button is clicked
     * @return the created button
     */
    private Button makeBtn(String text, Runnable action)
    {
        Button b = new Button(text);
        b.setOnAction(e -> action.run());
        return b;
    }

    /**
     * Displays an alert dialog with the given message
     * 
     * @param msg the message to display
     */
    private void alert(String msg)
    {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setContentText(msg);
        a.showAndWait();
    }
}