package smlauncher.util;

import org.apache.commons.io.FileUtils;
import smlauncher.StarMadeLauncher;

import java.io.File;
import java.io.InputStream;

/**
 * [Description]
 *
 * @author TheDerpGamer
 */
public class SetupUtil {

	/**
	 * Generates the HTML files for the launcher by copying them from the Jar.
	 */
	public static void generateHTMLFiles() {
		try {
			generateHTMLFile("html/news_template", "./launcher/html/news_template.html");
		} catch(Exception exception) {
			exception.printStackTrace();
		}
	}

	private static void generateHTMLFile(String path, String output) throws Exception {
		InputStream resourceAsStream = StarMadeLauncher.class.getClassLoader().getResourceAsStream(path);
		if(resourceAsStream == null) throw new IllegalArgumentException("Could not find resource: " + path);
		File file = new File(output);
		file.createNewFile();
		FileUtils.copyInputStreamToFile(resourceAsStream, file);
	}
}
