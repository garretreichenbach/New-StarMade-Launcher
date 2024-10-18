package smlauncher;

import smlauncher.starmade.ErrorDialog;
import smlauncher.starmade.GameBranch;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * [Description]
 *
 * @author TheDerpGamer
 */
public class LogManager {

	private static FileWriter logWriter;
	private static File logFile;

	public static void initialize() {
		try {
			File logsFolder = new File(LaunchSettings.getInstallDir() + "/logs");
			File[] logFiles = logsFolder.listFiles();
			if(logFiles != null) {
				for(File logFile : logFiles) {
					try {
						String fileName = logFile.getName();
						if(fileName.startsWith("launcher.") && fileName.endsWith(".log")) {
							String[] split = fileName.split("\\.");
							int logNumber = Integer.parseInt(split[1]);
							File newLogFile = new File(LaunchSettings.getInstallDir() + "/logs/launcher." + (logNumber + 1) + ".log");
							logFile.renameTo(newLogFile);
						}
					} catch(Exception exception) {
						exception.printStackTrace();
					}
				}
			}
			logFile = new File(LaunchSettings.getInstallDir() + "/logs/launcher.0.log");
			if(logFile.exists()) logFile.delete();
			logFile.createNewFile();
			logWriter = new FileWriter(logFile, StandardCharsets.UTF_8);
		} catch(Exception exception) {
			exception.printStackTrace();
		}
	}

	public static void logInfo(String message) {
		System.out.println("[INFO]: " + message);
		try {
			logWriter.append("[INFO]: ").append(message).append("\n");
			logWriter.flush();
		} catch(Exception exception) {
			exception.printStackTrace();
		}
	}

	public static void logWarning(String message) {
		System.out.println("[WARNING]: " + message);
		try {
			logWriter.append("[WARNING]: ").append(message).append("\n");
			logWriter.flush();
		} catch(Exception exception) {
			exception.printStackTrace();
		}
	}

	public static void logWarning(String message, Exception exception) {
		System.out.println("[WARNING]: " + message);
		try {
			logWriter.append("[WARNING]: ").append(message).append("\n");
			logWriter.append(exception.getMessage()).append("\n");
			logWriter.flush();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void logException(String message, Exception exception) {
		System.out.println("[ERROR]: " + message);
		try {
			logWriter.append("[ERROR]: ").append(message).append("\n");
			logWriter.append(exception.getMessage()).append("\n");
			logWriter.flush();
			(new ErrorDialog("Error", message, exception, false)).setVisible(true);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void logFatal(String message, Throwable exception) {
		System.out.println("[FATAL]: " + message);
		try {
			logWriter.append("[FATAL]: ").append(message).append("\n");
			logWriter.append(exception.getMessage()).append("\n");
			logWriter.flush();
			(new ErrorDialog("Fatal Error", message, exception, true)).setVisible(true);	
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static File createErrorReport(String error, String description, Throwable exception) {
		try {
			String date = (new Date()).toString().replace(" ", "_").replace(":", "-");
			File errorReport = new File("error_report_" + date + ".txt");
			FileWriter writer = new FileWriter(errorReport, StandardCharsets.UTF_8);
			writer.append("StarMade Launcher Error Report [").append(date).append("]:\n\n");

			writer.append("System Information:\n");
			writer.append("\tOS: ").append(System.getProperty("os.name")).append("\n");
			writer.append("\tOS Version: ").append(System.getProperty("os.version")).append("\n");
			writer.append("\tJava Version: ").append(System.getProperty("java.version")).append("\n");
			writer.append("\tJava Vendor: ").append(System.getProperty("java.vendor")).append("\n");
			writer.append("\tJava Home: ").append(System.getProperty("java.home")).append("\n");
			writer.append("\tCurrent Directory: ").append(System.getProperty("user.dir")).append("\n\n");
			
			writer.append("Launcher State Information:\n");
			writer.append("\tInstall Directory: ").append(LaunchSettings.getInstallDir()).append("\n");
			writer.append("\tLast Used Branch: ").append(GameBranch.values()[LaunchSettings.getLastUsedBranch()].name).append("\n");
			writer.append("\tLast Used Version: ").append(LaunchSettings.getLastUsedVersion()).append("\n");
			writer.append("\tLaunch Arguments: ").append(LaunchSettings.getLaunchArgs()).append("\n");
			writer.append("\tMemory: ").append(String.valueOf(LaunchSettings.getMemory())).append("\n");
			writer.append("\tJVM Arguments: ").append(LaunchSettings.getJvmArgs()).append("\n");
			writer.append("\tStarMade.jar Exists: ").append(String.valueOf(new File(LaunchSettings.getInstallDir() + "/StarMade.jar").exists())).append("\n\n");
			writer.append("\tJava 8 Folder Exists: ").append(String.valueOf(new File(LaunchSettings.getInstallDir() + "/jre8"))).append("\n");
			writer.append("\tJava 18 Folder Exists: ").append(String.valueOf(new File(LaunchSettings.getInstallDir() + "/jre18").exists())).append("\n");
			writer.append("\tData Folder Exists: ").append(String.valueOf(new File(LaunchSettings.getInstallDir() + "/data").exists())).append("\n\n");
			
			writer.append("Error Details:\n");
			writer.append("\tError: ").append(error).append("\n");
			writer.append("\tDescription: ").append(description).append("\n");
			writer.append("\tException: ").append(exception.getMessage()).append("\n");
			writer.flush();
			writer.close();
			return errorReport;
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
