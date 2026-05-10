package application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.Objects;
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
import javafx.scene.image.Image;
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
		
		webView.getEngine().setUserAgent(kg.userAgent);
		
		if (kg.kioskHtml != null) {		// local web pages.
			String html = null;
			
			html = readResource(kg.kioskHtml);
			
			if (html != null) {
				webView.getEngine().loadContent(html, "text/html");
			}
		} else if (kg.kioskUrl != null) {		// remote web site.
			webView.getEngine().load(kg.kioskUrl);
		} else {
			System.out.println("No KIOSKHTML or KIOSKURL key/value pair found in kiosktiger.conf.");
		}
		
		if (kg.pageZoom != null) {
			double d = Double.parseDouble(kg.pageZoom);
			webView.setZoom(d);
		}
		
		if (kg.fontScale != null) {
			double d = Double.parseDouble(kg.fontScale);
			webView.setFontScale(d);
		}
		
//		System.out.println("scale: " + webView.getFontScale());
		
		if (kg.sleepTime > 0) {
			kg.sleepTask = new Timeline(new KeyFrame(Duration.seconds(kg.delayTime), new EventHandler<ActionEvent>() {
	
			    @Override
			    public void handle(ActionEvent event) {
			    	long d = new Date().getTime() / 1000;
			    	
			    	if ((d - kg.lastAction) > kg.sleepTime && kg.sleepMode == false) {
//						Platform.runLater(new Runnable() {
//						    @Override
//						    public void run() {
//						    	kg.sleepMode = true;
//						    	sleepNow();
//						    }
//						});
			    		kg.sleepMode = true;
				    	sleepNow();
					}
			    }
			}));
			
			kg.sleepTask.setCycleCount(Timeline.INDEFINITE);
			kg.sleepTask.play();
		}
		
		startConfigWatcher();   // <-- add at the very end
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
    				kg.kioskHtml = a[x].substring(10).trim();
    			} else if (a[x].toLowerCase().startsWith("kioskurl=") == true) {
    				kg.kioskUrl = a[x].substring(9).trim();
    			} else if (a[x].toLowerCase().startsWith("sleeptime=") == true) {
    				try {
    					kg.sleepTime = Integer.parseInt(a[x].substring(10).trim());
    				} catch (NumberFormatException  nfe) {
    					
    				}
    			} else if (a[x].toLowerCase().startsWith("delaytime=") == true) {
    				try {
    					kg.delayTime = Integer.parseInt(a[x].substring(10).trim());
    				} catch (NumberFormatException  nfe) {
    					
    				}
    			} else if (a[x].toLowerCase().startsWith("kioskmsg=") == true) {
        			kg.kioskMsg = a[x].substring(9).trim();
    			} else if (a[x].toLowerCase().startsWith("txtcolor=") == true) {
        			kg.txtColor = a[x].substring(9).trim();
    			} else if (a[x].toLowerCase().startsWith("pagezoom=") == true) {
        			kg.pageZoom = a[x].substring(9).trim();
    			} else if (a[x].toLowerCase().startsWith("fontscale=") == true) {
        			kg.fontScale = a[x].substring(10).trim();
    			} else if (a[x].toLowerCase().startsWith("useragent=") == true) {
        			kg.userAgent = a[x].substring(10).trim();
    			} else if (a[x].toLowerCase().startsWith("sleepicon=") == true) {
    				String icon = a[x].substring(10).trim();

    			    // "default" is a sentinel meaning "use the bundled CuteTiger.png"
    			    if (icon.equalsIgnoreCase("default") || icon.isEmpty()) {
    			        kg.sleepIcon = new Image(getClass().getResourceAsStream("/images/CuteTiger.png"));
    			    } else {
    			        File iconFile = new File(icon);
    			        if (iconFile.exists()) {
    			            try {
    			                kg.sleepIcon = new Image(new FileInputStream(iconFile));
    			            } catch (FileNotFoundException e) {
    			                System.out.println("SLEEPICON file not found: " + icon + ", falling back to default.");
    			                kg.sleepIcon = new Image(getClass().getResourceAsStream("/images/CuteTiger.png"));
    			            }
    			        } else {
    			            System.out.println("SLEEPICON file not found: " + icon + ", falling back to default.");
    			            kg.sleepIcon = new Image(getClass().getResourceAsStream("/images/CuteTiger.png"));
    			        }
    			    }
//					try {
//						String icon = a[x].substring(10).trim();
//						FileInputStream is = new FileInputStream(icon);
//						kg.sleepIcon = new Image(is);
//					} catch (FileNotFoundException e) {
//						e.printStackTrace();
//					}
    			} else if (a[x].toLowerCase().startsWith("sleepicontext=") == true) {
        			kg.sleepIconText = a[x].substring(14).trim();
    			}
    		}
    		
//    		System.out.println(kioskHtml + ", " + kioskUrl +", " + kg.sleepTime + ", " + kg.delayTime);
    	}
		
		return false;
	}
	
	private void startConfigWatcher() {
	    kg.configWatcher = new ConfigWatcher("kiosktiger.conf", this::onConfigChanged);
	    kg.configWatcher.start();
	}
	
	private void onConfigChanged() {
	    // Snapshot values before reload so we can diff them
	    String oldHtml    = kg.kioskHtml;
	    String oldUrl     = kg.kioskUrl;
	    double oldZoom    = kg.pageZoom    != null ? Double.parseDouble(kg.pageZoom)    : 1.0;
	    double oldScale   = kg.fontScale   != null ? Double.parseDouble(kg.fontScale)   : 1.0;
	    int    oldSleep   = kg.sleepTime;
	    int    oldDelay   = kg.delayTime;
	    String oldAgent   = kg.userAgent;

	    boolean error = loadConfig();    // your existing method — reloads into KtGlobal
	    if (error) {
	        System.out.println("ConfigWatcher: reload failed, keeping current settings.");
	        return;
	    }

	    System.out.println("ConfigWatcher: config reloaded successfully.");

	    Platform.runLater(() -> applyConfigChanges(
	        oldHtml, oldUrl, oldZoom, oldScale, oldSleep, oldDelay, oldAgent
	    ));
	}
	
	private void applyConfigChanges(
	        String oldHtml,  String oldUrl,
	        double oldZoom,  double oldScale,
	        int    oldSleep, int    oldDelay,
	        String oldAgent) {

	    boolean pageChanged = !Objects.equals(kg.kioskHtml, oldHtml)
	                       || !Objects.equals(kg.kioskUrl,  oldUrl);

	    boolean zoomChanged  = kg.pageZoom  != null && Double.parseDouble(kg.pageZoom)  != oldZoom;
	    boolean scaleChanged = kg.fontScale != null && Double.parseDouble(kg.fontScale) != oldScale;
	    boolean agentChanged = !Objects.equals(kg.userAgent, oldAgent);

	    boolean sleepChanged = kg.sleepTime != oldSleep || kg.delayTime != oldDelay;

	    // ── Apply zoom / font scale ───────────────────────────────────────────────
	    if (zoomChanged) {
	        double z = Double.parseDouble(kg.pageZoom);
	        webView.setZoom(z);
	        System.out.println("ConfigWatcher: zoom → " + z);
	    }

	    if (scaleChanged) {
	        double s = Double.parseDouble(kg.fontScale);
	        webView.setFontScale(s);
	        System.out.println("ConfigWatcher: fontScale → " + s);
	    }

	    // ── Apply user agent (requires page reload to take effect) ────────────────
	    if (agentChanged && kg.userAgent != null) {
	        webView.getEngine().setUserAgent(kg.userAgent);
	        System.out.println("ConfigWatcher: userAgent → " + kg.userAgent);
	        pageChanged = true;   // force reload so the server sees the new UA
	    }

	    // ── Reload page if URL/HTML changed ───────────────────────────────────────
	    if (pageChanged) {
	        if (kg.kioskHtml != null) {
	            String html = readResource(kg.kioskHtml);
	            if (html != null) {
	                webView.getEngine().loadContent(html, "text/html");
	                System.out.println("ConfigWatcher: reloaded local HTML → " + kg.kioskHtml);
	            }
	        } else if (kg.kioskUrl != null) {
	            webView.getEngine().load(kg.kioskUrl);
	            System.out.println("ConfigWatcher: navigated to → " + kg.kioskUrl);
	        }
	    }

	    // ── Restart sleep timeline if timing changed ───────────────────────────────
	    if (sleepChanged) {
	        if (kg.sleepTask != null) {
	            kg.sleepTask.stop();
	            kg.sleepTask = null;
	        }

	        if (kg.sleepTime > 0) {
	            kg.sleepTask = new Timeline(new KeyFrame(
	                Duration.seconds(kg.delayTime),
	                event -> {
	                    long now = new Date().getTime() / 1000;
	                    if ((now - kg.lastAction) > kg.sleepTime && !kg.sleepMode) {
	                        Platform.runLater(() -> {
	                            kg.sleepMode = true;
	                            sleepNow();
	                        });
	                    }
	                }
	            ));
	            kg.sleepTask.setCycleCount(Timeline.INDEFINITE);
	            kg.sleepTask.play();
	            System.out.println("ConfigWatcher: sleep timer restarted → " + kg.sleepTime + "s");
	        }
	    }
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
