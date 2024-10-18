package smlauncher.starmade;

import smlauncher.LogManager;
import smlauncher.StarMadeLauncher;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

// TODO seems like duplicate of misc.ErrorDialog

/**
 * Simple dialog for showing error messages.
 *
 * @author TheDerpGamer (TheDerpGamer#0027)
 */
public class ErrorDialog extends JDialog {

	public ErrorDialog(String error, String description, Throwable exception, boolean exitAfterConfirmation) {
		setTitle("Error");
		setSize(400, 200);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setLayout(new BorderLayout());
		setResizable(false);

		JPanel errorPanel = new JPanel();
		errorPanel.setLayout(new BoxLayout(errorPanel, BoxLayout.Y_AXIS));
		errorPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JLabel errorLabel = new JLabel(error);
		errorLabel.setFont(new Font("Arial", Font.BOLD, 16));
		errorPanel.add(errorLabel, BorderLayout.NORTH);

		JLabel descriptionLabel = new JLabel(description);
		descriptionLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		errorPanel.add(descriptionLabel);
		add(errorPanel, BorderLayout.CENTER);

		JLabel messageLabel = new JLabel(exception.getMessage());
		messageLabel.setFont(new Font("Arial", Font.PLAIN, 12));
		errorPanel.add(messageLabel);
		add(errorPanel, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		add(buttonPanel, BorderLayout.SOUTH);

		JButton okButton = new JButton("Ok");
		okButton.addActionListener(e -> {
			dispose();
			if(exitAfterConfirmation) System.exit(-1);
		});
		buttonPanel.add(okButton);

		JButton reportButton = new JButton("Report");
		reportButton.addActionListener(e -> {
			dispose();
			File reportFile = LogManager.createErrorReport(error, description, exception);
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
	}
}
