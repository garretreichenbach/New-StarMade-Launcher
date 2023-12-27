package smlauncher.starmade;

/**
 * Represents a specific release of StarMade.
 *
 * @author TheDerpGamer
 */
public class IndexFileEntry implements Comparable<IndexFileEntry> {

	/**
	 * The date this release was created.
	 */
	public final String build;

	/**
	 * The semantic versioning of this release.
	 */
	public final String version;

	/**
	 * The branch this release belongs to.
	 */
	public final GameBranch branch;
	/**
	 * The download path of this release.
	 */
	public final String path;

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
	public int compareTo(IndexFileEntry other) {
		return build.compareToIgnoreCase(other.build);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s v%s (%s)", build, version, branch.name());
	}

}