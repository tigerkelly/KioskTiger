package application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.concurrent.RejectedExecutionException;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.input.TouchEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.robot.Robot;
import javafx.scene.web.WebView;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

public class KioskTigerController implements Initializable {

    @FXML
    private AnchorPane aPane;
    
    @FXML
    private HBox hboxMain = null;
    
    @FXML
    private WebView webView;
    
    private Robot robot;
    private String kioskHtml = null;
    private String kioskUrl = null;
    private KtGlobal kg = KtGlobal.getInstance();
    
    @FXML
    void hideMouse(TouchEvent event) {
    	webView.setCursor(Cursor.NONE);		// Keep the mouse cursor hidden.
    	kg.lastAction = new Date().getTime() / 1000;
    }

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		
		robot = new Robot();
		robot.mouseMove(0, 0);
		aPane.setCursor(Cursor.NONE);
		hboxMain.setCursor(Cursor.NONE);
		webView.setCursor(Cursor.NONE);
		
		kg.lastAction = new Date().getTime() / 1000;
		
		Rectangle2D screenBounds = Screen.getPrimary().getBounds();
		kg.screenWidth = screenBounds.getWidth();
		kg.screenHeight = screenBounds.getHeight();
		
		loadConfig();		// Load kiosktiger.conf file.
		
		if (kioskHtml != null) {		// local web pages.
			String html = null;
			
			html = readResource(kioskHtml);
			
			if (html != null) {
				webView.getEngine().loadContent(html, "text/html");
			}
		} else if (kioskUrl != null) {		// remote web site.
			webView.getEngine().load(kioskUrl);
		} else {
			System.out.println("No KIOSKHTML or KIOSKURL key/value pair found in kiosktiger.conf.");
		}
		
		if (kg.sleepTime > 0) {
			kg.sleepTask = new Timeline(new KeyFrame(Duration.seconds(kg.delayTime), new EventHandler<ActionEvent>() {
	
			    @Override
			    public void handle(ActionEvent event) {
			    	long d = new Date().getTime() / 1000;
			    	
			    	if ((d - kg.lastAction) > kg.sleepTime && kg.sleepMode == false) {
						Platform.runLater(new Runnable() {
						    @Override
						    public void run() {
						    	kg.sleepMode = true;
						    	sleepNow();
						    }
						});
					}
			    }
			}));
			
			kg.sleepTask.setCycleCount(Timeline.INDEFINITE);
			kg.sleepTask.play();
		}
	}
	
	public boolean loadConfig() {
		String config = null;
		
		File f = new File("kiosktiger.conf");
    	if (f.exists() == false) {
    		System.out.println("File kiosktiger.conf not found.");
    		return true;
    	}
    	
    	try {
			InputStream is2 = new FileInputStream(f.getAbsolutePath());
			config = new String(is2.readAllBytes());
        	
        	is2.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	if (config != null) {
    		String[] a = config.split("\n");
    		
    		for (int x = 0; x < a.length; x++) {
    			if (a[x].toLowerCase().startsWith("kioskhtml=") == true) {
    				kioskHtml = a[x].substring(10).trim();
    			} else if (a[x].toLowerCase().startsWith("kioskurl=") == true) {
    				kioskUrl = a[x].substring(9).trim();
    			} else if (a[x].toLowerCase().startsWith("sleeptime=") == true) {
    	    		kg.sleepTime = Integer.parseInt(a[x].substring(10).trim());
    			} else if (a[x].toLowerCase().startsWith("delaytime=") == true) {
    	    	    kg.delayTime = Integer.parseInt(a[x].substring(10).trim());
    			} else if (a[x].toLowerCase().startsWith("kioskmsg=") == true) {
        			kg.kioskMsg = a[x].substring(9).trim();
    			} else if (a[x].toLowerCase().startsWith("txtcolor=") == true) {
        			kg.txtColor = a[x].substring(9).trim();
    			}
    		}
    		
//    		System.out.println(kioskHtml + ", " + kioskUrl +", " + kg.sleepTime + ", " + kg.delayTime);
    	}
		
		return false;
	}
	
	public String readResource(String resourcePath) {
		String html = null;
		
    	File f = new File(resourcePath);
    	if (f.exists() == false) {
    		return html;
    	}
    	
		try {
			InputStream is2 = new FileInputStream(f.getAbsolutePath());
			html = new String(is2.readAllBytes());
        	
        	is2.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        return html;
    }
	
	public void sleepNow() {
		
		Platform.runLater(new Runnable() {
		    @Override
		    public void run() {
		    	try {
			    	FXMLLoader loader = kg.loadScene(aPane, SceneNav.SLEEPMODE, null, null);
					SleepModeController smc = (SleepModeController) loader.getController();
					smc.getPane().setPrefWidth(kg.screenWidth);
					smc.getPane().setPrefHeight(kg.screenHeight);
					smc.getScene().setCursor(Cursor.NONE);
					aPane.setPrefHeight(kg.screenHeight);
					Stage stage = smc.getStage();
					stage.showAndWait();
					
					kg.sleepMode = false;
		    	} catch (RejectedExecutionException ree) {
		    		
		    	}
		    }
		});
	}

}
