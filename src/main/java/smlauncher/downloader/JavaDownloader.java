package smlauncher.downloader;

import org.apache.commons.io.FileUtils;
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

	public JavaDownloader(JavaVersion version) {
		this(OperatingSystem.getCurrent(), version);
	}

	public JavaDownloader(OperatingSystem currentOS, JavaVersion version) {
		this.currentOS = currentOS;
		this.version = version;
	}

	public void downloadAndUnzip() throws Exception {
		// The folder already exists, don't unzip
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

	public void unzip() throws Exception {
		// Unzip the file
		String zipFilename = getZipFilename();
		ZipFile zipFile = new ZipFile(zipFilename);
		unzipFile(zipFile, new File("./"));
		zipFile.close();

		// Delete the zip file
		File zip = new File(zipFilename);
		if (zip.exists()) zip.delete();

		moveExtractedFolder(getJreFolderName());
	}

	// TODO do we need other folders?
	// TODO maybe just rename /<home>/ to /jre<#>/
	private void moveExtractedFolder(String jreFolderName) throws IOException {
		File jreFolder = new File(getJreFolderName());

		if (currentOS == OperatingSystem.MAC) {
			// Actual java will be inside /Contents/Home/bin/
			// Copy /Contents/Home/bin/ to /jre<#>/
			// Go into the folder, and copy the contents of /Contents/Home/ to /jre<#>/
			File homeFolder = new File(jreFolderName + "/Contents/Home");
			File binFolder = new File(homeFolder.getName() + "/bin");
			FileUtils.copyDirectory(binFolder, jreFolder);

			// Delete the old folder
			FileUtils.deleteDirectory(homeFolder);
		} else {
			// Move the extracted folder to jre<#>
			for (File file : Objects.requireNonNull(new File("./").listFiles())) {
				if (file.getName().startsWith(version.fileStart)) {
					file.renameTo(jreFolder);
					break;
				}
			}
		}
	}

	// Helper Methods

	private boolean doesJreFolderExist() {
		File jreFolder = new File(getJreFolderName());
		return jreFolder.isDirectory();
	}

	private String getJavaURL() {
		return String.format(version.fmtURL, currentOS.toString(), currentOS.zipExtension);
	}

	String getZipFilename() {
		return String.format("jre%d.%s", version.number, currentOS.zipExtension);
	}

	String getJreFolderName() {
		return "./jre" + version.number;
	}

	// TODO problems unzipping tar.gz
	private void unzipFile(ZipFile zipFile, File destinationFile) {
		Enumeration<? extends ZipEntry> entries = zipFile.entries();

		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			File entryDestination = new File(destinationFile, entry.getName());
			entryDestination.getParentFile().mkdirs();
			if (entry.isDirectory()) continue;

			try (InputStream in = zipFile.getInputStream(entry);
				 OutputStream out = new FileOutputStream(entryDestination)) {
				byte[] buffer = new byte[1024];
				int len;
				while ((len = in.read(buffer)) > 0) {
					out.write(buffer, 0, len);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
