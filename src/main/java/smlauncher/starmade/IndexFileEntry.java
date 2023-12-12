package smlauncher.starmade;

public class IndexFileEntry implements Comparable<IndexFileEntry> {

	public final String build; // date
	public final String version; // semantic versioning
	public final String path; // download path
	public final GameBranch branch;

	// TODO build and version are swapped in usage
	public IndexFileEntry(String version, String build, GameBranch branch, String path) {
		this.build = build;
		this.version = version;
		this.branch = branch;
		this.path = path;
	}

	public static IndexFileEntry create(String line, GameBranch branch) {
		String[] versionAndPath = line.split(" ", 2);
		String[] versionAndBuild = versionAndPath[0].split("#", 2);
		String version = versionAndBuild[0];
		String build = (versionAndBuild.length == 2) ? versionAndBuild[1] : "";
		String path = versionAndPath[1];
		return new IndexFileEntry(version, build, branch, path);
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