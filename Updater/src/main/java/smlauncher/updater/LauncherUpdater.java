package smlauncher.updater;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.zip.ZipFile;

/**
 * Updates the launcher itself, not the game. Exported in a separate jar file within the launcher jar file.
 *
 * @author TheDerpGamer
 */
public class LauncherUpdater {

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
			//Delete the output file if it exists
			File outputFile = new File(output);
			if(outputFile.exists()) outputFile.delete();
			outputFile.createNewFile();

			//Download the file at the URL and write it to the output file
			IOUtils.copy(new URL(url).openStream(), new FileOutputStream(outputFile));
			System.out.println("Downloaded update to " + outputFile.getAbsolutePath());
			System.out.println("Updating launcher...");

			//Unzip the downloaded file
			unzip(outputFile);
			outputFile.delete();
			System.out.println("Finished updating launcher");

			//Restart the launcher
			System.out.println("Restarting launcher...");
			runLauncher(new File("./app/StarMade-Launcher.jar").getAbsolutePath());
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
			String javaPath = "./runtime/bin/java";
			if(System.getProperty("os.name").toLowerCase().contains("win")) javaPath += ".exe";
			ProcessBuilder processBuilder = new ProcessBuilder(javaPath, "-jar", absolutePath);
			processBuilder.inheritIO();
			processBuilder.start();
			System.exit(0);
		} catch(Exception exception) {
			exception.printStackTrace();
			System.exit(1);
		}
	}
}
