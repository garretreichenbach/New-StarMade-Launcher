package smlauncher.starmade;

import smlauncher.LogManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;

public class VersionContainer {
	private static final String versionFile = "version.txt";
	private static final String devVersionFile = "version_dev";
	public static Version VERSION = new Version(0, 0, 0);
	public static String build = "undefined";
	public static String OS;

	public static void loadVersion() {
		loadVersion("");
	}

	public static boolean is64Bit() {
		return System.getProperty("os.arch").contains("64");
	}

	public static void loadVersion(String installDir) {

		if(!installDir.isEmpty() && !installDir.endsWith(File.separator)) {
			installDir += File.separator;
		} else if(installDir.isEmpty()) {
			installDir = "." + File.separator;
		}
		System.err.println("[VERSION] loading version from install dir: " + installDir);
		try {
			File f = new File(installDir + devVersionFile);
			VERSION = new Version(0, 0, 0);
			build = "undefined";
			if(f.exists()) {
				BufferedReader b = new BufferedReader(new FileReader(f, StandardCharsets.UTF_8));

				VERSION = Version.parseFrom(b.readLine());
				build = "latest";
				b.close();

			} else {
				f = new File(installDir + versionFile);
				if(f.exists()) {
					BufferedReader b = new BufferedReader(new FileReader(f, StandardCharsets.UTF_8));
					String[] st = b.readLine().split("#");
					VERSION = Version.parseFrom(st[0].trim());
					build = st[1].trim();
					b.close();
				}
			}

			if(build == null) {
				throw new VersionNotFoundException("no version file found, or coudn't parse");
			}
		} catch(Exception e) {
			LogManager.logException("Error loading version", e);
		}
		System.out.println("[VERSION] VERSION: " + VERSION);
		System.out.println("[VERSION] BUILD: " + build);
	}

	public static boolean equalVersion(Version version) {
		return VERSION.compareTo(version) == 0;
	}

	/**
	 * @param version
	 * @return 1 if provided version is bigger, -1 if provided version is smaller, 0 if equal to current version
	 */
	public static int compareVersion(Version version) {
		if(version == null) {
			return -1;
		}
		return compareVersion(VERSION, version);
	}

	public static int compareVersion(Version own, Version version) {

		return own.compareTo(version);
	}

	public static boolean isDev() {
		return "latest".equals(build);
	}
}
