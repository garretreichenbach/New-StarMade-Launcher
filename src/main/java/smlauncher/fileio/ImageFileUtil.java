package smlauncher.fileio;

import smlauncher.StarMadeLauncher;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Objects;

/**
 * Reads image files.
 *
 * @author TheDerpGamer
 * @author SlavSquatSuperstar
 */
public final class ImageFileUtil {

	private ImageFileUtil() {
	}

	/**
	 * Reads an image file at the specified path.
	 *
	 * @param path the file location
	 * @return the image
	 * @throws IOException if the file does not exist
	 */
	public static Image readImage(String path) throws IOException {
		try {
			return ImageIO.read(Objects.requireNonNull(StarMadeLauncher.class.getResource(path)));
		} catch (IOException exception) {
			throw new IOException("Could not read image from file");
		}
	}

	/**
	 * Reads an image file at the specified path and resizes it.
	 *
	 * @param path   the file location
	 * @param width  the width in pixels
	 * @param height the height in pixels
	 * @return the scaled image
	 * @throws IOException if the file does not exist
	 */
	public static Image readImage(String path, int width, int height) throws IOException {
		try {
			Image image = readImage(path);
			return image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
		} catch (IOException exception) {
			throw new IOException("Could not read image from file");
		}
	}

	/**
	 * Creates an image icon from the given image file.
	 *
	 * @param path the file location
	 * @return the icon, blank if the file does not exist
	 */
	public static ImageIcon getIcon(String path) {
		try {
			Image image = readImage("/" + path);
			return new ImageIcon(image);
		} catch (IOException exception) {
			return new ImageIcon();
		}
	}

	/**
	 * Creates an image icon from the given image file and scales it.
	 *
	 * @param path   the file location
	 * @param width  the width in pixels
	 * @param height the height in pixels
	 * @return the scale icon, blank if the file does not exist
	 */
	public static ImageIcon getIcon(String path, int width, int height) {
		try {
			Image image = readImage("/" + path, width, height);
			return new ImageIcon(image);
		} catch (IOException exception) {
			return new ImageIcon();
		}
	}

}
