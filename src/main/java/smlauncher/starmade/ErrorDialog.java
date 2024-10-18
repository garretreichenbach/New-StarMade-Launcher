package smlauncher.starmade;

import smlauncher.LogManager;
import smlauncher.StarMadeLauncher;
import smlauncher.community.LauncherCommunityPanel;
import smlauncher.util.OperatingSystem;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class ErrorDialog extends JDialog {

	private static final int STACKTRACE_LIMIT = 10;

	public ErrorDialog(String error, String description, Throwable exception, boolean exitAfterConfirmation) {
		StarMadeLauncher.emergencyStop();
		setTitle("Error");
		setSize(500, 430);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setLayout(new BorderLayout());
		setResizable(false);
		setAlwaysOnTop(true);

		JPanel errorPanel = new JPanel();
		errorPanel.setLayout(new BoxLayout(errorPanel, BoxLayout.Y_AXIS));
		errorPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JTextArea errorLabel = new JTextArea(error);
		errorLabel.setEditable(false);
		errorLabel.setFont(new Font("Arial", Font.BOLD, 16));
		errorPanel.add(errorLabel, BorderLayout.NORTH);

		try {
			if(OperatingSystem.getCurrent() == OperatingSystem.MAC) {
				description += "\n\nNote: MacOS detected. Please understand that Mac support is experimental at the moment and will likely have some issues.\nWe apologize for the inconvenience and are working on fixing things to the best of our ability.";
			} else if(OperatingSystem.getCurrent() == OperatingSystem.LINUX) {
				description += "\n\nNote: Linux detected. Please understand that Linux support is experimental at the moment and will likely have some issues.\nWe apologize for the inconvenience and are working on fixing things to the best of our ability.";
			}
		} catch(Exception exception1) {
			exception1.printStackTrace();
		}
		
		JPanel descriptionPanel = new JPanel();
		descriptionPanel.setLayout(new BoxLayout(descriptionPanel, BoxLayout.X_AXIS));
		descriptionPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		add(descriptionPanel, BorderLayout.CENTER);
		
		StringBuilder descriptionBuilder = new StringBuilder();
		descriptionBuilder.append("Description:\n");
		descriptionBuilder.append("\t").append(description).append("\n");
		descriptionBuilder.append("Message:\n");
		descriptionBuilder.append("\t").append(exception.getMessage()).append("\n");
		
		JTextArea descriptionLabel = new JTextArea(descriptionBuilder.toString());
		descriptionLabel.setEditable(false);
		descriptionLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		descriptionPanel.add(descriptionLabel, BorderLayout.NORTH);
		
		StringBuilder stackTrace = new StringBuilder();
		stackTrace.append("Stack Trace:\n");
		int i = 0;
		for(StackTraceElement element : exception.getStackTrace()) {
			if(i > STACKTRACE_LIMIT) {
				stackTrace.append("\t...").append("\n");
				break;
			}
			stackTrace.append("\t").append(element.toString()).append("\n");
			i ++;
		}

		JTextArea stackTraceLabel = new JTextArea(stackTrace.toString());
		descriptionLabel.setEditable(false);
		stackTraceLabel.setFont(new Font("Arial", Font.BOLD, 12));
		descriptionPanel.add(stackTraceLabel, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		add(buttonPanel, BorderLayout.SOUTH);

		JButton okButton = new JButton("Ok");
		okButton.addActionListener(e -> {
			dispose();
			if(exitAfterConfirmation) System.exit(-1);
		});
		buttonPanel.add(okButton);
		buttonPanel.add(Box.createHorizontalGlue());

		JButton reportButton = new JButton("Report");
		String finalDescription = description;
		reportButton.addActionListener(e -> {
			dispose();
			File reportFile = LogManager.createErrorReport(error, finalDescription, exception);
			if(reportFile != null) JOptionPane.showMessageDialog(null, "Error report saved to: " + reportFile.getAbsolutePath(), "\nPlease create a new bug report on the GitHub issues page and upload this file!", JOptionPane.INFORMATION_MESSAGE);
			else JOptionPane.showMessageDialog(null, "Failed to save error report", "Error Report", JOptionPane.ERROR_MESSAGE);
			try {
				Desktop.getDesktop().browse(new URI(StarMadeLauncher.BUG_REPORT_URL));
			} catch(IOException | URISyntaxException exception1) {
				exception1.printStackTrace();
				throw new RuntimeException(exception1);
			}
			if(exitAfterConfirmation) System.exit(-1);
		});
		buttonPanel.add(reportButton);
		buttonPanel.add(Box.createHorizontalGlue());

		JButton starmadeDiscordButton = new JButton("StarMade Discord");
		starmadeDiscordButton.addActionListener(e -> {
			if(Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
				try {
					Desktop.getDesktop().browse(new URI(LauncherCommunityPanel.MAIN_DISCORD_URL));
				} catch(IOException | URISyntaxException exception1) {
					exception1.printStackTrace();
					throw new RuntimeException(exception1);
				}
			}
		});
		buttonPanel.add(starmadeDiscordButton);
		buttonPanel.add(Box.createHorizontalGlue());

		JButton supportDiscordButton = new JButton("Support Discord");
		supportDiscordButton.addActionListener(e -> {
			if(Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
				try {
					Desktop.getDesktop().browse(new URI(LauncherCommunityPanel.STARLOADER_DISCORD_URL));
				} catch(IOException | URISyntaxException exception1) {
					exception1.printStackTrace();
					throw new RuntimeException(exception1);
				}
			}
		});
		buttonPanel.add(supportDiscordButton);
	}
}
