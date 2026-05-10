# KioskTiger

A lightweight kiosk mode for Raspberry Pi that displays a web page or local HTML file fullscreen — no X11, no Wayland, no Chromium required. Built on JavaFX WebView with the Monocle/Glass framebuffer renderer, it talks directly to `/dev/fb0` and handles touch input natively.

---

## Features

- Fullscreen WebView on the Linux framebuffer — zero desktop environment overhead
- Load a **local HTML file** or a **remote URL** from config
- **Sleep / screensaver mode** — bouncing icon appears after configurable inactivity timeout
- **Touchscreen support** — touch wakes the screen and resets the inactivity timer
- **Hot-reload config** — edit `kiosktiger.conf` while running; changes apply without a restart
- **Network error recovery** — automatic exponential back-off retry on connection failure
- Configurable page zoom, font scale, and user agent string
- Runs as a systemd service, starts on boot

---

## Requirements

| Component | Version |
|-----------|---------|
| Hardware | Raspberry Pi 4 or 5 |
| OS | Raspberry Pi OS 64-bit (Full) |
| Java | BellSoft Liberica JDK 11 (full, aarch64) — **must be JDK 11**, later versions dropped Monocle |

---

## Installation

### 1. Update the OS

```bash
sudo apt update
sudo apt -y full-upgrade
sudo reboot
```

### 2. Disable the desktop GUI

Using `raspi-config`:

```bash
sudo raspi-config
# System Options → Boot / Auto Login → Console Autologin
```

Or directly:

```bash
sudo update-rc.d lightdm disable
```

### 3. Install Java 11 (BellSoft Liberica full)

```bash
wget https://download.bell-sw.com/java/11.0.26+9/bellsoft-jdk11.0.26+9-linux-aarch64-full.deb
sudo dpkg -i bellsoft-jdk11.0.26+9-linux-aarch64-full.deb
```

> The **full** variant bundles JavaFX including the Monocle platform. Standard JDK 11 builds will not work.

### 4. Install optional tools

```bash
sudo apt install -y vim figlet dos2unix
```

### 5. Clone the repository

```bash
mkdir ~/work && cd ~/work
git clone https://github.com/tigerkelly/KioskTiger.git
cd KioskTiger
```

### 6. Set up the application directory

```bash
mkdir ~/KioskTiger

dos2unix profile_start.txt KioskTiger.service kiosktiger.sh kiosktiger.conf

cat profile_start.txt >> ~/.profile

cp KioskTiger.html  ~/KioskTiger
cp kiosktiger.sh    ~/KioskTiger
cp kiosktiger.jar   ~/KioskTiger
cp kiosktiger.conf  ~/KioskTiger
cp vimrc            ~/.vimrc

chmod +x ~/KioskTiger/kiosktiger.sh
```

### 7. Install and start the systemd service

```bash
sudo cp KioskTiger.service /etc/systemd/system/
sudo systemctl enable KioskTiger.service
sudo systemctl start KioskTiger.service
```

To check status and view logs:

```bash
sudo systemctl status KioskTiger.service
tail -f ~/KioskTiger/kiosktiger.log
```

---

## Configuration

All runtime behaviour is controlled by `~/KioskTiger/kiosktiger.conf`. Lines beginning with `#` are comments. The file is watched at runtime — **save the file and changes take effect immediately** without restarting the service.

### Content source — use one or the other

| Key | Description |
|-----|-------------|
| `KIOSKHTML` | Path to a local HTML file to display (relative to the KioskTiger directory) |
| `KIOSKURL` | Full URL of a remote web page to load |

```ini
KIOSKHTML=KioskTiger.html
#KIOSKURL=https://example.com
```

### Sleep / screensaver

| Key | Default | Description |
|-----|---------|-------------|
| `SLEEPTIME` | `60` | Seconds of inactivity before sleep mode activates. Set to `0` to disable. |
| `DELAYTIME` | `3` | How often (seconds) the inactivity timer is checked |
| `KIOSKMSG` | — | Message displayed at the bottom of the sleep screen |
| `TXTCOLOR` | — | CSS color for `KIOSKMSG`, e.g. `#0055ff` or `red` |
| `SLEEPICON` | `default` | Path to a PNG/JPG image shown on the sleep screen. Use `default` for the built-in tiger icon. |
| `SLEEPICONTEXT` | — | Text shown on the sleep screen button. Supports `$n` (newline) and `$v` (app version). |

```ini
SLEEPTIME=60
DELAYTIME=3
KIOSKMSG=Touch screen to continue
TXTCOLOR=#ffffff
SLEEPICON=default
SLEEPICONTEXT=KioskTiger$nv$v
```

### Display

| Key | Description |
|-----|-------------|
| `PAGEZOOM` | Scale the entire page. `1.0` = 100%, `1.25` = 125%, `0.8` = 80% |
| `FONTSCALE` | Scale text only (may not work on all pages). Same scale as `PAGEZOOM`. |
| `USERAGENT` | Override the browser user agent string sent to web servers |

```ini
#PAGEZOOM=1.25
#FONTSCALE=1.0
USERAGENT=KioskTiger WebView
```

### Full example `kiosktiger.conf`

```ini
KIOSKHTML=KioskTiger.html
#KIOSKURL=https://example.com

SLEEPTIME=60
DELAYTIME=3

KIOSKMSG=Touch to wake
TXTCOLOR=#ffffff

#PAGEZOOM=1.25
USERAGENT=KioskTiger WebView

SLEEPICON=default
SLEEPICONTEXT=KioskTiger$nby$nKelly Wiles$n$v
```

---

## Project structure

```
KioskTiger/
├── src/application/
│   ├── Main.java                  # App entry point, stage setup
│   ├── KtGlobal.java              # Singleton state store
│   ├── KioskTigerController.java  # WebView controller, config loader
│   ├── SceneNav.java              # Scene router with history stack
│   ├── SceneNavController.java    # Root pane switcher
│   ├── SleepModeController.java   # Screensaver screen
│   ├── SleepModeThread.java       # (legacy — superseded by Timeline)
│   ├── ConfigWatcher.java         # Hot-reload via WatchService
│   ├── RefreshScene.java          # Interface for scene lifecycle hooks
│   ├── SceneInfo.java             # Scene metadata holder
│   ├── screens/
│   │   ├── KioskTiger.fxml        # Main WebView layout
│   │   ├── SceneNav.fxml          # Root navigation pane
│   │   └── SleepMode.fxml         # Sleep screen layout
│   └── application.css
├── kiosktiger.conf                # Runtime configuration
├── kiosktiger.sh                  # Launch script (sets JVM flags)
├── KioskTiger.service             # systemd unit file
├── KioskTiger.html                # Default local HTML page
└── kiosktiger.jar                 # Pre-built JAR
```

---

## Building from source

The project targets Java 11 with JavaFX modules. Import into Eclipse with the included `build.fxbuild` project file, or build from the command line:

```bash
javac --module-path /usr/lib/jvm/bellsoft-java11-full-aarch64/legal \
      --add-modules javafx.controls,javafx.fxml,javafx.web \
      -d out \
      $(find src -name "*.java")

jar --create --file kiosktiger.jar \
    --manifest manifest.mf \
    -C out .
```

---

## Troubleshooting

**Black screen on startup**
Check that the desktop GUI is fully disabled and no other process has a lock on `/dev/fb0`. Run `sudo systemctl status KioskTiger.service` and inspect `~/KioskTiger/kiosktiger.log`.

**`FileNotFoundException: default`**
Set `SLEEPICON=default` in your config (not a blank value). The word `default` is a sentinel that loads the built-in tiger icon from the JAR.

**Touch input not responding**
Verify the framebuffer device in `kiosktiger.sh` matches your display (`/dev/fb0` vs `/dev/fb1`). Some displays require `-Dmonocle.screen.fb=/dev/fb1`.

**Page loads then goes blank**
This usually indicates a JavaScript error on the page. Add `-Djavafx.userAgentStylesheetUrl=` to the JVM flags in `kiosktiger.sh` to expose more WebEngine diagnostics in the log.

**Wrong Java version**
Run `java -version` — it must report `11.x.x` from BellSoft. If another JDK is on `PATH`, set `JAVA_HOME` explicitly in `kiosktiger.sh`.

---

## License

MIT License — © 2023 Richard Kelly Wiles. See source files for full license text.
