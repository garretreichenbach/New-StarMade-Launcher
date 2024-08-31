package smlauncher.starmade;

public interface DownloadCallback {
	void downloaded(long size, long diff);

	void doneDownloading();
}
