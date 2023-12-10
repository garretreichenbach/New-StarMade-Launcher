package smlauncher.downloader;

import org.junit.jupiter.api.Test;
import smlauncher.JavaVersion;
import smlauncher.OperatingSystem;

import java.io.File;

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
		testCanDownload(OperatingSystem.WINDOWS, JavaVersion.JAVA_8);
		testCanDownload(OperatingSystem.WINDOWS, JavaVersion.JAVA_18);
	}

	@Test
	void canDownloadOnMac() {
		testCanDownload(OperatingSystem.MAC, JavaVersion.JAVA_8);
		testCanDownload(OperatingSystem.MAC, JavaVersion.JAVA_18);
	}

	@Test
	void canDownloadOnLinux() {
		testCanDownload(OperatingSystem.LINUX, JavaVersion.JAVA_8);
		testCanDownload(OperatingSystem.LINUX, JavaVersion.JAVA_18);
	}

	// Helper Methods

	private void cleanup() {
		File zipFile = new File(dl.getZipFilename());
		if (zipFile.exists()) zipFile.delete();
	}

	private void testCanDownload(OperatingSystem os, JavaVersion version) {
		dl = new JavaDownloader(os, version);
		assertDoesNotThrow(() -> dl.download());
		cleanup();
	}

}
