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
