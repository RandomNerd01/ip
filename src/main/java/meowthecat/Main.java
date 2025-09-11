package meowthecat;
import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * A GUI for MeowTheCat using FXML.
 */
public class Main extends Application {

    private final MeowCat meow = new MeowCat();

    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/view/MainWindow.fxml"));
            AnchorPane ap = fxmlLoader.load();
            assert ap != null : "MainWindow FXML root AnchorPane must not be null";
            Scene scene = new Scene(ap);
            stage.setScene(scene);
            MainWindow controller = fxmlLoader.getController();
            assert controller != null : "MainWindow controller must be available after loading FXML";
            controller.setMeow(meow);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("MainWindow.fxml");
        }
    }
}
