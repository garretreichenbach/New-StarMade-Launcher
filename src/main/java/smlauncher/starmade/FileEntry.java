package smlauncher.starmade;

import java.io.IOException;

public class FileEntry implements Comparable<FileEntry> {

	private final String checksum;
	String name;
	long size;
	private String version;

	public FileEntry(String name, long size, String checksum) {
		this.name = name;
		this.size = size;
		version = name;
		version = version.replace("starmade-build_", "");
		version = version.replace(".zip", "");
		this.checksum = checksum;
	}

	@Override
	public int compareTo(FileEntry arg0) {
		return name.compareToIgnoreCase(arg0.name);
	}

	public String getVersion() {
		return version;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "FileEntry [name=" + name + ", size=" + size + "]";
	}

	public void validateCheckSum() throws CheckSumFailedException, IOException {
		String sha1Checksum = null;
		sha1Checksum = FileUtil.getSha1Checksum(name);

		System.err.println("Checking checksum for " + name);
		System.err.println("star-made.org: " + checksum);
		System.err.println("downloaded file: " + sha1Checksum);

		if(!checksum.equals(sha1Checksum)) {
			throw new CheckSumFailedException("The downloaded mirror file didn't match the checksum give from star-made.org. The file might be modified!");
		}

	}

}