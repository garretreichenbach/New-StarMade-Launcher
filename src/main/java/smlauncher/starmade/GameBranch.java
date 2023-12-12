package smlauncher.starmade;

/**
 * A branch of game versions.
 *
 * @author TheDerpGamer
 * @author SlavSquatSuperstar
 */
public enum GameBranch {

	RELEASE("Release", 0, "http://files.star-made.org/releasebuildindex"),
	DEV("Development", 1, "http://files.star-made.org/devbuildindex"),
	PRE("Pre-Release", 2, "http://files.star-made.org/preebuildindex"),
	ARCHIVE("Archive", -1, "http://files.star-made.org/archivebuildindex");

	public final String name, url; // download location
	public final int index; // index in drop-down

	GameBranch(String name, int index, String url) {
		this.name = name;
		this.index = index;
		this.url = url;
	}

	public static GameBranch getForVersion(IndexFileEntry gameVersion) {
		if (gameVersion == null || gameVersion.build == null) return RELEASE;
		switch (gameVersion.build) {
			case "DEV":
				return DEV;
			case "PRE":
				return PRE;
			default:
				return RELEASE;
		}
	}

	public static GameBranch getForIndex(int index) {
		switch (index) {
			case 1:
				return DEV;
			case 2:
				return PRE;
			default:
				return RELEASE;
		}
	}

}
