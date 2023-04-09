package smlauncher.util;

public class IndexFileEntry implements Comparable<IndexFileEntry> {

	public final String path;
	public final String version;
	public final String build;
	public final Updater.VersionFile v;

	public IndexFileEntry(String path, String version, String build, Updater.VersionFile v) {
		this.path = path;
		this.version = version;
		this.build = build;
		this.v = v;
	}

	@Override
	public int compareTo(IndexFileEntry arg0) {
		return build.compareToIgnoreCase(arg0.build);
	}

	@Override
	public String toString(){
		return build;
	}

	public String toStringFull() {
		return build + " v" + version + " (" + v.name() + ")";
	}

}