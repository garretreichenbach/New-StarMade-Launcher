package smlauncher.fileio;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Reads and writes plain text files.
 *
 * @author SlavSquatSuperstar
 */
public final class TextFileUtil {

	private TextFileUtil() {
	}

	public static String readText(File file) throws IOException {
		try(FileReader reader = new FileReader(file, StandardCharsets.UTF_8)) {
			return IOUtils.toString(reader);
		} catch(IOException e) {
			throw new IOException("Could not read text from file");
		}
	}

	public static List<String> readLines(File file) throws IOException {
		try(FileReader reader = new FileReader(file, StandardCharsets.UTF_8)) {
			return IOUtils.readLines(reader);
		} catch(IOException e) {
			throw new IOException("Could not read lines from file");
		}
	}

	public static void writeText(File file, String text) throws IOException {
		try(FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
			IOUtils.write(text, writer);
			writer.flush();
		} catch(IOException exception) {
			throw new IOException("Could not save text to file");
		}
	}

	public static void writeLines(File file, List<String> lines) throws IOException {
		try(FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
			IOUtils.writeLines(lines, "\n", writer);
			writer.flush();
		} catch(IOException exception) {
			throw new IOException("Could not save lines to file");
		}
	}

}
