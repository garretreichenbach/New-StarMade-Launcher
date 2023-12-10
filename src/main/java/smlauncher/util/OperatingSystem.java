package smlauncher.util;

/**
 * A computer's operating system (OS).
 *
 * @author SlavSquatSuperstar
 */
public enum OperatingSystem {

	WINDOWS("zip", "jre%d/bin/java.exe"),
	MAC("tar.gz", "jre%d/Contents/Home/bin/java"),
	LINUX("tar.gz", "jre%d/bin/java");

	public final String zipExtension; // The archive file extension
	public final String javaPath; // Path to the java executable

	OperatingSystem(String zipExtension, String javaPath) {
		this.zipExtension = zipExtension;
		this.javaPath = javaPath;
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
