package smlauncher.starmade;

import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.jasypt.util.text.BasicTextEncryptor;
import smlauncher.LogManager;
import smlauncher.util.OperatingSystem;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class StarMadeCredentials {

	private final String passwd;
	private final String user;

	public StarMadeCredentials(String user, String passwd) {
		this.passwd = passwd;
		this.user = user;
	}

	public static File getPath() throws IOException {
		return OperatingSystem.getAppDir();
	}

	public static boolean exists() {
		try {
			return (new File(getPath(), "cred")).exists();
		} catch(IOException e) {
			LogManager.logWarning("Error checking for credentials file", e);
		}
		return false;
	}

	public static StarMadeCredentials read() throws Exception {
		String passwd;
		String user;

		try(BufferedReader r = new BufferedReader(new FileReader(new File(getPath(), "cred"), StandardCharsets.UTF_8))) {
			user = r.readLine();
			String encryptedPasswd = r.readLine();
			BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
			textEncryptor.setPassword(getMac());
			passwd = textEncryptor.decrypt(encryptedPasswd.replaceAll("(\\r|\\n)", ""));
		} catch(EncryptionOperationNotPossibleException e) {
			removeFile();
			throw new Exception("Something went wrong reading your encrypted uplink credentials. This can happen due to operating system updates and/or a lot of other reasons.\n\nFIX: Please use the uplink button in 'Online Play' to re-enter your star-made.org credentials.\n", e);
			//throw new Exception(Lng.str("Something went wrong reading your encrypted uplink credentials. This can happen due to operating system updates and/or a lot of other reasons.\n\nFIX: Please use the uplink button in 'Online Play' to re-enter your star-made.org credentials.\n"), e);
		} catch(Exception e) {
			removeFile();
			throw e;
		}
		return new StarMadeCredentials(user, passwd);
	}

	public static void removeFile() throws IOException {
		(new File(getPath(), "cred")).delete();
	}

	private static String getJavaExec() {
		if("Mac OS X".equals(System.getProperty("os.name")) || System.getProperty("os.name").contains("Linux")) {
			return "java";
		} else {
			return "javaw";
		}
	}

	public static String getMac() {
		String starMadePath = "./data/mac.jar";
		File starmadeFile = new File(starMadePath);
		String[] command = null;
		//-XX:+ShowMessageBoxOnError
		command = new String[]{getJavaExec(), "-jar", starmadeFile.getAbsolutePath()};

		ProcessBuilder pb = new ProcessBuilder(command);
		Map<String, String> env = pb.environment();
		pb.directory(new File("./"));
		try {
			Process p = pb.start();
			BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8));
			return r.readLine();
		} catch(IOException e) {
			// TODO Auto-generated catch block
			LogManager.logFatal("Error getting mac jar", e);
		}
		return null;
	}

	public static void main(String[] args) throws Exception {
		try {
			StarMadeCredentials c = read();
			c = read();
			System.err.println(c.passwd);
		} catch(IOException e) {
			LogManager.logException("Error reading credentials", e);
		}
	}

	/**
	 * @return the passwd
	 */
	public String getPasswd() {
		return passwd;
	}

	/**
	 * @return the user
	 */
	public String getUser() {
		return user;
	}

	public void write() throws IOException {
		removeFile();
		BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
		String mac = getMac();
		textEncryptor.setPassword(mac);
		String encryptedPasswd = textEncryptor.encrypt(passwd);
		FileWriter fw = new FileWriter(new File(getPath(), "cred"), StandardCharsets.UTF_8);
		BufferedWriter bfw = new BufferedWriter(fw);
		bfw.append(user);
		bfw.newLine();
		bfw.append(encryptedPasswd);
		bfw.newLine();
		bfw.flush();
		bfw.close();
	}
}
