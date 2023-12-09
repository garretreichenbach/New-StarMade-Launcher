package smlauncher;

/**
 * A computer's operating system (OS).
 *
 * @author SlavSquatSuperstar
 */
public enum OperatingSystem {

	WINDOWS("zip"),
	MAC("tar.gz"),
	LINUX("tar.gz");

	public final String zipExtension; // The archive file extension

	OperatingSystem(String zipExtension) {
		this.zipExtension = zipExtension;
	}

	public static OperatingSystem getCurrent() {
		String osName = System.getProperty("os.name").toLowerCase();
		if (osName.contains("win")) return WINDOWS;
		else if (osName.contains("mac")) return MAC;
		else return LINUX;
	}

	@Override
	public String toString() {
		return this.name().toLowerCase();
	}

}
