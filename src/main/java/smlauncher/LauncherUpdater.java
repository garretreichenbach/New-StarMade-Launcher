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
//	private static final String UPDATE_URL = "http://launcher-files-origin.star-made.org/launcherbuildindex";
	private static final String UPDATE_URL_BASE = ""; //Temp link for testing
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
			File updaterJar = File.createTempFile("Updater", ".jar");
			updaterJar.deleteOnExit();
			extractUpdater(updaterJar);
			//Send the link to the latest launcher jar file in args
			ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", getLatestLauncherURL(), "StarMade Launcher" + getPlatformFolder() + ".zip");
			processBuilder.inheritIO();
			processBuilder.start();
			System.exit(0);
		} catch(IOException exception) {
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

	private static String getOutputPath() {
		String osName = System.getProperty("os.name").toLowerCase();
		if(osName.contains("win")) return "./starmade-launcher.exe";
		else if(osName.contains("mac")) return "./starmade-launcher.app";
		else return "./starmade-launcher.jar";
	}

	private static String getLatestLauncherURL() {
		return UPDATE_URL_BASE + getLatestVersion() + "/StarMade Launcher" + getPlatformFolder() + ".zip"; //Temp link for testing
	}

	private static String getPlatformFolder() {
		String osName = System.getProperty("os.name").toLowerCase();
		if(osName.contains("win")) return "-win32-ia32";
		else if(osName.contains("mac")) return "-darwin-x64";
		else return "-linux-x64";
	}

	private static String getPlatformExtension() {
		String osName = System.getProperty("os.name").toLowerCase();
		if(osName.contains("win")) return ".exe";
		else if(osName.contains("mac")) return ".app";
		else return ".jar";
	}

	/**
	 * Extracts the updater .jar file from the launcher jar file.
	 * @param out the output file
	 */
	public static void extractUpdater(File out) {
		try {
			InputStream inputStream = StarMadeLauncher.class.getResourceAsStream("/Updater.jar");
			OutputStream outputStream = new FileOutputStream(out);
			assert inputStream != null;
			IOUtils.copy(inputStream, outputStream);
			inputStream.close();
			outputStream.close();
		} catch(IOException exception) {
			exception.printStackTrace();
		}
	}
}
