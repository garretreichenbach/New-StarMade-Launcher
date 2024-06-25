package smlauncher;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import smlauncher.misc.MiscErrorDialog;
import smlauncher.util.OperatingSystem;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Checks whether the launcher needs to be updated and runs the launcher updater.
 *
 * @author TheDerpGamer
 */
public class LauncherUpdaterHelper {

	private static final String DOWNLOAD_URL = "https://www.star-made.org/download";
	private static final String UPDATE_URL_BASE = "https://github.com/garretreichenbach/New-StarMade-Launcher/releases/download/v"; //Temp link for testing
	private static final String INDEX_URL = "https://raw.githubusercontent.com/garretreichenbach/New-StarMade-Launcher/main/versions.json"; //Temp link for testing

	/**
	 * Checks whether the launcher should be updated.
	 */
	public static boolean checkForUpdate() {
		String currentVersion = StarMadeLauncher.LAUNCHER_VERSION;
		String latestVersion = getLatestVersion();
		System.err.println("Current Launcher Version: " + currentVersion);
		System.err.println("Latest Launcher Version: " + latestVersion);
		return !currentVersion.equals(latestVersion);
	}

	/**
	 * Updates the launcher to the latest version and restarts it.
	 */
	public static void updateLauncher() {
		String latestVersion = getLatestVersion();
		System.err.println("Updating launcher to version " + latestVersion);
		try {
			// Copy updater jar
			File updaterJar = new File("Updater.jar");
			if (updaterJar.exists()) updaterJar.delete();
			extractUpdater(updaterJar);

			// Run updater jar
			// Send the link to the latest launcher jar file in args
			ProcessBuilder processBuilder = new ProcessBuilder(
					"java", "-jar", "Updater.jar",
					getLatestLauncherURL(), getFileName()
			);
			processBuilder.inheritIO();
			processBuilder.start();
			System.exit(0);
		} catch (Exception exception) {
			System.out.println("Could not update launcher");
			// Open website to launcher download
			new MiscErrorDialog(MiscErrorDialog.ErrorType.ERROR, "Failed to update launcher. Please download the new launcher manually.", () -> {
				try {
					Desktop.getDesktop().browse(new URL(DOWNLOAD_URL).toURI());
				} catch (Exception exception1) {
					System.out.println("Could not open launcher website");
				}
			}).setVisible(true);
		}
	}

	// Helper Methods

	private static String getLatestVersion() {
		try (InputStream stream = new URL(INDEX_URL).openStream()) {
			String indexJSON = new String(getBytesFromInputStream(stream), StandardCharsets.UTF_8);
			JSONObject index = new JSONObject(indexJSON);
			JSONArray versions = index.getJSONArray("versions");
			JSONObject latestVersion = versions.getJSONObject(versions.length() - 1);
			return latestVersion.getString("version");
		} catch (IOException exception) {
			System.out.println("Could not get latest version");
			return "UNKNOWN";
		}
	}

	private static String getLatestLauncherURL() {
		return UPDATE_URL_BASE + getLatestVersion() + "/" + getFileName();
	}

	private static String getFileName() {
		String s = "StarMade-Launcher-";
		OperatingSystem currentOS = OperatingSystem.getCurrent();
		if (currentOS == OperatingSystem.WINDOWS) s += "Windows.exe";
		else if (currentOS == OperatingSystem.MAC) s += "Mac";
		else s += "Linux";
		return s;
	}

	/**
	 * Extracts the updater .jar file from the launcher jar file.
	 *
	 * @param out the output file
	 */
	private static void extractUpdater(File out) {
		try (
				InputStream inputStream = StarMadeLauncher.class.getClassLoader().getResourceAsStream("Updater.jar");
				OutputStream outputStream = new FileOutputStream(out)
		) {
			assert inputStream != null;
			IOUtils.copy(inputStream, outputStream);
		} catch (Exception exception) {
			System.out.println("Could not extract updater jar");
		}
	}

	/**
	 * Copy InputStream to byte array
	 * <a href="https://stackoverflow.com/questions/1264709/convert-inputstream-to-byte-array-in-java">From</a>
	 */
	public static byte[] getBytesFromInputStream(InputStream is) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		byte[] buffer = new byte[0xFFFF];
		for (int len = is.read(buffer); len != -1; len = is.read(buffer)) {
			os.write(buffer, 0, len);
		}
		return os.toByteArray();
	}

}
