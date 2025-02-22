# KioskTiger
A RPi Kiosk mode without X11 or Wayland using Javafx webview.

This should work on RPI 3/4/5 with raspian 64bit full OS.

This code uses the JavaFX WebView widget and the Monocle/glass feature of Java 11.
No need to have X11 or Wayland installed.  This uses the Linux frame buffer instead of X11 or Wayland.

For the RPi 4 SBC use the Raspian 64bit full OS.  You can find many online tutorials on how to install it.

Then update your OS.

	sudo apt update
 	sudo apt -y full-upgrade
  	sudo reboot

Once installed you can use the following command to disable the X11 and Wayland GUI interface. 

	sudo raspi-config
	
	System Options -> Boot / Auto login -> Console Autologin
 
 Or use comman line

 	sudo

The Java version has to be version 11, use the following link to get it. Later versions do not have Monocle/glass :(

https://bell-sw.com/pages/downloads/#jdk-11-lts

For RPi SBC uses the Linux ARM DEB file. Use wget to download file.

	wget https://download.bell-sw.com/java/11.0.26+9/bellsoft-jdk11.0.26+9-linux-aarch64-full.deb

Then use the dpkg command to install it.

	sudo dpkg -i ./bellsoft-jdk11.0.26+9-linux-aarch64-full.deb
	
Create a directory call KioskTiger in your home directory.
	
Place the following bash script into the KioskTiger directory and name it kiosktiger.sh.

	#!/usr/bin/bash
	
	logf=/home/pi/KioskTiger/java_log.log
	
	echo "Starting `date`" > ${logf}
	
	cd /home/pi/KioskTiger
	
	echo "*** Java command line logging started. ***" >> ${logf}
	
    sudo -E java \
    -Djava.library.path=. \
    -Dglass.platform=Monocle \
    -Dmonocle.platform=Linux \
    -Dmonocle.screen.fb=/dev/fb0 \
    -Dembedded=Monocle \
    -Dprism.order=sw \
    -Dcom.sun.javafx.virtualKeyboard="none" \
    -Dcom.sun.javafx.isEmbedded=true \
    -Dcom.sun.javafx.touch=true \
    --module-path /usr/lib/jvm/bellsoft-java11-full-aarch64/legal \
    --add-modules javafx.controls,javafx.fxml,javafx.graphics \
    -jar kiosktiger.jar >> ${logf} 2>&1

This script will execute the KioskTiger jar file. 

Be sure and change the permissions of the kiosktiger.sh file.
	
	chmod +x kiosktiger.sh

The configuration for the KioskTiger jar file is in the file kiosktiger.conf file.

	# Currently only two key/value pairs exist.
	# Use only one of the two.
	# KIOSKHTML = HTML file name, like KioskTiger.html
	# KIOSKURL = Use URL instead of HTML file.
	# To comment out a key/value just prefix it with a '#'
	
	KIOSKHTML=KioskTiger.html
	#KIOSKURL=https://google.com

The code currently only understands the two config items but you can add more if you need to.

Place the KioskTiger.html file in to the KioskTiger directory.
In the above configuration it will load the KioskTiger.html file into the WebView widget but
if you want to use a remote web site then comment out the KIOSKHTML and set the KIOSKURL key/value pair.

To execute the bash script when the RPi boots up then do the following.
Edit .profile 
	
	nano .profile
	
At the end of the file add the following lines.

	export XCURSOR_SIZE=16

	x=$(figlet KioskTiger)
	printf "\033[94m$x\033[0m\n"
	
	t=$(tty)
	
	if [ "$t" == "/dev/tty1" ]; then
	       /home/pi/KioskTiger/kiosktiger.sh
	       echo KioskTiger
	fi

The figlet program can be installed using

	sudo apt install -y figlet
	
