package smlauncher;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import smlauncher.misc.ErrorDialog;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Updater for updating the launcher itself, not the game.
 *
 * @author TheDerpGamer
 */
public class LauncherUpdater {

	private static final String DOWNLOAD_URL = "https://www.star-made.org/download";
	private static final String UPDATE_URL_BASE = "https://github.com/garretreichenbach/New-StarMade-Launcher/releases/download/v"; //Temp link for testing
	private static final String INDEX_URL = "https://raw.githubusercontent.com/garretreichenbach/New-StarMade-Launcher/main/versions.json"; //Temp link for testing

	public static boolean checkForUpdate() {
		String currentVersion = StarMadeLauncher.LAUNCHER_VERSION;
		String latestVersion = getLatestVersion();
		System.err.println("Current Launcher Version: " + currentVersion);
		System.err.println("Latest Launcher Version: " + latestVersion);
		return !currentVersion.equals(latestVersion);
	}

	private static String getLatestVersion() {
		try {
			String indexJSON = new String(new URL(INDEX_URL).openStream().readAllBytes(), StandardCharsets.UTF_8);
			System.err.println(indexJSON);
			JSONObject index = new JSONObject(indexJSON);
			JSONArray versions = index.getJSONArray("versions");
			JSONObject latestVersion = versions.getJSONObject(versions.length() - 1);
			return latestVersion.getString("version");
		} catch(IOException exception) {
			exception.printStackTrace();
			return "UNKNOWN";
		}
	}

	/**
	 * Updates the launcher to the latest version and restarts it.
	 */
	public static void updateLauncher() {
		String latestVersion = getLatestVersion();
		System.err.println("Updating launcher to version " + latestVersion);
		try {
			File updaterJar = new File("Updater.jar");
			if(updaterJar.exists()) updaterJar.delete();
			updaterJar.createNewFile();
			try {
				extractUpdater(updaterJar);
				//Send the link to the latest launcher jar file in args
				ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", "Updater.jar", getLatestLauncherURL(), "StarMade_Launcher_" + getPlatformFolder() + ".zip");
				processBuilder.inheritIO();
				processBuilder.start();
			} catch(Exception e) {
				throw new RuntimeException(e);
			}
			System.exit(0);
		} catch(Exception exception) {
			exception.printStackTrace();
			(new ErrorDialog(ErrorDialog.ErrorType.ERROR, "Failed to update launcher. Please download the new launcher manually.", () -> {
				try {
					Desktop.getDesktop().browse(new URL(DOWNLOAD_URL).toURI());
				} catch(Exception exception1) {
					exception1.printStackTrace();
				}
			})).setVisible(true);
		}
	}

	private static String getLatestLauncherURL() {
		return UPDATE_URL_BASE + getLatestVersion() + "/StarMade_Launcher_" + getPlatformFolder() + ".zip";
	}

	private static String getPlatformFolder() {
		String osName = System.getProperty("os.name").toLowerCase();
		if(osName.contains("win")) return "Windows";
		else if(osName.contains("mac")) return "Mac";
		else return "Linux";
	}

	/**
	 * Extracts the updater .jar file from the launcher jar file.
	 * @param out the output file
	 */
	public static void extractUpdater(File out) {
		try {
			InputStream inputStream = StarMadeLauncher.class.getClassLoader().getResourceAsStream("Updater.jar");
			OutputStream outputStream = new FileOutputStream(out);
			assert inputStream != null;
			IOUtils.copy(inputStream, outputStream);
			inputStream.close();
			outputStream.close();
		} catch(Exception exception) {
			exception.printStackTrace();
		}
	}
}
