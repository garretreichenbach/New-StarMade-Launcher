package smlauncher;

/**
 * A version of the Java Runtime Environment (JRE).
 *
 * @author SlavSquatSuperstar
 */
public enum JavaVersion {

	JAVA_8(8, "https://github.com/adoptium/temurin8-binaries/releases/download/jdk8u392-b08/OpenJDK8U-jre_x64_%s_hotspot_8u392b08.%s"),
	JAVA_18(18, "https://github.com/adoptium/temurin18-binaries/releases/download/jdk-18.0.2.1%2B1/OpenJDK18U-jre_x64_%s_hotspot_18.0.2.1_1.%s");

	public final int value;
	public final String baseURL;

	JavaVersion(int value, String baseURL) {
		this.value = value;
		this.baseURL = baseURL;
	}

	public static JavaVersion getWithValue(int value) {
		if (value == 8) return JAVA_8;
		else if (value == 18) return JAVA_18;
		else return null;
	}

}
