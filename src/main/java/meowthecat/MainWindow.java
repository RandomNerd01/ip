package meowthecat;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
/**
 * Controller for the main GUI.
 */
public class MainWindow extends AnchorPane {
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox dialogContainer;
    @FXML
    private TextField userInput;
    @FXML
    private Button sendButton;

    private MeowCat meow;

    private Image userImage;
    private Image meowImage;

    @FXML
    public void initialize() {
        scrollPane.vvalueProperty().bind(dialogContainer.heightProperty());

        // ensure FXML Injected properly
        assert scrollPane != null : "scrollPane should be injected";
        assert dialogContainer != null : "dialogContainer should be injected";

        userImage = new Image(this.getClass().getResourceAsStream("/cat1.PNG"));
        meowImage = new Image(this.getClass().getResourceAsStream("/cat.PNG"));

        assert userImage != null : "userImage should be present";
        assert meowImage != null : "MeowTheCat's image should be present";
    }

    /** Injects the Duke instance */
    public void setMeow(MeowCat d) {
        meow = d;
    }

    /**
     * Creates two dialog boxes, one echoing user input and the other containing Duke's reply and then appends them to
     * the dialog container. Clears the user input after processing.
     */
    @FXML
    private void handleUserInput() {
        String input = userInput.getText();
        String response = meow.getResponse(input);
        dialogContainer.getChildren().addAll(
                DialogBox.getUserDialog(input, userImage),
                DialogBox.getMeowDialog(response, meowImage)
        );
        userInput.clear();
    }
}
