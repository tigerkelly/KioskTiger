package application;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


public class Main extends Application {
	private Pane mainPane = null;
	private KtGlobal kg = KtGlobal.getInstance();
	
	@Override
	public void start(Stage primaryStage) {
		try {
			primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/CuteTiger.png")));
			primaryStage.setTitle("KioskTiger by Kelly Wiles");
			primaryStage.initStyle(StageStyle.UNDECORATED);
			primaryStage.setScene(createScene(loadMainPane()));
			
			// Use the Run Configuration screens to set InEclipse to true in the Environment tab.
			String flag = System.getenv("InEclipse");
			if (flag == null || flag.equalsIgnoreCase("true") == false) {
				primaryStage.setMaximized(true);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		primaryStage.show();
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
	public void setWidth(Double width) {
		mainPane.setPrefWidth(width);
	}

	@Override
	public void stop() {
//		System.out.println("*** KioskTiger is Ending. ***");
		
		if (kg.sleepTask != null) {
			kg.sleepTask.stop();
		}

		SceneInfo si = kg.sceneNav.fxmls.get(kg.scenePeek());
		if (si != null && si.controller instanceof RefreshScene) {
			RefreshScene c = (RefreshScene) si.controller;
			c.leaveScene();
		}
	}

	/**
	 * Loads the main fxml layout.
	 *
	 * @return the loaded pane.
	 * @throws IOException if the pane could not be loaded.
	 */
//    @SuppressWarnings("resource")
	private Pane loadMainPane() throws IOException {
//		System.out.println("*** KioskTiger is Starting. ***");

		FXMLLoader loader = new FXMLLoader();

		mainPane = (Pane) loader.load(getClass().getResourceAsStream(SceneNav.MAIN)); // SceneNav

		SceneNavController mainController = loader.getController();

		kg.sceneNav.setMainController(mainController);
		kg.sceneNav.loadScene(SceneNav.KIOSKTIGER);

		return mainPane;
	}

	/**
	 * Creates the main application scene.
	 *
	 * @param mainPane the main application layout.
	 *
	 * @return the created scene.
	 */
	private Scene createScene(Pane mainPane) {
		Scene scene = new Scene(mainPane);
		scene.setCursor(Cursor.NONE);

//		scene.getStylesheets().setAll(getClass().getResource("application.css").toExternalForm());

		return scene;
	}
}
