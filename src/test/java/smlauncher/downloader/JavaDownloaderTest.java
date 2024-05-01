package smlauncher.downloader;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import smlauncher.LaunchSettings;
import smlauncher.util.OperatingSystem;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests for {@link JavaDownloader} class.
 *
 * @author SlavSquatSuperstar
 */
public class JavaDownloaderTest {

	private static final String DOWNLOAD_FOLDER_NAME = "./JavaDownloaderTest";

	private JavaDownloader dl;

	@BeforeAll
	static void setDownloadFolder() {
		LaunchSettings.setInstallDir(DOWNLOAD_FOLDER_NAME);
	}

	@Test
	void canDownloadAndUnzipOnWindows() {
		testCanDownloadAndUnzip(OperatingSystem.WINDOWS, JavaVersion.JAVA_8);
		testCanDownloadAndUnzip(OperatingSystem.WINDOWS, JavaVersion.JAVA_18);
	}

	@Test
	void canDownloadAndUnzipOnMac() {
		testCanDownloadAndUnzip(OperatingSystem.MAC, JavaVersion.JAVA_8);
		testCanDownloadAndUnzip(OperatingSystem.MAC, JavaVersion.JAVA_18);
	}

	@Test
	void canDownloadAndUnzipOnLinux() {
		testCanDownloadAndUnzip(OperatingSystem.LINUX, JavaVersion.JAVA_8);
		testCanDownloadAndUnzip(OperatingSystem.LINUX, JavaVersion.JAVA_18);
	}

	@AfterAll
	static void removeDownloadFolder() {
		File downloadFolder = new File(DOWNLOAD_FOLDER_NAME);
		try {
			if (downloadFolder.isDirectory()) {
				FileUtils.cleanDirectory(downloadFolder);
				FileUtils.deleteDirectory(downloadFolder);
			}
		} catch (IOException e) {
		}
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
