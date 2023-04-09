package smlauncher.util;

public class FileDownloadUpdate {
	public long downloaded;
	public long size;
	public String fileName;
	public int index;
	public int total;
	public long totalSize;
	public long currentSize;
	public double downloadSpeed;

	@Override
	public String toString() {
		return "FileDownloadUpdate{" +
				"downloaded=" + downloaded +
				", size=" + size +
				", fileName='" + fileName + '\'' +
				", index=" + index +
				", total=" + total +
				", totalSize=" + totalSize +
				", currentSize=" + currentSize +
				", downloadSpeed=" + downloadSpeed +
				'}';
	}
}
