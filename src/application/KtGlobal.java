package application;

import java.io.IOException;

import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class KtGlobal {

	private static KtGlobal singleton = null;
	
	private KtGlobal() {
		initGlobals();
	}
	
	private void initGlobals() {
		appVersion = "1.1.0";
		
		sceneNav = new SceneNav();
	}
	
	public String appVersion;
	
	public double screenWidth = 0.0;
	public double screenHeight = 0.0;
	
	public SceneNav sceneNav = null;
	
	public long lastAction = 0;
	public int sleepTime = 0;
	public int delayTime = 3;
	public boolean sleepMode = false;
	public String kioskMsg = null;
	public String txtColor = null;
	public Timeline sleepTask = null;
	
	public static KtGlobal getInstance() {
		// return SingletonHolder.singleton;
		if (singleton == null) {
			synchronized (KtGlobal.class) {
				singleton = new KtGlobal();
			}
		}
		return singleton;
	}
	
	public String scenePeek() {
		if (sceneNav.sceneQue == null || sceneNav.sceneQue.isEmpty())
			return SceneNav.KIOSKTIGER;
		else
			return sceneNav.sceneQue.peek();
	}
	
	public FXMLLoader loadScene(Node node, String fxml, String title, String data) {
		FXMLLoader loader = null;
		try {
			Stage stage = new Stage();
			stage.setTitle(title);

			loader = new FXMLLoader(getClass().getResource(fxml));

			stage.initModality(Modality.APPLICATION_MODAL);

			stage.setScene(new Scene(loader.load()));
			stage.hide();
			
			stage.getScene().setCursor(Cursor.NONE);

			Stage ps = (Stage) node.getScene().getWindow();

			ChangeListener<Number> widthListener = (observable, oldValue, newValue) -> {
				double stageWidth = newValue.doubleValue();
				stage.setX(ps.getX() + ps.getWidth() / 2 - stageWidth / 2);
			};
			ChangeListener<Number> heightListener = (observable, oldValue, newValue) -> {
				double stageHeight = newValue.doubleValue();
				stage.setY(ps.getY() + ps.getHeight() / 2 - stageHeight / 2);
			};

			stage.widthProperty().addListener(widthListener);
			stage.heightProperty().addListener(heightListener);

			// Once the window is visible, remove the listeners
			stage.setOnShown(e2 -> {
				stage.widthProperty().removeListener(widthListener);
				stage.heightProperty().removeListener(heightListener);
			});

		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		return loader;
	}
}
