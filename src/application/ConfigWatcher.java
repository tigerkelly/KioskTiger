// new file: src/application/ConfigWatcher.java

package application;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

public class ConfigWatcher {

    private final Path configPath;
    private final Runnable onChanged;
    private Thread watchThread;
    private volatile boolean running = false;

    public ConfigWatcher(String configFilePath, Runnable onChanged) {
        this.configPath = Paths.get(configFilePath).toAbsolutePath();
        this.onChanged  = onChanged;
    }

    public void start() {
        if (running) return;
        running = true;

        watchThread = new Thread(() -> {
            try (WatchService watcher = FileSystems.getDefault().newWatchService()) {

                Path dir = configPath.getParent();
                dir.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);

                System.out.println("ConfigWatcher: watching " + dir);

                while (running) {
                    WatchKey key;
                    try {
                        key = watcher.take();   // blocks until an event
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }

                    for (WatchEvent<?> event : key.pollEvents()) {
                        if (event.kind() == StandardWatchEventKinds.OVERFLOW) continue;

                        Path changed = dir.resolve((Path) event.context());

                        // Only react to our specific config file
                        if (!changed.equals(configPath)) continue;

                        System.out.println("ConfigWatcher: change detected in " + configPath.getFileName());

                        // Small debounce — editors often write files twice in quick succession
                        try { Thread.sleep(200); } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }

                        onChanged.run();
                    }

                    if (!key.reset()) {
                        System.out.println("ConfigWatcher: watch key invalidated, stopping.");
                        break;
                    }
                }

            } catch (IOException e) {
                System.out.println("ConfigWatcher: failed to start — " + e.getMessage());
            }
        }, "config-watcher");

        watchThread.setDaemon(true);   // won't block JVM shutdown
        watchThread.start();
    }

    public void stop() {
        running = false;
        if (watchThread != null) watchThread.interrupt();
    }
}
