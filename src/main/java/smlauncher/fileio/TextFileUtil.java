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

	/**
	 * Read a single string from a text file
	 *
	 * @param file the text file
	 * @return the string
	 * @throws IOException if the file is not found
	 */
	public static String readText(File file) throws IOException {
		try (FileReader reader = new FileReader(file)) {
			return IOUtils.toString(reader);
		} catch (IOException e) {
			throw new IOException("Could not read text from file");
		}
	}

	/**
	 * Read an array of strings from a text file
	 *
	 * @param file the text file
	 * @return the lines
	 * @throws IOException if the file is not found
	 */
	public static List<String> readLines(File file) throws IOException {
		try (FileReader reader = new FileReader(file)) {
			return IOUtils.readLines(reader);
		} catch (IOException e) {
			throw new IOException("Could not read lines from file");
		}
	}

	/**
	 * Write a single string to a text file and override its contents.
	 *
	 * @param file the text file
	 * @param text the string
	 * @throws IOException if the file cannot be written to
	 */
	public static void writeText(File file, String text) throws IOException {
		try (FileWriter writer = new FileWriter(file)) {
			IOUtils.write(text, writer);
			writer.flush();
		} catch (IOException exception) {
			throw new IOException("Could not save text to file");
		}
	}

	/**
	 * Write an array of strings to a text file and override its contents.
	 *
	 * @param file  the text file
	 * @param lines the lines
	 * @throws IOException if the file cannot be written to
	 */
	public static void writeLines(File file, List<String> lines) throws IOException {
		try (FileWriter writer = new FileWriter(file)) {
			IOUtils.writeLines(lines, "\n", writer);
			writer.flush();
		} catch (IOException exception) {
			throw new IOException("Could not save lines to file");
		}
	}

}
