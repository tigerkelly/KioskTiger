package application;

import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.TouchEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.robot.Robot;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SleepModeController implements Initializable {
	
	private KtGlobal kg = KtGlobal.getInstance();
	private Robot robot = null;

    @FXML
    private AnchorPane aPane;

    @FXML
    private Button btnCute;
    
    @FXML
    private Label lblMessage;

    @FXML
    private ImageView ivCute;

    @FXML
    void doBtnCute(ActionEvent event) {
    	kg.lastAction = new Date().getTime() / 1000;
    	Stage stage = (Stage)aPane.getScene().getWindow();
    	stage.close();
    }
    
    @FXML
    void doTouch(TouchEvent event) {
    	doBtnCute(null);
    }

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		
		aPane.setCursor(Cursor.NONE);
		robot = new Robot();
		robot.mouseMove(0, 0);
		
		btnCute.setText("KioskTiger\nKelly Wiles\n" + kg.appVersion);
		
		if (kg.kioskMsg != null)
			lblMessage.setText(kg.kioskMsg);
		
		if (kg.txtColor != null)
			lblMessage.setStyle("-fx-text-fill: " + kg.txtColor + ";");
		
		lblMessage.setLayoutX(1.0);
		lblMessage.setLayoutY(kg.screenHeight - 20);
		
		btnCute.setLayoutX(kg.screenWidth / 3);
        btnCute.setLayoutY(kg.screenHeight / 3);
		
		timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
	}
	
	public Stage getStage() {
		return (Stage)aPane.getScene().getWindow();
	}
	
	public Scene getScene() {
		return (Scene)aPane.getScene();
	}
	
	public AnchorPane getPane() {
		return aPane;
	}
	
	// Move button around screen.
    Timeline timeline = new Timeline(new KeyFrame(Duration.millis(20), new EventHandler<ActionEvent>() {

        double deltaX = 2;
        double deltaY = 2;

        @Override
        public void handle(ActionEvent actionEvent) {
            btnCute.setLayoutX(btnCute.getLayoutX() + deltaX);
            btnCute.setLayoutY(btnCute.getLayoutY() + deltaY);

            Bounds b2 = btnCute.getLayoutBounds();
            
            boolean rightBorder = btnCute.getLayoutX() >= (kg.screenWidth - b2.getWidth());
            boolean leftBorder = btnCute.getLayoutX() <= 4;
            boolean bottomBorder = btnCute.getLayoutY() >= (kg.screenHeight - b2.getHeight());
            boolean topBorder = btnCute.getLayoutY() <= 4;

            if (rightBorder || leftBorder) {
                deltaX *= -1;
            }
            if (bottomBorder || topBorder) {
                deltaY *= -1;
            }
        }
    }));

}
