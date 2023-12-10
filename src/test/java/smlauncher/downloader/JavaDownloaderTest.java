package smlauncher.downloader;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Disabled;
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
	void canDownloadAndUnzipOnWindows() {
		testCanDownloadAndUnzip(OperatingSystem.WINDOWS, JavaVersion.JAVA_8);
		testCanDownloadAndUnzip(OperatingSystem.WINDOWS, JavaVersion.JAVA_18);
	}

	@Test
	void canDownloadAndUnzipOnMac() {
		testCanDownloadAndUnzip(OperatingSystem.MAC, JavaVersion.JAVA_8);
//		testCanDownloadAndUnzip(OperatingSystem.MAC, JavaVersion.JAVA_18);
	}

	@Test
	void canDownloadAndUnzipOnLinux() {
		testCanDownloadAndUnzip(OperatingSystem.LINUX, JavaVersion.JAVA_8);
		testCanDownloadAndUnzip(OperatingSystem.LINUX, JavaVersion.JAVA_18);
	}

	// Helper Methods
	
	private void testCanDownloadAndUnzip(OperatingSystem os, JavaVersion version) {
		dl = new JavaDownloader(os, version);
		assertDoesNotThrow(() -> dl.download());
		assertDoesNotThrow(() -> dl.unzip());
		cleanupZip();
		cleanupFolder();
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
