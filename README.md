# KioskTiger
A RPi Kiosk mode without X11, Wayland and Chromimum using Javafx webview.

This should work on RPI 4/5 with raspian 64bit full OS.

This code uses the JavaFX WebView widget and the Monocle/glass feature of Java 11.
No need to have X11 or Wayland installed.  This uses the Linux frame buffer instead of X11 or Wayland.

For the RPi 4 SBC use the Raspian 64bit full OS.  You can find many online tutorials on how to install it.

Update your OS.

	sudo apt update
 	sudo apt -y full-upgrade
  	sudo reboot

Once installed you can use the following command to disable the X11 and Wayland GUI interface. 

	sudo raspi-config
	
	System Options -> Boot / Auto login -> Console Autologin
 
 Or use command line

 	sudo update-rc.d lightdm disable

The Java version has to be version 11, use the following link to get it. Later versions do not have Monocle/glass :(

For RPi SBC uses the Linux ARM DEB file. Use wget to download file.

	wget https://download.bell-sw.com/java/11.0.26+9/bellsoft-jdk11.0.26+9-linux-aarch64-full.deb

Then use the dpkg command to install it.

	sudo dpkg -i ./bellsoft-jdk11.0.26+9-linux-aarch64-full.deb

Create a work directory.

	mkdir work
	cd work
	git clone https://github.com/tigerkelly/KioskTiger.git
	cd ~/
	
Create a directory call KioskTiger in your home directory.

	dos2unix /home/pi/work/KioskTiger/profile_start.txt
	dos2unix /home/pi/work/KioskTiger/KioskTiger.service
	dos2unix /home/pi/work/KioskTiger/kiosktiger.sh
	dos2unix /home/pi/work/KioskTiger/kiosktiger.conf
	cat /home/pi/work/KioskTiger/profile_start.txt >> ~/.profile
	cp /home/pi/work/KioskTiger/vimrc ~/.vimrc
	cp /home/pi/work/KioskTiger/KioskTiger.html ~/KioskTiger
	cp /home/pi/work/KioskTiger/kiosktiger.sh ~/KioskTiger
	sudo chmod 755 /home/pi/KioskTiger/kiosktiger.sh
	cp /home/pi/work/KioskTiger/KioskTiger.conf ~/KioskTiger

	sudo cp /home/pi/work/KioskTiger/KisokTiger.service /etc/systemd/system/
	sudo systemctl enable KioskTiger.service
	sudo systemctl start KioskTiger.dervice

	sudo apt install -y vim figlet

The code currently only understands the two config items but you can add more if you need to.

Edit the KioskTiger.conf file to define the operating parameters.
If you want KioskTiger.jar to load a web page like KioskTiger.html then make sure the config file
has an uncommented key/value pair call KIOSKHTML.
If you want the KioskTiger.jar to load a remote web site then comment out KIOSKHTML and set KIOSKURL.
