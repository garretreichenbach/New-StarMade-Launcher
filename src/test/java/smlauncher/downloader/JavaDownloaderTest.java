package smlauncher.downloader;

import org.junit.jupiter.api.Test;
import smlauncher.util.OperatingSystem;

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
		dl.cleanupZip();
		dl.cleanupFolder();
	}

}
