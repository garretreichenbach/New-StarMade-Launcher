package smlauncher.util;

public interface FileDowloadCallback {
	void update(FileDownloadUpdate u);

	void update(String u);
}
