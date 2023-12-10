package smlauncher.downloader;

import org.apache.commons.io.FileUtils;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.tar.TGZUnArchiver;
import org.codehaus.plexus.archiver.tar.TarGZipUnArchiver;
import org.codehaus.plexus.archiver.zip.ZipArchiver;
import org.codehaus.plexus.archiver.zip.ZipUnArchiver;
import smlauncher.JavaVersion;
import smlauncher.OperatingSystem;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Enumeration;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Downloads a JDK from the web and unzips it.
 *
 * @author SlavSquatSuperstar
 */
public class JavaDownloader {

	private final OperatingSystem currentOS;
	private final JavaVersion version;
	private String installDir;

	public JavaDownloader(JavaVersion version) {
		this(OperatingSystem.getCurrent(), version);
	}

	public JavaDownloader(OperatingSystem currentOS, JavaVersion version) {
		this.currentOS = currentOS;
		this.version = version;
		installDir = ".";
	}

	public JavaDownloader setInstallDir(String installDir) {
		this.installDir = installDir;
		return this;
	}

	public void downloadAndUnzip() throws IOException {
		// Don't unzip if the folder already exists
		if (doesJreFolderExist()) return;
		download();
		unzip();
	}

	public void download() throws IOException {
		String url = getJavaURL();
		if (url == null) return;

		URL website = new URL(url);
		ReadableByteChannel rbc = Channels.newChannel(website.openStream());

		String destination = getZipFilename();
		FileOutputStream fos = new FileOutputStream(destination);
		fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		fos.close();
	}

	public void unzip() throws IOException {
		String zipFilename = getZipFilename();
		File zipFile = new File(zipFilename);

		// Extract the file
		UnArchiver unzipper;
		if (currentOS.zipExtension.equals("zip")) {
			unzipper = new ZipUnArchiver(zipFile);
		} else {
			unzipper = new TarGZipUnArchiver(zipFile);
		}
		unzipper.setDestDirectory(new File("./"));
		unzipper.extract();

		cleanupZip(); // Delete the zip file
		moveExtractedFolder();
	}

	private void moveExtractedFolder() throws IOException {
		File jreFolder = new File(getJreFolderName());
		File extractedFolder = null;

		// Find the extracted folder
		for (File file : Objects.requireNonNull(new File("./").listFiles())) {
			if (file.getName().startsWith(version.fileStart)) {
				extractedFolder = file;
				break;
			}
		}
		if (extractedFolder == null) throw new IOException("Could not find extracted folder");

		// Rename the extracted folder to jre<#>/
		extractedFolder.renameTo(jreFolder);
	}

	// Helper Methods

	private boolean doesJreFolderExist() {
		File jreFolder = new File(getJreFolderName());
		return jreFolder.isDirectory();
	}

	private String getJavaURL() {
		return String.format(version.fmtURL, currentOS.toString(), currentOS.zipExtension);
	}

	private String getZipFilename() {
		return String.format("jre%d.%s", version.number, currentOS.zipExtension);
	}

	private String getJreFolderName() {
		return installDir + "/jre" + version.number;
	}

	void cleanupZip() {
		File zipFile = new File(getZipFilename());
		if (zipFile.exists()) zipFile.delete();
	}

	void cleanupFolder() {
		File jreFolder = new File(getJreFolderName());
		try {
			if (jreFolder.exists()) {
				FileUtils.cleanDirectory(jreFolder);
				FileUtils.deleteDirectory(jreFolder);
			}
		} catch (IOException ignored) {
		}
	}

}
