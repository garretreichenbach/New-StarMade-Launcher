package smlauncher.updater;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.zip.ZipEntry;
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

			//It should download as a .zip file, so we need to extract it
			ZipFile zipFile = new ZipFile(outputFile);
			for(Object object : zipFile.stream().toArray()) {
				ZipEntry entry = (ZipEntry) object;
				File file = new File(outputFile.getParentFile(), entry.getName());
				if(entry.isDirectory()) file.mkdirs();
				else {
					file.getParentFile().mkdirs();
					IOUtils.copy(zipFile.getInputStream(entry), new FileOutputStream(file));
				}
			}
			outputFile.delete();

			//Restart the launcher
			runLauncher(new File("starmade-launcher.jar").getAbsolutePath());
		} catch(Exception exception) {
			exception.printStackTrace();
			System.exit(-1);
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
