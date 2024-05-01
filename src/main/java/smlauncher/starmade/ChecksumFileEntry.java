package smlauncher.starmade;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;

/**
 * A checksum entry for an individual game file, used to compare changes between
 * releases.
 *
 * @author TheDerpGamer
 */
public class ChecksumFileEntry {
	public final long size;
	public final String checksum;
	public final String relativePath;
	protected int index;

	public ChecksumFileEntry(long size, String checksum, String relativePath) {
		this.size = size;
		this.checksum = checksum;
		this.relativePath = relativePath.replaceFirst("\\.", "");
	}

	public void download(boolean force, String buildPath, String installDirStr, FileDownloadCallback cb, FileUpdateTotal o) throws NoSuchAlgorithmException, IOException {
		String sourceFilePath = buildPath + relativePath;
		File dst = new File(installDirStr, relativePath);

		printUpdaterMessage(String.format("Downloading %s -> %s", sourceFilePath, dst.getAbsolutePath()));
		boolean replace = needsDownload(installDirStr) || force;

		if (dst.exists() && replace) {
			if (!dst.delete()) {
				throw new IOException("File " + dst.getAbsolutePath() + " could not be removed! Is it still in use?");
			}
		}
		if (!dst.getParentFile().exists()) {
			System.err.println("Creating path: " + dst.getParentFile().getAbsolutePath());
		}

		dst.getParentFile().mkdirs();

		String name = dst.getName();

		File file = new File(dst.getAbsolutePath() + ".filepart");
		//remove file part
		file.delete();
//		final long[] update = new long[2];
		FileDownloadUpdate e = new FileDownloadUpdate();
		try {
			FileUtil.copyURLToFile(FileUtil.convertToURLEscapingIllegalCharacters(sourceFilePath), file, 50000, 50000, new DownloadCallback() {

				@Override
				public void doneDownloading() {
					int s = Integer.MAX_VALUE;
					synchronized (ChecksumFile.running) {
						for (ChecksumFileEntry en : ChecksumFile.running) {
							s = Math.min(en.index, s);
						}
					}
					if (s == index) {
						cb.done(e);
					} else {

//						System.err.println(s+" INDNN "+index+": "+ChecksumFile.running);
					}
				}

				@Override
				public void downloaded(long size, long diff) {

					o.lastSpeedSize += diff;

					o.currentSize += diff;
					e.downloaded = size;
					e.size = ChecksumFileEntry.this.size;
					e.fileName = name;
					e.index = o.index;
					e.total = o.total;
					e.totalSize = o.totalSize;
					e.currentSize = o.currentSize;

					long diffTime = System.currentTimeMillis() - o.startTime;
					if (diffTime / 200 > 1) {
						double secs = diffTime / 200.0d;

						o.downloadSpeed = o.lastSpeedSize / secs;
						o.lastSpeedSize = 0;
						o.startTime = System.currentTimeMillis();

					}
					e.downloadSpeed = o.downloadSpeed;

					int s = Integer.MAX_VALUE;
					synchronized (ChecksumFile.running) {
						for (ChecksumFileEntry en : ChecksumFile.running) {
							s = Math.min(en.index, s);
						}
					}
					if (s == index) {
//						System.err.println("INDNN "+index+": "+ChecksumFile.running);
						cb.update(e);
					} else {
//						System.err.println(s+" INDNN "+index+": "+ChecksumFile.running);
					}
				}
			}, "dev", "dev", true);

			file.renameTo(dst);
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
			throw new IOException(e1);
		}
	}

	public boolean needsDownload(String installDirStr) throws IOException {
		File dst = new File(installDirStr, relativePath);
		if (dst.exists()) {
			String localChecksum = FileUtil.getSha1Checksum(dst.getAbsolutePath());

			if (localChecksum.equals(checksum)) {
				printUpdaterMessage(String.format("Not downloading %s; remote file same as local", relativePath));
				return false;
			} else {
				printUpdaterMessage(String.format("Downloading %s; checksum differs %s :: %s", relativePath, localChecksum, checksum));
				return true;
			}
		} else {
			printUpdaterMessage(String.format("Downloading %s; local does not exist", dst.getAbsolutePath()));
			return true;
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return index;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return index == ((ChecksumFileEntry) obj).index;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ChecksumFileEntry [size=" + size + ", checksum=" + checksum
				+ ", relativePath=" + relativePath + ", index " + index + "]";
	}

	private static void printUpdaterMessage(String message) {
		if (GameUpdater.PRINT_DOWNLOAD_LOGS) {
			System.err.println("[UPDATER] " + message);
		}
	}

}
