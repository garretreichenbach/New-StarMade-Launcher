package smlauncher.starmade;

public class IndexFileEntry implements Comparable<IndexFileEntry> {

	public final String path;
	public final String version;
	public final String build;
	public final GameBranch branch;

	public IndexFileEntry(String path, String version, String build, GameBranch branch) {
		this.path = path;
		this.version = version;
		this.build = build;
		this.branch = branch;
	}

	@Override
	public int compareTo(IndexFileEntry arg0) {
		return build.compareToIgnoreCase(arg0.build);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return build + " v" + version + " (" + branch.name() + ")";
	}

}