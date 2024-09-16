package smlauncher;

/**
 * Information about the download progress of the game.
 *
 * @author SlavSquatSuperstar
 */
// TODO merge with FileDownloadUpdate
public class DownloadStatus {
	private float installProgress;
	private String filename = "None";
	private long downloadedMb, totalMb, speedMb;

	// Setter Methods

	private static String formatBytes(long bytes) {
		if(bytes < 1024) return bytes + " B";
		else if(bytes < 1024 * 1024) return bytes / 1024 + " KB";
		else if(bytes < 1024 * 1024 * 1024) return bytes / (1024 * 1024) + " MB";
		else return bytes / (1024 * 1024 * 1024) + " GB";
	}

	public void setInstallProgress(float installProgress) {
		this.installProgress = installProgress;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public void setDownloadedMb(long downloadedMb) {
		this.downloadedMb = downloadedMb;
	}

	public void setTotalMb(long totalMb) {
		this.totalMb = totalMb;
	}

	// To String Methods

	public void setSpeedMb(long speedMb) {
		this.speedMb = speedMb;
	}

	@Override
	public String toString() {
		return String.format(
				"Updating... [%d%%]\nDownloading %s [%s / %s] at %s/s",
				(int) (installProgress * 100), filename, formatBytes(downloadedMb), formatBytes(totalMb), formatBytes(speedMb)
		);
	}
}
