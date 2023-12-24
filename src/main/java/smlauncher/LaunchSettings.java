package smlauncher;

import org.json.JSONObject;
import smlauncher.fileio.TextFileUtil;

import java.io.File;
import java.io.IOException;

/**
 * Stores launch settings and manages the settings file.
 *
 * @author SlavSquatSuperstar
 */
public final class LaunchSettings {

	// TODO store more properties

	private static JSONObject launchSettings;

	private LaunchSettings() {
	}

	private static final String SETTINGS_FILENAME = "launch-settings.json";

	// Settings File Methods

	public static JSONObject getLaunchSettings() {
		File jsonFile = new File(SETTINGS_FILENAME);
		JSONObject defaultSettings = getDefaultLaunchSettings();

		// Create file if not present
		if (!jsonFile.exists()) {
			launchSettings = defaultSettings;
			saveLaunchSettings(defaultSettings);
		} else {
			// Read the settings file
			try {
				JSONObject jsonObject = new JSONObject(TextFileUtil.readText(jsonFile));
				LaunchSettings.launchSettings = jsonObject;
				return jsonObject;
			} catch (IOException e) {
				System.out.println("Could not read launch settings from file");
			}
		}
		return defaultSettings;
	}

	public static void saveLaunchSettings(JSONObject settings) {
		File settingsFile = new File(SETTINGS_FILENAME);
		try {
			TextFileUtil.writeText(settingsFile, settings.toString());
		} catch (IOException exception) {
			System.out.println("Could not save launch settings to file");
		}
	}

	private static JSONObject getDefaultLaunchSettings() {
		JSONObject settings = new JSONObject();
		settings.put("memory", 4096);
		settings.put("launchArgs", "");
		settings.put("installDir", "./StarMade");
		settings.put("lastUsedBranch", 0); // Release
		settings.put("lastUsedVersion", "NONE");
		settings.put("jvm_args", "");
		return settings;
	}

	// Settings Getters and Setters

	public static String getInstallDir() {
		return launchSettings.getString("installDir");
	}

	public static void setInstallDir(String installDir) {
		launchSettings.put("installDir", installDir);
	}

}
