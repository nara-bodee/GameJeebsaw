package online;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class ClientSessionManager {

    private static final String SESSION_FILE_NAME = "session.properties";
    private static final File SESSION_FILE = new File(getAppDataDir(), SESSION_FILE_NAME);

    private static File getAppDataDir() {
        String appName = "GameJeebsaw";
        String os = System.getProperty("os.name").toLowerCase();
        File appDataDir;
        if (os.contains("win")) {
            appDataDir = new File(System.getenv("APPDATA"), appName);
        } else if (os.contains("mac")) {
            appDataDir = new File(System.getProperty("user.home"), "Library/Application Support/" + appName);
        } else {
            appDataDir = new File(System.getProperty("user.home"), "." + appName);
        }
        if (!appDataDir.exists()) {
            appDataDir.mkdirs();
        }
        return appDataDir;
    }

    public static void saveSession(SessionInfo info) {
        Properties props = new Properties();
        props.setProperty("host", info.host());
        props.setProperty("port", String.valueOf(info.port()));
        props.setProperty("roomName", info.roomName());
        props.setProperty("playerName", info.playerName());
        props.setProperty("token", info.token());

        try (FileOutputStream out = new FileOutputStream(SESSION_FILE)) {
            props.store(out, "GameJeebsaw - Last Online Session");
        } catch (IOException e) {
            // Optional: log error
        }
    }

    public static SessionInfo loadSession() {
        if (!SESSION_FILE.exists()) {
            return null;
        }

        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(SESSION_FILE)) {
            props.load(in);
            String host = props.getProperty("host");
            int port = Integer.parseInt(props.getProperty("port", "0"));
            String roomName = props.getProperty("roomName");
            String playerName = props.getProperty("playerName");
            String token = props.getProperty("token");

            if (host != null && port > 0 && token != null) {
                return new SessionInfo(host, port, roomName, playerName, token);
            }
        } catch (IOException | NumberFormatException e) {
            // Optional: log error
        }
        return null;
    }

    public static void clearSession() {
        if (SESSION_FILE.exists()) {
            SESSION_FILE.delete();
        }
    }
}