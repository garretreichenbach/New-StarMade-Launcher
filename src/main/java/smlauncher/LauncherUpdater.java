package smlauncher;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Updater for updating the launcher itself, not the game.
 *
 * @author TheDerpGamer
 */
public class LauncherUpdater {

	private static final String UPDATE_URL = "http://launcher-files-origin.star-made.org/launcherbuildindex";

	public static boolean checkForUpdate() {
		String currentVersion = StarMadeLauncher.LAUNCHER_VERSION;
		String latestVersion = getLatestVersion();
		System.err.println("Current Launcher Version: " + currentVersion);
		System.err.println("Latest Launcher Version: " + latestVersion);
		return !currentVersion.equals(latestVersion);
	}

	private static String getLatestVersion() {
		try {
			String allVersions = new String(new URL(UPDATE_URL).openStream().readAllBytes(), StandardCharsets.UTF_8);
			System.err.println(allVersions);
			String[] versions = allVersions.split("\n");
			return versions[versions.length - 1].split("#")[0].trim();
		} catch(IOException exception) {
			exception.printStackTrace();
			return "UNKNOWN";
		}
	}
}
