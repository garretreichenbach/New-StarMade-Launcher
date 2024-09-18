package smlauncher.starmade;

public interface FileDownloadCallback {
	void update(FileDownloadUpdate u);

	void update(String u);

	void done(FileDownloadUpdate u);
}
