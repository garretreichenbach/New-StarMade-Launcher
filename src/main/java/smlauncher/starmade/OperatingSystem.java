package smlauncher.starmade;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public enum OperatingSystem {
	LINUX("linux", "unix"), SOLARIS("sunos", "solaris"), WINDOWS("win"), MAC("mac"), UNKNOWN;

	public static OperatingSystem OS;
	public final String[] ids;
	public String serial;

	OperatingSystem(String... ids) {
		this.ids = ids;
	}

	public static File getAppDir() throws IOException {
		return getAppDir("StarMade");
	}

	/**
	 * @param appName
	 *
	 * @return the working dir for the appName in the specific OS home directory
	 * @throws IOException
	 */

	public static File getAppDir(String appName) throws IOException {

		String s = System.getProperty("user.home", ".");

		File file;

		switch(getOS()) {
			case WINDOWS:
				String s1 = System.getenv("APPDATA");
				if(s1 != null) file = new File(s1, "." + appName + '/');
				else file = new File(s, '.' + appName + '/');
				break;
			case MAC:
				file = new File(s, "Library/Application Support/" + appName);
				break;
			case LINUX:
			case SOLARIS:
				file = new File(s, '.' + appName + '/');
				break;

			default:
				file = new File(s, appName + '/');
				break;
		}

		if(!file.exists() && !file.mkdirs()) {
			throw new IOException("Error: Failed to create working directory: " + file);
		} else if(!file.isDirectory()) {
			throw new IOException("Error: File is not a directory: " + file);
		} else {
			return file;
		}
	}

	//	private String getSerial(){
	//		switch(this){
	//			case LINUX: return Hardware4Nix.getSerialNumber();
	//			case SOLARIS: return Hardware4Nix.getSerialNumber();
	//			case MAC: return Hardware4Mac.getSerialNumber();
	//			case WINDOWS: return Hardware4Win.getSerialNumber();
	//			case UNKNOWN: return Hardware4Win.getSerialNumber();
	//			default: throw new IllegalArgumentException("No operationg system: "+OS);
	//		}
	//	}
	public static OperatingSystem getOS() {
		if(OS == null) {
			String s = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
			OS = UNKNOWN;
			for(OperatingSystem os : values()) {
				for(int i = 0; i < os.ids.length; i++) {
					if(s.contains(os.ids[i])) {
						try {
							os.serial = "not retrieved";
							//							System.err.println("READ SERIAL for "+os.name()+": "+os.serial);
						} catch(Exception e) {
							e.printStackTrace();
						}
						OS = os;
						return OS;
					}
				}
			}
		}

		return OS;
	}
}

