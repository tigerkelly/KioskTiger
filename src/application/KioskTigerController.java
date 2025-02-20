package application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;

public class KioskTigerController implements Initializable {

    @FXML
    private AnchorPane aPane;
    
    private VBox vboxMain = null;
    private String kioskHtml = null;
    private String kioskUrl = null;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		
		loadConfig();		// Load kiosktiger.conf file.
		
		WebView webView = new WebView();
		VBox.getVgrow(webView);
		
		vboxMain = new VBox();
		
		vboxMain.getChildren().add(webView);
		
		aPane.getChildren().add(vboxMain);
		
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
    			}if (a[x].toLowerCase().startsWith("kioskurl=") == true) {
    				kioskUrl = a[x].substring(9).trim();
    			}
    		}
    		
//    		System.out.println(kioskHtml + ", " + kioskUrl);
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

}
