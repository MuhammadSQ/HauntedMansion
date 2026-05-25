import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Popup window showing the player's ability book 
 * They can have a max of 6
 * And are able to swap abilities when they get a new one and are at max
 */
public class AbilityBookUI
{
    private Character character;

    private static final String CSS = AbilityBookUI.class.getResource("style.css").toExternalForm();

    public AbilityBookUI(Character character)
    {
        this.character = character;
    }

    //Opens te ability book and can be closed once done
    public void show()
    {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Ability Book");

        Scene scene = new Scene(buildLayout(popup, null, null), 500, 450);
        scene.getStylesheets().add(CSS);
        popup.setScene(scene);
        popup.show();
    }

    /**
     * Opens the ability book and allows the player to swap abilities
     * This occurs when a player already has the maximum amount of abilities which is 6
     * 
     * @param newAbility The Ability that the player wants to add
     * @param onConfirmed runnable to execute after the swap is confirmed 
     */
    public void showSwapDialog(Ability newAbility, Runnable onConfirmed)
    {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Ability Book Full - Choose Replacement");

        Scene scene = new Scene(buildLayout(popup, newAbility, onConfirmed), 500, 500);
        scene.getStylesheets().add(CSS);
        popup.setScene(scene);
        popup.show();
    }

    /**
     * Builds the layout for the ability book popup
     * 
     * @param popup the popup stage to allow for closing after swap
     * @param newAbility the new ability the player is trying to acquire 
     * @param onConfirmed runnable to execute after confirming a swap 
     * @return the constructed VBox containing the complete UI layout
     */
    private VBox buildLayout(Stage popup, Ability newAbility, Runnable onConfirmed)
    {
        VBox root = new VBox(12);
        root.getStyleClass().add("ability-root");

        Label title = new Label("Ability Book  (" + character.getAbilities().size() + "/6)");
        title.getStyleClass().add("ability-title");
        root.getChildren().add(title);

        //If this is a swap dialog additional info will be shown
        if (newAbility != null) 
        {
            Label newLabel = new Label("New: " + newAbility.toString());
            newLabel.getStyleClass().add("new-ability-label");

            Label instr = new Label("Select an ability to replace, then click Swap");
            instr.getStyleClass().add("swap-instruction-label");
            root.getChildren().addAll(newLabel, instr);
        }

        //Shows all current abilities with their details
        ListView<String> list = new ListView<>();
        list.setPrefHeight(220);
        character.getAbilities().forEach(a -> list.getItems().add(a.toString()));
        root.getChildren().add(list);

        HBox btnRow = new HBox(15);
        btnRow.getStyleClass().add("btn-row");

        // If this is a swap dialog show the swap and cancel buttons
        if (newAbility != null && onConfirmed != null) 
        {
            // Swap button will replace the selected ability with the new one
            Button swapBtn = new Button("Swap Selected");
            swapBtn.getStyleClass().add("btn-medium");
            swapBtn.setOnAction(e -> {
                // Get selected index
                int idx = list.getSelectionModel().getSelectedIndex();

                if (idx >= 0)
                {
                    // Replace ability
                    character.replaceAbility(idx, newAbility);
                    popup.close();
                    onConfirmed.run();
                } 
                else 
                {
                    // No ability chosen show info alert
                    showInfo("Please select an ability to replace.");
                }
            });

            // Skip button will keep the current abilities
            Button skipBtn = new Button("Keep Current Abilities");
            skipBtn.getStyleClass().add("btn-medium");
            skipBtn.setOnAction(e -> popup.close());

            btnRow.getChildren().addAll(swapBtn, skipBtn);
        } 
        else 
        {
            Button closeBtn = new Button("Close");
            closeBtn.getStyleClass().add("btn-medium");
            closeBtn.setOnAction(e -> popup.close());
            btnRow.getChildren().add(closeBtn);
        }

        root.getChildren().add(btnRow);
        return root;
    }

    /**
     * Shows an informational alert with the given message
     * @param msg the message to display
     */
    private void showInfo(String msg)
    {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}