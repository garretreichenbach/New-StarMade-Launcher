package smlauncher.updater;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.zip.ZipFile;

/**
 * Updater for updating the launcher itself, not the game. Exported in a separate jar file within the launcher jar file.
 *
 * @author TheDerpGamer
 */
public class Updater {

	public static void main(String[] args) {
		try {
			if(args.length == 0) throw new IllegalArgumentException("No URL specified");
		} catch(IllegalArgumentException exception) {
			exception.printStackTrace();
			System.exit(-1);
		}
		String url = args[0];
		String output = args[1];
		System.out.println("Downloading update from " + url);

		try {
			File outputFile = new File(output);
			if(outputFile.exists()) outputFile.delete();
			outputFile.createNewFile();
			//Download the file at the URL and write it to the output file
			IOUtils.copy(new URL(url).openStream(), new FileOutputStream(outputFile));
			System.out.println("Downloaded update to " + outputFile.getAbsolutePath());
			System.out.println("Updating launcher...");

			unzip(outputFile);
			File launcherJar = new File("starmade-launcher.jar");
			if(launcherJar.exists()) launcherJar.delete();
			File folder = null;
			String os = System.getProperty("os.name").toLowerCase();
			if(os.contains("win")) folder = new File("StarMade_Launcher_Windows/release-builds/StarMade Launcher-win32-ia32");
			else if(os.contains("mac")) folder = new File("StarMade_Launcher_Mac/release-builds/StarMade Launcher-darwin-x64");
			else if(os.contains("linux")) folder = new File("StarMade_Launcher_Linux/release-builds/StarMade Launcher-linux-x64");
			//Move everything in the folder to current directory
			for(File file : folder.listFiles()) {
				if(file.isDirectory()) {
					for(File subFile : file.listFiles()) subFile.renameTo(new File(subFile.getName()));
				} else file.renameTo(new File(file.getName()));
			}
			outputFile.delete();
			(new File("StarMade_Launcher_Windows")).delete();
			(new File("StarMade_Launcher_Mac")).delete();
			(new File("StarMade_Launcher_Linux")).delete();

			//Restart the launcher
			runLauncher(new File("starmade-launcher.jar").getAbsolutePath());
		} catch(Exception exception) {
			exception.printStackTrace();
			System.exit(-1);
		}
	}

	private static void unzip(File file) {
		try {
			ZipFile zipFile = new ZipFile(file);
			zipFile.stream().forEach(zipEntry -> {
				try {
					File outputFile = new File(zipEntry.getName());
					if(zipEntry.isDirectory()) outputFile.mkdirs();
					else {
						if(outputFile.exists()) outputFile.delete();
						outputFile.createNewFile();
						IOUtils.copy(zipFile.getInputStream(zipEntry), new FileOutputStream(outputFile));
					}
				} catch(Exception exception) {
					exception.printStackTrace();
				}
			});
			zipFile.close();
		} catch(Exception exception) {
			exception.printStackTrace();
		}
	}

	private static void runLauncher(String absolutePath) {
		try {
			Runtime.getRuntime().exec("java -jar \"" + absolutePath + "\"");
			System.exit(0);
		} catch(Exception exception) {
			exception.printStackTrace();
		}
	}
}
