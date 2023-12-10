package smlauncher.downloader;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import smlauncher.JavaVersion;
import smlauncher.OperatingSystem;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Tests for {@link JavaDownloader} class.
 *
 * @author SlavSquatSuperstar
 */
public class JavaDownloaderTest {

	private JavaDownloader dl;

	@Test
	void canDownloadOnWindows() {
		dl = new JavaDownloader(OperatingSystem.WINDOWS, JavaVersion.JAVA_8);
		assertDoesNotThrow(() -> dl.download());
		cleanupZip();
	}

	@Test
	void canDownloadOnMac() {
		dl = new JavaDownloader(OperatingSystem.MAC, JavaVersion.JAVA_8);
		assertDoesNotThrow(() -> dl.download());
		cleanupZip();
	}

	@Test
	void canDownloadOnLinux() {
		dl = new JavaDownloader(OperatingSystem.LINUX, JavaVersion.JAVA_8);
		assertDoesNotThrow(() -> dl.download());
		cleanupZip();
	}

	@Test
	void canUnzipOnWindows() {
		dl = new JavaDownloader(OperatingSystem.WINDOWS, JavaVersion.JAVA_18);
		assertDoesNotThrow(() -> dl.downloadAndUnzip());
		cleanupFolder();
	}

	@Test
	void canUnzipOnLinux() {
		dl = new JavaDownloader(OperatingSystem.WINDOWS, JavaVersion.JAVA_18);
		assertDoesNotThrow(() -> dl.downloadAndUnzip());
		cleanupFolder();
	}

	@Test
	void canUnzipOnMac() {
		dl = new JavaDownloader(OperatingSystem.WINDOWS, JavaVersion.JAVA_18);
		assertDoesNotThrow(() -> dl.downloadAndUnzip());
		cleanupFolder();
	}

	// Helper Methods

	private void testCanDownload(OperatingSystem os, JavaVersion version) {
		dl = new JavaDownloader(os, version);
		assertDoesNotThrow(() -> dl.download());
		cleanupZip();
	}

	private void cleanupZip() {
		File zipFile = new File(dl.getZipFilename());
		if (zipFile.exists()) zipFile.delete();
	}

	private void cleanupFolder() {
		File jreFolder = new File(dl.getJreFolderName());
		try {
			if (jreFolder.exists()) {
				FileUtils.cleanDirectory(jreFolder);
				FileUtils.deleteDirectory(jreFolder);
			}
		} catch (IOException ignored) {
		}
	}

}
